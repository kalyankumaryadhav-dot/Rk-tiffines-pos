package com.example.pos.data.remote

import android.content.Context
import android.util.Log
import com.example.pos.data.local.OrderDao
import com.example.pos.data.local.OrderEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    }

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val firestore: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseFirestore.getInstance()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase not yet initialized or available", e)
            null
        }
    }

    fun isFirestoreConfigured(): Boolean {
        return firestore != null
    }

    suspend fun syncSingleOrder(order: OrderEntity): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore
        if (db == null) {
            Log.d(TAG, "Firestore not available for immediate sync. Bill saved in local Room DB.")
            return@withContext Result.failure(IllegalStateException("Firestore not configured"))
        }

        try {
            val saleData = mutableMapOf<String, Any>(
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

            // Safe null checks - never write undefined/null keys into Firestore
            order.tableNumber?.let { if (it.isNotBlank()) saleData["tableNumber"] = it }
            order.customerName?.let { if (it.isNotBlank()) saleData["customerName"] = it }

            // Using billId as document ID guarantees no duplicate sales
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

    suspend fun syncPendingOrders(): Result<Int> = withContext(Dispatchers.IO) {
        val db = firestore
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
                val saleData = mutableMapOf<String, Any>(
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
                order.tableNumber?.let { if (it.isNotBlank()) saleData["tableNumber"] = it }
                order.customerName?.let { if (it.isNotBlank()) saleData["customerName"] = it }

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
}
