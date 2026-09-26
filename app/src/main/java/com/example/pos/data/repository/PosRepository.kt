package com.example.pos.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.pos.data.local.AppSettingDao
import com.example.pos.data.local.AppSettingEntity
import com.example.pos.data.local.DefaultMenuData
import com.example.pos.data.local.MenuItemDao
import com.example.pos.data.local.MenuItemEntity
import com.example.pos.data.local.OrderDao
import com.example.pos.data.local.OrderEntity
import com.example.pos.data.remote.SalesFirestoreRepository
import com.example.pos.data.remote.SyncState
import com.example.pos.model.BillRecord
import com.example.pos.model.CartItem
import com.example.pos.model.ItemFontSize
import com.example.pos.model.MenuItem
import com.example.pos.model.OrderType
import com.example.pos.model.PaymentMode
import com.example.pos.model.PrinterPaperWidth
import com.example.pos.model.ReceiptFormatConfig
import com.example.pos.model.ShopSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class PosRepository(
    private val context: Context,
    private val menuItemDao: MenuItemDao,
    private val orderDao: OrderDao,
    private val settingDao: AppSettingDao,
    private val firestoreRepository: SalesFirestoreRepository,
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "PosRepository"
    }

    private val counterMutex = Mutex()

    private val _settingsState = MutableStateFlow(ShopSettings())
    val settingsState: StateFlow<ShopSettings> = _settingsState.asStateFlow()

    val syncState: StateFlow<SyncState> = firestoreRepository.syncState

    val allMenuItems: Flow<List<MenuItem>> = menuItemDao.getAllMenuItems().map { list ->
        list.map { it.toModel() }
    }

    val availableMenuItems: Flow<List<MenuItem>> = menuItemDao.getAvailableMenuItems().map { list ->
        list.map { it.toModel() }
    }

    val allOrders: Flow<List<BillRecord>> = orderDao.getAllOrders().map { list ->
        list.map { it.toModel() }
    }

    init {
        // Initialize settings and default menu seed
        scope.launch(Dispatchers.IO) {
            initSettings()
            checkAndSeedMenu()
        }
    }

    private suspend fun checkAndSeedMenu() {
        val count = menuItemDao.getMenuItemCount()
        if (count == 0) {
            Log.i(TAG, "Database menu is empty. Seeding initial categories and items...")
            menuItemDao.insertMenuItems(DefaultMenuData.INITIAL_MENU_ITEMS)
        }
    }

    private suspend fun initSettings() {
        try {
            val shopName = settingDao.getSettingValue("shop_name") ?: "RK TIFFINES"
            val address = settingDao.getSettingValue("address") ?: "Kadapa, Andhra Pradesh, India"
            val phone = settingDao.getSettingValue("phone") ?: "9392509555"
            val logoPath = settingDao.getSettingValue("logo_path")
            val paperWidth = if (settingDao.getSettingValue("paper_width") == "58") {
                PrinterPaperWidth.WIDTH_58MM
            } else {
                PrinterPaperWidth.WIDTH_80MM
            }
            val billItemFontSize = if (settingDao.getSettingValue("bill_item_font_size") == "LARGE") {
                ItemFontSize.LARGE
            } else {
                ItemFontSize.NORMAL
            }
            val kotItemFontSize = if (settingDao.getSettingValue("kot_item_font_size") == "NORMAL") {
                ItemFontSize.NORMAL
            } else {
                ItemFontSize.LARGE
            }
            val autoPrintBill = settingDao.getSettingValue("auto_print_bill")?.toBooleanStrictOrNull() ?: true
            val autoPrintToken = settingDao.getSettingValue("auto_print_token")?.toBooleanStrictOrNull() ?: false
            val autoPrintBoth = settingDao.getSettingValue("auto_print_both")?.toBooleanStrictOrNull() ?: false

            val maxDbBill = orderDao.getMaxBillNumber() ?: 1000L
            val savedBillNum = settingDao.getSettingValue("next_bill_number")?.toLongOrNull() ?: 1001L
            val nextBillNumber = maxOf(savedBillNum, maxDbBill + 1)

            val todayDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
            val maxDbTokenToday = orderDao.getMaxTokenNumberForDate(todayDate) ?: 0
            val savedTokenNum = settingDao.getSettingValue("next_token_number")?.toIntOrNull() ?: 1
            val nextTokenNumber = maxOf(savedTokenNum, maxDbTokenToday + 1)

            val savedPrinterMac = settingDao.getSettingValue("saved_printer_mac") ?: ""
            val savedPrinterName = settingDao.getSettingValue("saved_printer_name") ?: ""
            val autoReconnect = settingDao.getSettingValue("auto_reconnect")?.toBooleanStrictOrNull() ?: true
            val footer = settingDao.getSettingValue("receipt_footer") ?: "Thank you! Visit again"
            val showCust = settingDao.getSettingValue("show_cust_name")?.toBooleanStrictOrNull() ?: true
            val showTable = settingDao.getSettingValue("show_table_num")?.toBooleanStrictOrNull() ?: true
            val showLogo = settingDao.getSettingValue("show_logo")?.toBooleanStrictOrNull() ?: true
            val receiptFormatJson = settingDao.getSettingValue("receipt_format_json")
            val receiptFormat = ReceiptFormatConfig.fromJsonString(receiptFormatJson)

            _settingsState.value = ShopSettings(
                shopName = shopName,
                address = address,
                phone = phone,
                logoPath = logoPath,
                paperWidth = paperWidth,
                billItemFontSize = billItemFontSize,
                kotItemFontSize = kotItemFontSize,
                autoPrintBill = autoPrintBill,
                autoPrintToken = autoPrintToken,
                autoPrintBoth = autoPrintBoth,
                nextBillNumber = nextBillNumber,
                nextTokenNumber = nextTokenNumber,
                savedPrinterMac = savedPrinterMac,
                savedPrinterName = savedPrinterName,
                autoReconnectPrinter = autoReconnect,
                receiptFooter = footer,
                showCustomerName = showCust,
                showTableNumber = showTable,
                showLogo = showLogo,
                receiptFormat = receiptFormat
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error loading settings", e)
        }
    }

    suspend fun updateSettings(newSettings: ShopSettings) = withContext(Dispatchers.IO) {
        _settingsState.value = newSettings
        val entities = listOf(
            AppSettingEntity("shop_name", newSettings.shopName),
            AppSettingEntity("address", newSettings.address),
            AppSettingEntity("phone", newSettings.phone),
            AppSettingEntity("logo_path", newSettings.logoPath ?: ""),
            AppSettingEntity("paper_width", if (newSettings.paperWidth == PrinterPaperWidth.WIDTH_58MM) "58" else "80"),
            AppSettingEntity("bill_item_font_size", newSettings.billItemFontSize.name),
            AppSettingEntity("kot_item_font_size", newSettings.kotItemFontSize.name),
            AppSettingEntity("auto_print_bill", newSettings.autoPrintBill.toString()),
            AppSettingEntity("auto_print_token", newSettings.autoPrintToken.toString()),
            AppSettingEntity("auto_print_both", newSettings.autoPrintBoth.toString()),
            AppSettingEntity("next_bill_number", newSettings.nextBillNumber.toString()),
            AppSettingEntity("next_token_number", newSettings.nextTokenNumber.toString()),
            AppSettingEntity("saved_printer_mac", newSettings.savedPrinterMac),
            AppSettingEntity("saved_printer_name", newSettings.savedPrinterName),
            AppSettingEntity("auto_reconnect", newSettings.autoReconnectPrinter.toString()),
            AppSettingEntity("receipt_footer", newSettings.receiptFooter),
            AppSettingEntity("show_cust_name", newSettings.showCustomerName.toString()),
            AppSettingEntity("show_table_num", newSettings.showTableNumber.toString()),
            AppSettingEntity("show_logo", newSettings.showLogo.toString()),
            AppSettingEntity("receipt_format_json", newSettings.receiptFormat.toJsonString())
        )
        settingDao.saveSettings(entities)
    }

    suspend fun updateReceiptFormat(config: ReceiptFormatConfig) {
        val updated = _settingsState.value.copy(receiptFormat = config)
        updateSettings(updated)
    }

    suspend fun savePrinterDevice(mac: String, name: String) {
        val updated = _settingsState.value.copy(
            savedPrinterMac = mac,
            savedPrinterName = name
        )
        updateSettings(updated)
    }

    suspend fun forgetPrinter() {
        val updated = _settingsState.value.copy(
            savedPrinterMac = "",
            savedPrinterName = ""
        )
        updateSettings(updated)
    }

    suspend fun resetBillNumber(startingNumber: Long) {
        val updated = _settingsState.value.copy(nextBillNumber = startingNumber)
        updateSettings(updated)
    }

    suspend fun resetTokenNumber(startingToken: Int) {
        val updated = _settingsState.value.copy(nextTokenNumber = startingToken)
        updateSettings(updated)
    }

    suspend fun saveLogoBitmap(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val logoFile = File(context.filesDir, "shop_logo.png")
        FileOutputStream(logoFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        val path = logoFile.absolutePath
        val updated = _settingsState.value.copy(logoPath = path)
        updateSettings(updated)
        path
    }

    suspend fun removeLogo() = withContext(Dispatchers.IO) {
        val logoFile = File(context.filesDir, "shop_logo.png")
        if (logoFile.exists()) {
            logoFile.delete()
        }
        val updated = _settingsState.value.copy(logoPath = null)
        updateSettings(updated)
    }

    fun getLogoBitmap(): Bitmap? {
        val path = _settingsState.value.logoPath ?: return null
        val file = File(path)
        return if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    }

    // -------------------------------------------------------------------------
    // Order / Billing Management
    // -------------------------------------------------------------------------

    suspend fun createAndSaveOrder(
        cartItems: List<CartItem>,
        orderType: OrderType,
        tableNumber: String?,
        customerName: String?,
        paymentMode: PaymentMode
    ): BillRecord = withContext(Dispatchers.IO) {
        counterMutex.withLock {
            val now = Date()
            val dateFormatted = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(now)
            val timeFormatted = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(now)
            val timestamp = now.time

            val currentSettings = _settingsState.value
            val billNumber = currentSettings.nextBillNumber
            val tokenNumber = currentSettings.nextTokenNumber

            val nextBill = billNumber + 1
            val nextToken = tokenNumber + 1

            // Persist incremented counters
            val updatedSettings = currentSettings.copy(
                nextBillNumber = nextBill,
                nextTokenNumber = nextToken
            )
            updateSettings(updatedSettings)

            val subtotal = cartItems.sumOf { it.total }
            val grandTotal = subtotal
            val itemCount = cartItems.sumOf { it.quantity }
            val billId = UUID.randomUUID().toString()

            val itemsJson = serializeCartItems(cartItems)

            val orderEntity = OrderEntity(
                billId = billId,
                billNumber = billNumber,
                tokenNumber = tokenNumber,
                timestamp = timestamp,
                dateString = dateFormatted,
                timeString = timeFormatted,
                orderType = orderType.name,
                tableNumber = tableNumber?.trim()?.ifBlank { null },
                customerName = customerName?.trim()?.ifBlank { null },
                paymentMode = paymentMode.name,
                subtotal = subtotal,
                grandTotal = grandTotal,
                itemCount = itemCount,
                itemsJson = itemsJson,
                isSynced = false
            )

            // 1. Save locally to Room FIRST (guaranteed offline safety)
            orderDao.insertOrder(orderEntity)

            val billRecord = BillRecord(
                billId = billId,
                billNumber = billNumber,
                tokenNumber = tokenNumber,
                timestamp = timestamp,
                dateString = dateFormatted,
                timeString = timeFormatted,
                orderType = orderType,
                tableNumber = orderEntity.tableNumber,
                customerName = orderEntity.customerName,
                paymentMode = paymentMode,
                items = cartItems.map { it.copy() },
                subtotal = subtotal,
                grandTotal = grandTotal,
                itemCount = itemCount,
                isSynced = false
            )

            // 2. Asynchronously attempt Firestore sync in background (non-blocking for billing flow)
            scope.launch(Dispatchers.IO) {
                firestoreRepository.syncSingleOrder(orderEntity)
            }

            billRecord
        }
    }

    suspend fun syncSalesToFirestore(): Result<Int> {
        return firestoreRepository.syncPendingOrders()
    }

    // -------------------------------------------------------------------------
    // Menu Management
    // -------------------------------------------------------------------------

    suspend fun addMenuItem(item: MenuItem): Long = withContext(Dispatchers.IO) {
        menuItemDao.insertMenuItem(item.toEntity())
    }

    suspend fun updateMenuItem(item: MenuItem) = withContext(Dispatchers.IO) {
        menuItemDao.updateMenuItem(item.toEntity())
    }

    suspend fun deleteMenuItem(id: Long) = withContext(Dispatchers.IO) {
        menuItemDao.deleteMenuItemById(id)
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun serializeCartItems(items: List<CartItem>): String {
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("id", item.menuItem.id)
                put("name", item.menuItem.name)
                put("category", item.menuItem.category)
                put("price", item.menuItem.price)
                put("qty", item.quantity)
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun deserializeCartItems(json: String): List<CartItem> {
        val list = mutableListOf<CartItem>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val menuItem = MenuItem(
                    id = obj.optLong("id", 0),
                    name = obj.optString("name", "Item"),
                    category = obj.optString("category", ""),
                    price = obj.optDouble("price", 0.0),
                    isAvailable = true
                )
                val qty = obj.optInt("qty", 1)
                list.add(CartItem(menuItem, qty))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deserializing cart items", e)
        }
        return list
    }

    private fun MenuItemEntity.toModel(): MenuItem {
        return MenuItem(
            id = id,
            name = name,
            category = category,
            price = price,
            isAvailable = isAvailable,
            sortOrder = sortOrder
        )
    }

    private fun MenuItem.toEntity(): MenuItemEntity {
        return MenuItemEntity(
            id = id,
            name = name,
            category = category,
            price = price,
            isAvailable = isAvailable,
            sortOrder = sortOrder
        )
    }

    private fun OrderEntity.toModel(): BillRecord {
        return BillRecord(
            billId = billId,
            billNumber = billNumber,
            tokenNumber = tokenNumber,
            timestamp = timestamp,
            dateString = dateString,
            timeString = timeString,
            orderType = try {
                OrderType.valueOf(orderType)
            } catch (e: Exception) {
                OrderType.DINE_IN
            },
            tableNumber = tableNumber,
            customerName = customerName,
            paymentMode = try {
                PaymentMode.valueOf(paymentMode)
            } catch (e: Exception) {
                PaymentMode.CASH
            },
            items = deserializeCartItems(itemsJson),
            subtotal = subtotal,
            grandTotal = grandTotal,
            itemCount = itemCount,
            isSynced = isSynced
        )
    }
}
