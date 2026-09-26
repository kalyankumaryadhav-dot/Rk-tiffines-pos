package com.example.pos.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "menu_items",
    indices = [Index(value = ["category"])]
)
data class MenuItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,
    val price: Double,
    val isAvailable: Boolean = true,
    val sortOrder: Int = 0
)

@Entity(
    tableName = "orders",
    indices = [
        Index(value = ["billId"], unique = true),
        Index(value = ["billNumber"], unique = true),
        Index(value = ["timestamp"]),
        Index(value = ["isSynced"])
    ]
)
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val billId: String,
    val billNumber: Long,
    val tokenNumber: Int,
    val timestamp: Long,
    val dateString: String,
    val timeString: String,
    val orderType: String,
    val tableNumber: String?,
    val customerName: String?,
    val paymentMode: String,
    val subtotal: Double,
    val grandTotal: Double,
    val itemCount: Int,
    val itemsJson: String,
    val isSynced: Boolean = false
)

@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey
    val key: String,
    val value: String
)
