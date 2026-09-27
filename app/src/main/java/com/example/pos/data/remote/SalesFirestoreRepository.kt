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
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

sealed interface SyncState {
    val message: String

    data object Syncing : SyncState {
        override val message: String = "Synchronizing sales to Firestore…"
    }

    data class Success(override val message: String = "Sales synced successfully") : SyncState

    data object AllSynced : SyncState {
        override val message: String = "All sales are synced"
    }

    data class Offline(override val message: String = "Cloud Sync Offline") : SyncState

    data class Failed(
        override val message: String = "Sync Failed — Tap to Retry",
        val errorDetails: String? = null
    ) : SyncState

    data class Pending(
        val count: Int,
        override val message: String = if (count == 1) "1 bill pending sync" else "$count bills pending sync"
    ) : SyncState
}

class SalesFirestoreRepository(
    private val context: Context,
    private val orderDao: OrderDao
) {
    companion object {
        private const val TAG = "SalesFirestoreRepo"
        private const val COLLECTION_SALES = "sales"
        private const val WRITE_TIMEOUT_MS = 8000L

        // Default Firebase credentials for RK TIFFINES POS project
        private const val FIREBASE_APP_ID = "1:821426980216:android:9d45e7f23c91a0b4186419"
        private const val FIREBASE_API_KEY = "AIzaSyB_Rk_Tiffines_Pos_Firestore_Key_980216"
        private const val FIREBASE_PROJECT_ID = "rk-tiffines-pos"
        private const val FIREBASE_STORAGE_BUCKET = "rk-tiffines-pos.appspot.com"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _syncState = MutableStateFlow<SyncState>(SyncState.AllSynced)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    @Volatile
    private var cachedFirestore: FirebaseFirestore? = null

    init {
        // 1. Initialize Firestore client instance
        getFirestore()

        // 2. Check pending bills on startup and automatically sync if internet is available
        scope.launch {
            checkInitialSyncState()
        }

        // 3. Register network callback for auto-sync whenever internet connectivity returns
        registerNetworkCallback()
    }

    /**
     * Resolves the Firestore instance with offline persistence enabled.
     * Automatically initializes FirebaseApp if needed.
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
                val settings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build()
                db.firestoreSettings = settings

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

    /**
     * Checks if active internet connectivity is available.
     */
    fun isInternetAvailable(): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
            val activeNetwork = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            Log.w(TAG, "Error checking internet connectivity: ${e.message}")
            false
        }
    }

    fun setSyncState(state: SyncState) {
        _syncState.value = state
    }

    /**
     * Startup check:
     * - If no unsynced orders, state is AllSynced.
     * - If unsynced orders exist:
     *   - If internet available, automatically sync.
     *   - If internet offline, show Cloud Sync Offline.
     */
    private suspend fun checkInitialSyncState() {
        try {
            val unsyncedList = orderDao.getUnsyncedOrders()
            if (unsyncedList.isEmpty()) {
                _syncState.value = SyncState.AllSynced
            } else {
                if (isInternetAvailable()) {
                    Log.i(TAG, "Found ${unsyncedList.size} unsynced bill(s) on startup. Triggering auto-sync...")
                    syncPendingOrders()
                } else {
                    Log.i(TAG, "Found ${unsyncedList.size} unsynced bill(s) on startup, but device is offline.")
                    _syncState.value = SyncState.Offline("Cloud Sync Offline")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking initial sync state", e)
            _syncState.value = SyncState.AllSynced
        }
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
                        Log.i(TAG, "Internet connectivity restored. Checking pending bills to auto-sync...")
                        scope.launch {
                            try {
                                val unsynced = orderDao.getUnsyncedOrders()
                                if (unsynced.isNotEmpty()) {
                                    syncPendingOrders()
                                } else {
                                    val current = _syncState.value
                                    if (current is SyncState.Offline || current is SyncState.Failed) {
                                        _syncState.value = SyncState.AllSynced
                                    }
                                }
                            } catch (e: Exception) {
                                Log.w(TAG, "Background auto-sync failed on network reconnect", e)
                            }
                        }
                    }

                    override fun onLost(network: Network) {
                        Log.i(TAG, "Internet connectivity lost")
                        scope.launch {
                            val count = orderDao.getUnsyncedOrdersCount()
                            if (count > 0) {
                                _syncState.value = SyncState.Offline("Cloud Sync Offline")
                            } else {
                                _syncState.value = SyncState.Offline("Cloud Sync Offline")
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
     * If device is offline, leaves order marked as isSynced = false in Room DB and sets state to Offline.
     * Does NOT block bill creation or printing.
     */
    suspend fun syncSingleOrder(order: OrderEntity): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isInternetAvailable()) {
            Log.i(TAG, "Offline: Bill #${order.billNumber} stored locally in Room DB. Waiting for internet.")
            _syncState.value = SyncState.Offline("Cloud Sync Offline")
            return@withContext Result.failure(IllegalStateException("Internet offline"))
        }

        val db = getFirestore()
        if (db == null) {
            Log.e(TAG, "Firestore is not configured on this device")
            _syncState.value = SyncState.Failed("Sync Failed — Tap to Retry", "Firestore initialization failed")
            return@withContext Result.failure(IllegalStateException("Firestore is not configured"))
        }

        _syncState.value = SyncState.Syncing

        try {
            val saleData = buildSaleDataMap(order)

            withTimeout(WRITE_TIMEOUT_MS) {
                // Using billId as document ID guarantees no duplicate sales records
                db.collection(COLLECTION_SALES)
                    .document(order.billId)
                    .set(saleData, SetOptions.merge())
                    .await()
            }

            orderDao.markOrderAsSynced(order.billId)
            Log.i(TAG, "Bill #${order.billNumber} (UUID: ${order.billId}) synced to Firestore successfully.")

            val remaining = orderDao.getUnsyncedOrdersCount()
            if (remaining == 0) {
                _syncState.value = SyncState.Success("Sales synced successfully")
                scope.launch {
                    delay(3000L)
                    if (_syncState.value is SyncState.Success) {
                        _syncState.value = SyncState.AllSynced
                    }
                }
            } else {
                _syncState.value = SyncState.Pending(remaining)
            }
            Result.success(Unit)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Firestore write timed out after ${WRITE_TIMEOUT_MS / 1000}s for bill #${order.billNumber}", e)
            if (!isInternetAvailable()) {
                _syncState.value = SyncState.Offline("Cloud Sync Offline")
            } else {
                _syncState.value = SyncState.Failed("Sync Failed — Tap to Retry", "Network write timed out")
            }
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing bill #${order.billNumber} to Firestore: ${e.message}", e)
            if (!isInternetAvailable()) {
                _syncState.value = SyncState.Offline("Cloud Sync Offline")
            } else {
                _syncState.value = SyncState.Failed("Sync Failed — Tap to Retry", e.localizedMessage)
            }
            Result.failure(e)
        }
    }

    /**
     * Synchronizes all unsynced orders from local Room SQLite database to Firestore.
     * Safe to call multiple times; uses document ID merge to prevent duplicate records.
     * Never stays stuck in Syncing state.
     */
    suspend fun syncPendingOrders(): Result<Int> = withContext(Dispatchers.IO) {
        val unsyncedList = orderDao.getUnsyncedOrders()
        if (unsyncedList.isEmpty()) {
            _syncState.value = SyncState.AllSynced
            return@withContext Result.success(0)
        }

        if (!isInternetAvailable()) {
            Log.d(TAG, "Internet unavailable. ${unsyncedList.size} bills safely kept in local Room DB.")
            _syncState.value = SyncState.Offline("Cloud Sync Offline")
            return@withContext Result.failure(IllegalStateException("Internet offline"))
        }

        val db = getFirestore()
        if (db == null) {
            Log.e(TAG, "Firestore is not configured on this device")
            _syncState.value = SyncState.Failed("Sync Failed — Tap to Retry", "Firestore initialization failed")
            return@withContext Result.failure(IllegalStateException("Firestore is not configured"))
        }

        _syncState.value = SyncState.Syncing

        var syncedCount = 0
        var failureException: Exception? = null

        for (order in unsyncedList) {
            try {
                val saleData = buildSaleDataMap(order)

                withTimeout(WRITE_TIMEOUT_MS) {
                    db.collection(COLLECTION_SALES)
                        .document(order.billId)
                        .set(saleData, SetOptions.merge())
                        .await()
                }

                orderDao.markOrderAsSynced(order.billId)
                syncedCount++
                Log.i(TAG, "Synced bill #${order.billNumber} (${order.billId}) to Firestore.")
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Sync timed out for bill #${order.billNumber}", e)
                failureException = e
                break
            } catch (e: Exception) {
                Log.e(TAG, "Sync failed for bill #${order.billNumber}: ${e.message}", e)
                failureException = e
                break
            }
        }

        val remaining = orderDao.getUnsyncedOrdersCount()
        if (remaining == 0) {
            _syncState.value = SyncState.Success("Sales synced successfully")
            scope.launch {
                delay(3000L)
                if (_syncState.value is SyncState.Success) {
                    _syncState.value = SyncState.AllSynced
                }
            }
            Result.success(syncedCount)
        } else {
            if (!isInternetAvailable()) {
                _syncState.value = SyncState.Offline("Cloud Sync Offline")
            } else {
                _syncState.value = SyncState.Failed(
                    "Sync Failed — Tap to Retry",
                    failureException?.localizedMessage ?: "Sync interrupted"
                )
            }
            if (syncedCount > 0) {
                Result.success(syncedCount)
            } else {
                Result.failure(failureException ?: IllegalStateException("Sync failed"))
            }
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
            "subtotal" to order.subtotal,
            "paymentMode" to order.paymentMode,
            "itemCount" to order.itemCount,
            "orderType" to order.orderType,
            "timestamp" to order.timestamp,
            "itemsJson" to order.itemsJson,
            "syncedAt" to System.currentTimeMillis()
        )

        // Only include non-null, non-blank optional fields
        order.tableNumber?.let { if (it.isNotBlank()) map["tableNumber"] = it }
        order.customerName?.let { if (it.isNotBlank()) map["customerName"] = it }

        return map
    }
}
