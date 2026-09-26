package com.example.pos.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pos.data.local.PosDatabase
import com.example.pos.data.remote.SalesFirestoreRepository
import com.example.pos.data.remote.SyncState
import com.example.pos.data.repository.PosRepository
import com.example.pos.model.BillRecord
import com.example.pos.model.CartItem
import com.example.pos.model.ItemFontSize
import com.example.pos.model.MenuCategories
import com.example.pos.model.MenuItem
import com.example.pos.model.OrderType
import com.example.pos.model.PaymentMode
import com.example.pos.model.PrinterConnectionState
import com.example.pos.model.PrinterPaperWidth
import com.example.pos.model.ShopSettings
import com.example.pos.printer.BluetoothPrinterManager
import com.example.pos.printer.KotFormatter
import com.example.pos.printer.ReceiptFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class SalesSummary(
    val totalRevenue: Double = 0.0,
    val billCount: Int = 0,
    val cashTotal: Double = 0.0,
    val upiTotal: Double = 0.0,
    val cardTotal: Double = 0.0
)

enum class SalesTimeFilter {
    TODAY,
    WEEKLY,
    MONTHLY,
    ALL
}

class PosViewModel(application: Application) : AndroidViewModel(application) {

    private val database = PosDatabase.getInstance(application)
    private val firestoreRepo = SalesFirestoreRepository(application, database.orderDao())
    val repository = PosRepository(
        context = application,
        menuItemDao = database.menuItemDao(),
        orderDao = database.orderDao(),
        settingDao = database.appSettingDao(),
        firestoreRepository = firestoreRepo,
        scope = viewModelScope
    )

    val printerManager = BluetoothPrinterManager(application, viewModelScope)

    // UI Events (e.g. snackbar messages or print status alerts)
    private val _eventFlow = MutableSharedFlow<String>()
    val eventFlow: SharedFlow<String> = _eventFlow.asSharedFlow()

    // -------------------------------------------------------------------------
    // Billing State
    // -------------------------------------------------------------------------
    private val _selectedCategory = MutableStateFlow(MenuCategories.SOUTH_INDIAN)
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _orderType = MutableStateFlow(OrderType.DINE_IN)
    val orderType: StateFlow<OrderType> = _orderType.asStateFlow()

    private val _tableNumber = MutableStateFlow("1")
    val tableNumber: StateFlow<String> = _tableNumber.asStateFlow()

    private val _customerName = MutableStateFlow("")
    val customerName: StateFlow<String> = _customerName.asStateFlow()

    private val _paymentMode = MutableStateFlow(PaymentMode.CASH)
    val paymentMode: StateFlow<PaymentMode> = _paymentMode.asStateFlow()

    private val _lastCompletedBill = MutableStateFlow<BillRecord?>(null)
    val lastCompletedBill: StateFlow<BillRecord?> = _lastCompletedBill.asStateFlow()

    val settings: StateFlow<ShopSettings> = repository.settingsState
    val printerState: StateFlow<PrinterConnectionState> = printerManager.connectionState
    val syncState: StateFlow<SyncState> = repository.syncState

    val allMenuItems: StateFlow<List<MenuItem>> = repository.allMenuItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allOrders: StateFlow<List<BillRecord>> = repository.allOrders.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Sales filter
    private val _salesFilter = MutableStateFlow(SalesTimeFilter.TODAY)
    val salesFilter: StateFlow<SalesTimeFilter> = _salesFilter.asStateFlow()

    // Filtered orders & sales summary calculation
    val filteredOrders: StateFlow<List<BillRecord>> = combine(
        allOrders,
        _salesFilter
    ) { orders, filter ->
        filterOrders(orders, filter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val salesSummary: StateFlow<SalesSummary> = filteredOrders.combine(_salesFilter) { orders, _ ->
        calculateSummary(orders)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SalesSummary()
    )

    init {
        // Automatically check if printer was previously saved and reconnect
        viewModelScope.launch {
            settings.collect { currentSettings ->
                if (currentSettings.autoReconnectPrinter &&
                    currentSettings.savedPrinterMac.isNotBlank() &&
                    printerManager.connectionState.value is PrinterConnectionState.Disconnected
                ) {
                    printerManager.triggerAutoReconnect(
                        currentSettings.savedPrinterMac,
                        currentSettings.savedPrinterName.ifBlank { "POS-8380" }
                    )
                }
            }
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setOrderType(type: OrderType) {
        _orderType.value = type
    }

    fun setTableNumber(table: String) {
        _tableNumber.value = table
    }

    fun setCustomerName(name: String) {
        _customerName.value = name
    }

    fun setPaymentMode(mode: PaymentMode) {
        _paymentMode.value = mode
    }

    fun setSalesFilter(filter: SalesTimeFilter) {
        _salesFilter.value = filter
    }

    // -------------------------------------------------------------------------
    // Cart Actions
    // -------------------------------------------------------------------------
    fun addItemToCart(menuItem: MenuItem) {
        val current = _cartItems.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.menuItem.id == menuItem.id }
        if (existingIndex >= 0) {
            val item = current[existingIndex]
            current[existingIndex] = item.copy(quantity = item.quantity + 1)
        } else {
            current.add(CartItem(menuItem, 1))
        }
        _cartItems.value = current
    }

    fun decrementItemQuantity(menuItem: MenuItem) {
        val current = _cartItems.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.menuItem.id == menuItem.id }
        if (existingIndex >= 0) {
            val item = current[existingIndex]
            if (item.quantity > 1) {
                current[existingIndex] = item.copy(quantity = item.quantity - 1)
            } else {
                current.removeAt(existingIndex)
            }
            _cartItems.value = current
        }
    }

    fun removeItemFromCart(menuItem: MenuItem) {
        val current = _cartItems.value.toMutableList()
        current.removeAll { it.menuItem.id == menuItem.id }
        _cartItems.value = current
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _customerName.value = ""
    }

    // -------------------------------------------------------------------------
    // Order Completion & Printing
    // -------------------------------------------------------------------------
    enum class PrintChoice {
        BILL_ONLY,
        TOKEN_ONLY,
        BOTH,
        NONE
    }

    fun processOrderAndPrint(printChoice: PrintChoice) {
        val items = _cartItems.value
        if (items.isEmpty()) {
            viewModelScope.launch {
                _eventFlow.emit("Cart is empty! Add items to bill.")
            }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bill = repository.createAndSaveOrder(
                    cartItems = items,
                    orderType = _orderType.value,
                    tableNumber = if (_orderType.value == OrderType.DINE_IN) _tableNumber.value else null,
                    customerName = _customerName.value.ifBlank { null },
                    paymentMode = _paymentMode.value
                )

                _lastCompletedBill.value = bill

                // Reset cart on main thread
                _cartItems.value = emptyList()
                _customerName.value = ""

                // Determine what to print based on explicit action or shop settings
                val currentSettings = settings.value
                val shouldPrintBill = when (printChoice) {
                    PrintChoice.BILL_ONLY, PrintChoice.BOTH -> true
                    PrintChoice.TOKEN_ONLY -> false
                    PrintChoice.NONE -> currentSettings.autoPrintBill || currentSettings.autoPrintBoth
                }

                val shouldPrintToken = when (printChoice) {
                    PrintChoice.TOKEN_ONLY, PrintChoice.BOTH -> true
                    PrintChoice.BILL_ONLY -> false
                    PrintChoice.NONE -> currentSettings.autoPrintToken || currentSettings.autoPrintBoth
                }

                val logoBitmap = repository.getLogoBitmap()

                if (shouldPrintBill) {
                    val receiptBytes = ReceiptFormatter.buildCustomerReceipt(bill, currentSettings, logoBitmap)
                    val result = printerManager.sendBytes(receiptBytes)
                    if (result.isFailure) {
                        _eventFlow.emit("Bill #${bill.billNumber} saved, but printer offline.")
                    }
                }

                if (shouldPrintToken) {
                    val kotBytes = KotFormatter.buildKitchenTicket(bill, currentSettings)
                    printerManager.sendBytes(kotBytes)
                }

                _eventFlow.emit("Bill #${bill.billNumber} (Token #${bill.tokenNumber}) processed successfully!")
            } catch (e: Exception) {
                _eventFlow.emit("Error saving bill: ${e.message}")
            }
        }
    }

    fun reprintBill(bill: BillRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            val logoBitmap = repository.getLogoBitmap()
            val receiptBytes = ReceiptFormatter.buildCustomerReceipt(bill, settings.value, logoBitmap)
            val result = printerManager.sendBytes(receiptBytes)
            if (result.isSuccess) {
                _eventFlow.emit("Reprinted Bill #${bill.billNumber}")
            } else {
                _eventFlow.emit("Printer error: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun reprintToken(bill: BillRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            val kotBytes = KotFormatter.buildKitchenTicket(bill, settings.value)
            val result = printerManager.sendBytes(kotBytes)
            if (result.isSuccess) {
                _eventFlow.emit("Reprinted Token #${bill.tokenNumber}")
            } else {
                _eventFlow.emit("Printer error: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    // -------------------------------------------------------------------------
    // Printer Management
    // -------------------------------------------------------------------------
    fun connectPrinter(macAddress: String, name: String) {
        viewModelScope.launch {
            repository.savePrinterDevice(macAddress, name)
            printerManager.resetReconnectAttempts()
            val result = printerManager.connect(macAddress, name)
            if (result.isSuccess) {
                _eventFlow.emit("Connected to $name ($macAddress)")
            } else {
                _eventFlow.emit("Failed to connect to $name: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun disconnectPrinter() {
        printerManager.disconnect()
        viewModelScope.launch {
            _eventFlow.emit("Printer disconnected")
        }
    }

    fun forgetPrinter() {
        printerManager.disconnect()
        viewModelScope.launch {
            repository.forgetPrinter()
            _eventFlow.emit("Saved printer forgotten")
        }
    }

    fun testPrint() {
        viewModelScope.launch {
            val shopName = settings.value.shopName
            val result = printerManager.printTestReceipt(shopName)
            if (result.isSuccess) {
                _eventFlow.emit("Test receipt sent to POS-8380 printer!")
            } else {
                _eventFlow.emit("Printer test failed: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    // -------------------------------------------------------------------------
    // Settings Updates
    // -------------------------------------------------------------------------
    fun updateBusinessDetails(shopName: String, address: String, phone: String) {
        viewModelScope.launch {
            val updated = settings.value.copy(
                shopName = shopName.trim(),
                address = address.trim(),
                phone = phone.trim()
            )
            repository.updateSettings(updated)
            _eventFlow.emit("Business details updated")
        }
    }

    fun updateReceiptSettings(
        paperWidth: PrinterPaperWidth,
        billFontSize: ItemFontSize,
        kotFontSize: ItemFontSize,
        footer: String,
        showCustomer: Boolean,
        showTable: Boolean,
        showLogo: Boolean
    ) {
        viewModelScope.launch {
            val updated = settings.value.copy(
                paperWidth = paperWidth,
                billItemFontSize = billFontSize,
                kotItemFontSize = kotFontSize,
                receiptFooter = footer.trim(),
                showCustomerName = showCustomer,
                showTableNumber = showTable,
                showLogo = showLogo
            )
            repository.updateSettings(updated)
            _eventFlow.emit("Receipt settings saved")
        }
    }

    fun updateAutoPrintSettings(
        autoBill: Boolean,
        autoToken: Boolean,
        autoBoth: Boolean,
        autoReconnect: Boolean
    ) {
        viewModelScope.launch {
            val updated = settings.value.copy(
                autoPrintBill = autoBill,
                autoPrintToken = autoToken,
                autoPrintBoth = autoBoth,
                autoReconnectPrinter = autoReconnect
            )
            repository.updateSettings(updated)
            _eventFlow.emit("Print preferences saved")
        }
    }

    fun resetNextBillNumber(newBillNumber: Long) {
        viewModelScope.launch {
            repository.resetBillNumber(newBillNumber)
            _eventFlow.emit("Next bill number set to #$newBillNumber")
        }
    }

    fun resetNextTokenNumber(newTokenNumber: Int) {
        viewModelScope.launch {
            repository.resetTokenNumber(newTokenNumber)
            _eventFlow.emit("Next token number set to #$newTokenNumber")
        }
    }

    fun saveUploadedLogo(bitmap: Bitmap) {
        viewModelScope.launch {
            repository.saveLogoBitmap(bitmap)
            _eventFlow.emit("Logo uploaded and saved for thermal printing")
        }
    }

    fun removeLogo() {
        viewModelScope.launch {
            repository.removeLogo()
            _eventFlow.emit("Logo removed")
        }
    }

    // -------------------------------------------------------------------------
    // Menu Management Actions
    // -------------------------------------------------------------------------
    fun addMenuItem(name: String, category: String, price: Double) {
        viewModelScope.launch {
            val item = MenuItem(
                name = name.trim(),
                category = category,
                price = price,
                isAvailable = true
            )
            repository.addMenuItem(item)
            _eventFlow.emit("Added ${item.name} to $category")
        }
    }

    fun updateMenuItem(item: MenuItem) {
        viewModelScope.launch {
            repository.updateMenuItem(item)
            _eventFlow.emit("Updated ${item.name}")
        }
    }

    fun deleteMenuItem(id: Long) {
        viewModelScope.launch {
            repository.deleteMenuItem(id)
            _eventFlow.emit("Menu item removed")
        }
    }

    fun toggleItemAvailability(item: MenuItem) {
        viewModelScope.launch {
            repository.updateMenuItem(item.copy(isAvailable = !item.isAvailable))
        }
    }

    // -------------------------------------------------------------------------
    // Cloud Sync
    // -------------------------------------------------------------------------
    fun triggerFirestoreSync() {
        viewModelScope.launch {
            val result = repository.syncSalesToFirestore()
            if (result.isSuccess) {
                _eventFlow.emit("Cloud Sync: Synced ${result.getOrNull()} bills to Firestore")
            } else {
                _eventFlow.emit("Cloud Sync failed: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    // -------------------------------------------------------------------------
    // Calculations & Filtering
    // -------------------------------------------------------------------------
    private fun filterOrders(orders: List<BillRecord>, filter: SalesTimeFilter): List<BillRecord> {
        val calendar = Calendar.getInstance()
        val todayStr = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(calendar.time)

        return when (filter) {
            SalesTimeFilter.TODAY -> orders.filter { it.dateString == todayStr }
            SalesTimeFilter.WEEKLY -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val sevenDaysAgo = calendar.timeInMillis
                orders.filter { it.timestamp >= sevenDaysAgo }
            }
            SalesTimeFilter.MONTHLY -> {
                calendar.add(Calendar.DAY_OF_YEAR, -30)
                val thirtyDaysAgo = calendar.timeInMillis
                orders.filter { it.timestamp >= thirtyDaysAgo }
            }
            SalesTimeFilter.ALL -> orders
        }
    }

    private fun calculateSummary(orders: List<BillRecord>): SalesSummary {
        var totalRev = 0.0
        var cash = 0.0
        var upi = 0.0
        var card = 0.0

        for (order in orders) {
            totalRev += order.grandTotal
            when (order.paymentMode) {
                PaymentMode.CASH -> cash += order.grandTotal
                PaymentMode.UPI -> upi += order.grandTotal
                PaymentMode.CARD -> card += order.grandTotal
            }
        }

        return SalesSummary(
            totalRevenue = totalRev,
            billCount = orders.size,
            cashTotal = cash,
            upiTotal = upi,
            cardTotal = card
        )
    }

    override fun onCleared() {
        super.onCleared()
        printerManager.cleanup()
    }
}
