package com.example.pos.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuItemDao {
    @Query("SELECT * FROM menu_items ORDER BY sortOrder ASC, name ASC")
    fun getAllMenuItems(): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE category = :category ORDER BY sortOrder ASC, name ASC")
    fun getMenuItemsByCategory(category: String): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE isAvailable = 1 ORDER BY sortOrder ASC, name ASC")
    fun getAvailableMenuItems(): Flow<List<MenuItemEntity>>

    @Query("SELECT COUNT(*) FROM menu_items")
    suspend fun getMenuItemCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItem(item: MenuItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItems(items: List<MenuItemEntity>)

    @Update
    suspend fun updateMenuItem(item: MenuItemEntity)

    @Delete
    suspend fun deleteMenuItem(item: MenuItemEntity)

    @Query("DELETE FROM menu_items WHERE id = :id")
    suspend fun deleteMenuItemById(id: Long)

    @Query("DELETE FROM menu_items")
    suspend fun deleteAllMenuItems()
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE dateString = :dateString ORDER BY timestamp DESC")
    fun getOrdersForDate(dateString: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getOrdersInRange(startTime: Long, endTime: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE billNumber = :billNumber LIMIT 1")
    suspend fun getOrderByBillNumber(billNumber: Long): OrderEntity?

    @Query("SELECT * FROM orders WHERE isSynced = 0 ORDER BY timestamp ASC")
    suspend fun getUnsyncedOrders(): List<OrderEntity>

    @Query("UPDATE orders SET isSynced = 1 WHERE billId = :billId")
    suspend fun markOrderAsSynced(billId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Query("SELECT MAX(billNumber) FROM orders")
    suspend fun getMaxBillNumber(): Long?

    @Query("SELECT MAX(tokenNumber) FROM orders WHERE dateString = :dateString")
    suspend fun getMaxTokenNumberForDate(dateString: String): Int?

    @Query("SELECT COUNT(*) FROM orders")
    fun getTotalOrdersCount(): Flow<Int>
}

@Dao
interface AppSettingDao {
    @Query("SELECT * FROM app_settings")
    fun getAllSettings(): Flow<List<AppSettingEntity>>

    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSettingValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSetting(setting: AppSettingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: List<AppSettingEntity>)
}
