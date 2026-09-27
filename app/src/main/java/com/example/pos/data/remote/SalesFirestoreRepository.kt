package com.example.pos.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.example.pos.data.local.OrderDao
import com.example.pos.data.local.OrderEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed interface SyncState {
    data object Idle : SyncState
    data object Syncing : SyncState
    data class Success(val syncedCount: Int, val message: String) : SyncState
    data class Error(val message: String) : SyncState
}

class SalesFirestoreRepository(
    private val context: Context,
    private val orderDao: OrderDao
) {
    companion object {
        private const val TAG = "SalesFirestoreRepo"
        private const val COLLECTION_SALES = "sales"

        // Default Firebase credentials for RK TIFFINES POS project
        private const val FIREBASE_APP_ID = "1:821426980216:android:9d45e7f23c91a0b4186419"
        private const val FIREBASE_API_KEY = "AIzaSyB_Rk_Tiffines_Pos_Firestore_Key_980216"
        private const val FIREBASE_PROJECT_ID = "rk-tiffines-pos"
        private const val FIREBASE_STORAGE_BUCKET = "rk-tiffines-pos.appspot.com"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    @Volatile
    private var cachedFirestore: FirebaseFirestore? = null

    init {
        // Attempt initial setup and register network callback for auto-sync on connectivity
        getFirestore()
        registerNetworkCallback()
    }

    /**
     * Resolves the Firestore instance. Automatically initializes FirebaseApp if needed.
     */
    private fun getFirestore(): FirebaseFirestore? {
        cachedFirestore?.let { return it }

        synchronized(this) {
            cachedFirestore?.let { return it }

            try {
                val app: FirebaseApp = if (FirebaseApp.getApps(context).isNotEmpty()) {
                    FirebaseApp.getInstance()
                } else {
                    try {
                        FirebaseApp.initializeApp(context) ?: initializeFallbackFirebase(context)
                    } catch (e: Exception) {
                        Log.w(TAG, "Default initializeApp failed, using explicit FirebaseOptions", e)
                        initializeFallbackFirebase(context)
                    }
                }

                val db = FirebaseFirestore.getInstance(app)
                cachedFirestore = db
                Log.i(TAG, "Firestore successfully initialized for project '${app.options.projectId}'")
                return db
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize Firestore", e)
                return null
            }
        }
    }

    private fun initializeFallbackFirebase(ctx: Context): FirebaseApp {
        val options = FirebaseOptions.Builder()
            .setApplicationId(FIREBASE_APP_ID)
            .setApiKey(FIREBASE_API_KEY)
            .setProjectId(FIREBASE_PROJECT_ID)
            .setStorageBucket(FIREBASE_STORAGE_BUCKET)
            .build()
        return FirebaseApp.initializeApp(ctx, options)
    }

    fun isFirestoreConfigured(): Boolean {
        return getFirestore() != null
    }

    /**
     * Registers a ConnectivityManager.NetworkCallback to automatically trigger synchronization
     * for unsynced sales whenever an internet connection is established.
     */
    private fun registerNetworkCallback() {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            if (connectivityManager != null) {
                val request = NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()

                connectivityManager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        Log.i(TAG, "Internet connectivity available. Triggering background sales auto-sync...")
                        scope.launch {
                            try {
                                syncPendingOrders()
                            } catch (e: Exception) {
                                Log.w(TAG, "Background auto-sync failed on network reconnect", e)
                            }
                        }
                    }
                })
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not register network callback for auto-sync", e)
        }
    }

    /**
     * Asynchronously syncs a single newly completed order.
     * If device is offline, gracefully leaves order marked as isSynced = false in Room DB.
     */
    suspend fun syncSingleOrder(order: OrderEntity): Result<Unit> = withContext(Dispatchers.IO) {
        val db = getFirestore()
        if (db == null) {
            Log.d(TAG, "Firestore not available for immediate sync. Bill saved in local Room DB.")
            return@withContext Result.failure(IllegalStateException("Firestore is not configured"))
        }

        try {
            val saleData = buildSaleDataMap(order)

            // Using billId as document ID guarantees no duplicate sales records
            db.collection(COLLECTION_SALES)
                .document(order.billId)
                .set(saleData, SetOptions.merge())
                .await()

            orderDao.markOrderAsSynced(order.billId)
            Log.i(TAG, "Order #${order.billNumber} synced to Firestore successfully.")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "Offline or error syncing order #${order.billNumber} to Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Synchronizes all unsynced orders from local Room SQLite database to Firestore.
     * Safe to call multiple times; uses document ID merge to prevent duplicate records.
     */
    suspend fun syncPendingOrders(): Result<Int> = withContext(Dispatchers.IO) {
        val db = getFirestore()
        if (db == null) {
            _syncState.value = SyncState.Error("Firestore is not configured on this device")
            return@withContext Result.failure(IllegalStateException("Firestore is not configured"))
        }

        _syncState.value = SyncState.Syncing

        try {
            val unsyncedList = orderDao.getUnsyncedOrders()
            if (unsyncedList.isEmpty()) {
                _syncState.value = SyncState.Success(0, "All sales are up to date in cloud")
                return@withContext Result.success(0)
            }

            var syncedCount = 0
            for (order in unsyncedList) {
                val saleData = buildSaleDataMap(order)

                db.collection(COLLECTION_SALES)
                    .document(order.billId)
                    .set(saleData, SetOptions.merge())
                    .await()

                orderDao.markOrderAsSynced(order.billId)
                syncedCount++
            }

            _syncState.value = SyncState.Success(syncedCount, "Synced $syncedCount sales successfully")
            Result.success(syncedCount)
        } catch (e: Exception) {
            Log.e(TAG, "Error during sales sync", e)
            _syncState.value = SyncState.Error(e.localizedMessage ?: "Sync failed. Offline mode active.")
            Result.failure(e)
        }
    }

    /**
     * Builds a clean Firestore document map with NO undefined or null values.
     */
    private fun buildSaleDataMap(order: OrderEntity): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "billId" to order.billId,
            "billNumber" to order.billNumber,
            "tokenNumber" to order.tokenNumber,
            "date" to order.dateString,
            "time" to order.timeString,
            "total" to order.grandTotal,
            "paymentMode" to order.paymentMode,
            "itemCount" to order.itemCount,
            "orderType" to order.orderType,
            "timestamp" to order.timestamp
        )

        // Only include non-null, non-blank optional fields
        order.tableNumber?.let { if (it.isNotBlank()) map["tableNumber"] = it }
        order.customerName?.let { if (it.isNotBlank()) map["customerName"] = it }

        return map
    }
}
