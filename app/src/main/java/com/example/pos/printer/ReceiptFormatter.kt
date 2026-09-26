package com.example.pos.printer

import android.graphics.Bitmap
import com.example.pos.model.BillRecord
import com.example.pos.model.ItemFontSize
import com.example.pos.model.OrderType
import com.example.pos.model.PrinterPaperWidth
import com.example.pos.model.ShopSettings
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

object ReceiptFormatter {

    fun buildCustomerReceipt(
        bill: BillRecord,
        settings: ShopSettings,
        logoBitmap: Bitmap? = null
    ): ByteArray {
        val stream = ByteArrayOutputStream()
        val charset = Charset.forName("ISO-8859-1")
        val cols = settings.paperWidth.maxColumns

        // 1. Initialize
        stream.write(EscPosCommands.INIT)

        // 2. Logo (if enabled and present)
        if (settings.showLogo && logoBitmap != null) {
            val maxPixels = if (settings.paperWidth == PrinterPaperWidth.WIDTH_58MM) 384 else 576
            val rasterLogo = EscPosCommands.bitmapToEscPosRaster(logoBitmap, maxPixels)
            stream.write(EscPosCommands.ALIGN_CENTER)
            stream.write(rasterLogo)
            stream.write(EscPosCommands.feedLines(1))
        }

        // 3. Header: Shop Name (Bold, Double height & width)
        stream.write(EscPosCommands.ALIGN_CENTER)
        stream.write(EscPosCommands.BOLD_ON)
        stream.write(EscPosCommands.TEXT_DOUBLE_BOTH)
        stream.write("${settings.shopName}\n".toByteArray(charset))
        stream.write(EscPosCommands.TEXT_NORMAL)
        stream.write(EscPosCommands.BOLD_OFF)

        // 4. Address & Phone
        if (settings.address.isNotBlank()) {
            stream.write("${settings.address}\n".toByteArray(charset))
        }
        if (settings.phone.isNotBlank()) {
            stream.write("Ph: ${settings.phone}\n".toByteArray(charset))
        }
        stream.write(EscPosCommands.feedLines(1))

        // 5. Bill Info: Bill Number, Token, Date/Time, Order Type
        stream.write(EscPosCommands.ALIGN_LEFT)
        val billTokenLine = EscPosCommands.formatTwoColumns(
            left = "Bill No: #${bill.billNumber}",
            right = "Token: #${bill.tokenNumber}",
            totalColumns = cols
        )
        stream.write(billTokenLine.toByteArray(charset))

        val dateTimeLine = EscPosCommands.formatTwoColumns(
            left = "Date: ${bill.dateString}",
            right = "Time: ${bill.timeString}",
            totalColumns = cols
        )
        stream.write(dateTimeLine.toByteArray(charset))

        val orderTypeStr = when (bill.orderType) {
            OrderType.DINE_IN -> {
                if (settings.showTableNumber && !bill.tableNumber.isNullOrBlank()) {
                    "TYPE: DINE IN (Table: ${bill.tableNumber})"
                } else {
                    "TYPE: DINE IN"
                }
            }
            OrderType.PARCEL -> "TYPE: PARCEL (Takeaway)"
        }
        stream.write(EscPosCommands.BOLD_ON)
        stream.write("$orderTypeStr\n".toByteArray(charset))
        stream.write(EscPosCommands.BOLD_OFF)

        if (settings.showCustomerName && !bill.customerName.isNullOrBlank()) {
            stream.write("Customer: ${bill.customerName}\n".toByteArray(charset))
        }

        // 6. Separator line before items
        stream.write(EscPosCommands.separatorLine(cols))

        // 7. ITEM Headings: ITEM, QTY, RATE, TOTAL
        val headingRow = EscPosCommands.formatItemRow(
            name = "ITEM",
            qty = "QTY",
            rate = "RATE",
            total = "TOTAL",
            totalColumns = cols
        )
        stream.write(EscPosCommands.BOLD_ON)
        stream.write(headingRow.toByteArray(charset))
        stream.write(EscPosCommands.BOLD_OFF)
        stream.write(EscPosCommands.separatorLine(cols, '-'))

        // 8. Items
        for (item in bill.items) {
            val name = item.menuItem.name
            val qty = item.quantity.toString()
            val rate = "%.0f".format(item.menuItem.price)
            val total = "%.0f".format(item.total)

            if (settings.billItemFontSize == ItemFontSize.LARGE) {
                // Item name double height for prominence
                stream.write(EscPosCommands.TEXT_DOUBLE_HEIGHT)
                stream.write(EscPosCommands.BOLD_ON)
                stream.write("$name\n".toByteArray(charset))
                stream.write(EscPosCommands.TEXT_NORMAL)
                stream.write(EscPosCommands.BOLD_OFF)

                // Sub-row with qty, rate, total aligned to right
                val qtyRateTotal = "  $qty x $rate = $total"
                val alignedLine = EscPosCommands.formatTwoColumns(
                    left = "",
                    right = qtyRateTotal,
                    totalColumns = cols
                )
                stream.write(alignedLine.toByteArray(charset))
            } else {
                // Standard 4-column row
                val row = EscPosCommands.formatItemRow(name, qty, rate, total, cols)
                stream.write(row.toByteArray(charset))
            }
        }

        // 9. Full-width dashed separator between last item and GRAND TOTAL (mandated by Section 9)
        stream.write(EscPosCommands.separatorLine(cols, '-'))

        // 10. GRAND TOTAL
        stream.write(EscPosCommands.ALIGN_RIGHT)
        stream.write(EscPosCommands.BOLD_ON)
        stream.write(EscPosCommands.TEXT_DOUBLE_BOTH)
        val grandTotalStr = "TOTAL: Rs. %.0f".format(bill.grandTotal)
        stream.write("$grandTotalStr\n".toByteArray(charset))
        stream.write(EscPosCommands.TEXT_NORMAL)

        // 11. Payment Mode & Total Items
        stream.write(EscPosCommands.ALIGN_LEFT)
        val summaryLine = EscPosCommands.formatTwoColumns(
            left = "Items: ${bill.itemCount}",
            right = "Mode: ${bill.paymentMode.name}",
            totalColumns = cols
        )
        stream.write(summaryLine.toByteArray(charset))
        stream.write(EscPosCommands.BOLD_OFF)

        // 12. Footer / Thank You Message
        if (settings.receiptFooter.isNotBlank()) {
            stream.write(EscPosCommands.ALIGN_CENTER)
            stream.write("${settings.receiptFooter}\n".toByteArray(charset))
        }

        // 13. Bottom separator after thank-you message (mandated by Section 9)
        stream.write(EscPosCommands.separatorLine(cols, '-'))

        // 14. Feed lines & Cut
        stream.write(EscPosCommands.feedLines(4))
        stream.write(EscPosCommands.PAPER_CUT)

        return stream.toByteArray()
    }
}
