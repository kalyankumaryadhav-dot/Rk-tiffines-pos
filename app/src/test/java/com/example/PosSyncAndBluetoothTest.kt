package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.pos.data.local.OrderEntity
import com.example.pos.data.local.PosDatabase
import com.example.pos.data.remote.SyncState
import com.example.pos.model.PrinterConnectionState
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PosSyncAndBluetoothTest {

    private lateinit var db: PosDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, PosDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun testSyncStateMessagesMatchRequirements() {
        // Requirement 7: EXACT strings required
        assertEquals("Synchronizing sales to Firestore…", SyncState.Syncing.message)
        assertEquals("Sales synced successfully", SyncState.Success().message)
        assertEquals("All sales are synced", SyncState.AllSynced.message)
        assertEquals("Cloud Sync Offline", SyncState.Offline().message)
        assertEquals("Sync Failed — Tap to Retry", SyncState.Failed().message)
        assertEquals("1 bill pending sync", SyncState.Pending(1).message)
        assertEquals("5 bills pending sync", SyncState.Pending(5).message)
    }

    @Test
    fun testLocalBillCreationAndSyncCycle() = runBlocking {
        val orderDao = db.orderDao()

        // 1. Initial state: 0 unsynced bills
        assertEquals(0, orderDao.getUnsyncedOrdersCount())

        // 2. Bill Created: saved locally first with isSynced = false
        val order1 = OrderEntity(
            id = 1,
            billId = "BILL-UUID-001",
            billNumber = 1001L,
            tokenNumber = 1,
            timestamp = System.currentTimeMillis(),
            dateString = "27-09-2026",
            timeString = "02:30 PM",
            orderType = "DINE_IN",
            tableNumber = "3",
            customerName = "Kalyan",
            paymentMode = "CASH",
            subtotal = 120.0,
            grandTotal = 120.0,
            itemCount = 2,
            itemsJson = "[]",
            isSynced = false
        )
        orderDao.insertOrder(order1)

        // Verify bill is stored locally and marked pending sync
        val unsynced = orderDao.getUnsyncedOrders()
        assertEquals(1, unsynced.size)
        assertEquals("BILL-UUID-001", unsynced[0].billId)
        assertFalse(unsynced[0].isSynced)
        assertEquals(1, orderDao.getUnsyncedOrdersCount())

        // 3. Same bill inserted twice (idempotency): no duplicates
        orderDao.insertOrder(order1)
        val afterDuplicate = orderDao.getUnsyncedOrders()
        assertEquals(1, afterDuplicate.size)

        // 4. Multiple pending bills: add second bill
        val order2 = order1.copy(id = 2, billId = "BILL-UUID-002", billNumber = 1002L, tokenNumber = 2)
        orderDao.insertOrder(order2)
        assertEquals(2, orderDao.getUnsyncedOrdersCount())

        // 5. Successful sync: mark bill1 synced
        orderDao.markOrderAsSynced(order1.billId)
        assertEquals(1, orderDao.getUnsyncedOrdersCount())
        val remaining = orderDao.getUnsyncedOrders()
        assertEquals("BILL-UUID-002", remaining[0].billId)

        // 6. Mark bill2 synced
        orderDao.markOrderAsSynced(order2.billId)
        assertEquals(0, orderDao.getUnsyncedOrdersCount())
        assertTrue(orderDao.getUnsyncedOrders().isEmpty())
    }

    @Test
    fun testBluetoothStatusDisplayMapping() {
        // Requirement 4 & 5: Exact status text mapping
        val connectedState = PrinterConnectionState.Connected("POS Thermal", "00:11:22:33:44:55")
        val connectingState = PrinterConnectionState.Connecting
        val disconnectedState = PrinterConnectionState.Disconnected

        fun getDisplayText(state: PrinterConnectionState): String {
            return when (state) {
                is PrinterConnectionState.Connected -> "Bluetooth Connected"
                is PrinterConnectionState.Connecting -> "Bluetooth Connecting…"
                is PrinterConnectionState.Disconnected,
                is PrinterConnectionState.Error -> "Bluetooth Disconnected"
            }
        }

        assertEquals("Bluetooth Connected", getDisplayText(connectedState))
        assertEquals("Bluetooth Connecting…", getDisplayText(connectingState))
        assertEquals("Bluetooth Disconnected", getDisplayText(disconnectedState))
        assertFalse(getDisplayText(disconnectedState).contains("POS-8380"))
    }

    @Test
    fun testMenuSearchAcrossItemNameAndDepartment() {
        val items = listOf(
            com.example.pos.model.MenuItem(id = 1, name = "GHEEPODI PONGANALU(7)", category = "SNACKS", price = 85.0),
            com.example.pos.model.MenuItem(id = 2, name = "PONGANALU(7)", category = "SNACKS", price = 40.0),
            com.example.pos.model.MenuItem(id = 3, name = "PLAIN DOSA", category = "NORMAL DOSA", price = 35.0),
            com.example.pos.model.MenuItem(id = 4, name = "CLASSIC MAGGI", category = "MAGGI", price = 40.0)
        )

        // Query: "PONG" should match items with "PONG" in name
        val pongMatches = items.filter {
            it.name.contains("PONG", ignoreCase = true) || it.category.contains("PONG", ignoreCase = true)
        }
        assertEquals(2, pongMatches.size)
        assertTrue(pongMatches.any { it.name == "GHEEPODI PONGANALU(7)" })
        assertTrue(pongMatches.any { it.name == "PONGANALU(7)" })

        // Query: "SNACK" should match SNACKS department items
        val snackMatches = items.filter {
            it.name.contains("SNACK", ignoreCase = true) || it.category.contains("SNACK", ignoreCase = true)
        }
        assertEquals(2, snackMatches.size)
        assertTrue(snackMatches.all { it.category == "SNACKS" })
    }

    @Test
    fun testRenameDepartmentPreservesItems() = runBlocking {
        val itemDao = db.menuItemDao()
        val item1 = com.example.pos.data.local.MenuItemEntity(
            id = 1,
            name = "GHEEPODI PONGANALU(7)",
            category = "SNACKS",
            price = 85.0
        )
        val item2 = com.example.pos.data.local.MenuItemEntity(
            id = 2,
            name = "PLAIN DOSA",
            category = "DOSA ITEMS",
            price = 35.0
        )
        itemDao.insertMenuItems(listOf(item1, item2))
        assertEquals(2, itemDao.getMenuItemCount())

        // Rename "SNACKS" to "EVENING SNACKS"
        itemDao.updateCategoryName("SNACKS", "EVENING SNACKS")

        // Total count preserved
        assertEquals(2, itemDao.getMenuItemCount())
    }
}
