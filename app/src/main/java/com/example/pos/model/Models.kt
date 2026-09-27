package com.example.pos.model

enum class OrderType {
    DINE_IN,
    PARCEL
}

enum class PaymentMode {
    CASH,
    UPI,
    CARD
}

enum class PrinterPaperWidth(val widthMm: Int, val maxColumns: Int) {
    WIDTH_58MM(58, 32),
    WIDTH_80MM(80, 48)
}

enum class ItemFontSize {
    NORMAL,
    LARGE
}

data class MenuItem(
    val id: Long = 0,
    val name: String,
    val category: String,
    val price: Double,
    val isAvailable: Boolean = true,
    val sortOrder: Int = 0
)

data class CartItem(
    val menuItem: MenuItem,
    var quantity: Int
) {
    val total: Double get() = menuItem.price * quantity
}

data class BluetoothDeviceInfo(
    val name: String,
    val address: String,
    val isBonded: Boolean = false
)

sealed interface PrinterConnectionState {
    data object Disconnected : PrinterConnectionState
    data object Connecting : PrinterConnectionState
    data class Connected(val deviceName: String, val address: String) : PrinterConnectionState
    data class Error(val message: String) : PrinterConnectionState
}

data class ShopSettings(
    val shopName: String = "RK TIFFINES",
    val address: String = "Kadapa, Andhra Pradesh, India",
    val phone: String = "9392509555",
    val logoPath: String? = null,
    val paperWidth: PrinterPaperWidth = PrinterPaperWidth.WIDTH_80MM,
    val billItemFontSize: ItemFontSize = ItemFontSize.NORMAL,
    val kotItemFontSize: ItemFontSize = ItemFontSize.LARGE,
    val autoPrintBill: Boolean = true,
    val autoPrintToken: Boolean = false,
    val autoPrintBoth: Boolean = false,
    val autoCutPaper: Boolean = true,
    val nextBillNumber: Long = 1001L,
    val nextTokenNumber: Int = 1,
    val savedPrinterMac: String = "",
    val savedPrinterName: String = "",
    val autoReconnectPrinter: Boolean = true,
    val receiptFooter: String = "Thank you! Visit again",
    val showCustomerName: Boolean = true,
    val showTableNumber: Boolean = true,
    val showLogo: Boolean = true,
    val receiptFormat: ReceiptFormatConfig = ReceiptFormatConfig()
)

data class BillRecord(
    val billId: String,
    val billNumber: Long,
    val tokenNumber: Int,
    val timestamp: Long,
    val dateString: String,
    val timeString: String,
    val orderType: OrderType,
    val tableNumber: String?,
    val customerName: String?,
    val paymentMode: PaymentMode,
    val items: List<CartItem>,
    val subtotal: Double,
    val grandTotal: Double,
    val itemCount: Int,
    val isSynced: Boolean = false
)
