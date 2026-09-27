package com.example.pos.printer

import android.graphics.Bitmap
import com.example.pos.model.BillRecord
import com.example.pos.model.ElementConfig
import com.example.pos.model.OrderType
import com.example.pos.model.PrinterPaperWidth
import com.example.pos.model.ReceiptAlignment
import com.example.pos.model.ReceiptFontSize
import com.example.pos.model.ShopSettings
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

object ReceiptFormatter {

    /**
     * Builds the complete ESC/POS raw byte stream for the customer bill receipt
     * strictly applying the saved ReceiptFormatConfig across all 20 receipt elements.
     */
    fun buildCustomerReceipt(
        bill: BillRecord,
        settings: ShopSettings,
        logoBitmap: Bitmap? = null
    ): ByteArray {
        val stream = ByteArrayOutputStream()
        val charset = Charset.forName("ISO-8859-1")
        val cols = settings.paperWidth.maxColumns
        val format = settings.receiptFormat

        // 1. Initialize Printer
        stream.write(EscPosCommands.INIT)

        // 2. Logo (Element 1: Logo)
        if (format.logoVisible && settings.showLogo && logoBitmap != null) {
            val maxPixels = if (settings.paperWidth == PrinterPaperWidth.WIDTH_58MM) 384 else 576
            val rasterLogo = EscPosCommands.bitmapToEscPosRaster(logoBitmap, maxPixels)
            stream.write(EscPosCommands.ALIGN_CENTER)
            stream.write(rasterLogo)
            stream.write(EscPosCommands.feedLines(1))
        }

        // 3. Shop Name (Element 2: Shop Name)
        if (format.shopName.visible && settings.shopName.isNotBlank()) {
            writeStyledText(
                stream = stream,
                text = "${settings.shopName}\n",
                config = format.shopName,
                charset = charset
            )
        }

        // 4. Address (Element 3: Address)
        if (format.address.visible && settings.address.isNotBlank()) {
            writeStyledText(
                stream = stream,
                text = "${settings.address}\n",
                config = format.address,
                charset = charset
            )
        }

        // 5. Phone Number (Element 4: Phone Number)
        if (format.phone.visible && settings.phone.isNotBlank()) {
            writeStyledText(
                stream = stream,
                text = "Ph: ${settings.phone}\n",
                config = format.phone,
                charset = charset
            )
        }

        // Optional blank line spacing after shop header
        if (format.shopName.visible || format.address.visible || format.phone.visible) {
            stream.write(EscPosCommands.feedLines(1))
        }

        // 6. Bill Number (Element 5) & Token Number (Element 6)
        val showBill = format.billNumber.visible
        val showToken = format.tokenNumber.visible
        if (showBill && showToken && format.billNumber.alignment == ReceiptAlignment.LEFT && format.tokenNumber.alignment == ReceiptAlignment.RIGHT) {
            stream.write(EscPosCommands.ALIGN_LEFT)
            if (format.billNumber.bold || format.tokenNumber.bold) stream.write(EscPosCommands.BOLD_ON)
            stream.write(format.billNumber.fontSize.toEscPos())
            val line = EscPosCommands.formatTwoColumns(
                left = "Bill No: #${bill.billNumber}",
                right = "Token: #${bill.tokenNumber}",
                totalColumns = cols
            )
            stream.write(line.toByteArray(charset))
            stream.write(EscPosCommands.TEXT_NORMAL)
            stream.write(EscPosCommands.BOLD_OFF)
        } else {
            if (showBill) {
                writeStyledText(
                    stream = stream,
                    text = "Bill No: #${bill.billNumber}\n",
                    config = format.billNumber,
                    charset = charset
                )
            }
            if (showToken) {
                writeStyledText(
                    stream = stream,
                    text = "Token: #${bill.tokenNumber}\n",
                    config = format.tokenNumber,
                    charset = charset
                )
            }
        }

        // 7. Date (Element 7) & Time (Element 8)
        val showDate = format.date.visible
        val showTime = format.time.visible
        if (showDate && showTime && format.date.alignment == ReceiptAlignment.LEFT && format.time.alignment == ReceiptAlignment.RIGHT) {
            stream.write(EscPosCommands.ALIGN_LEFT)
            if (format.date.bold || format.time.bold) stream.write(EscPosCommands.BOLD_ON)
            stream.write(format.date.fontSize.toEscPos())
            val line = EscPosCommands.formatTwoColumns(
                left = "Date: ${bill.dateString}",
                right = "Time: ${bill.timeString}",
                totalColumns = cols
            )
            stream.write(line.toByteArray(charset))
            stream.write(EscPosCommands.TEXT_NORMAL)
            stream.write(EscPosCommands.BOLD_OFF)
        } else {
            if (showDate) {
                writeStyledText(
                    stream = stream,
                    text = "Date: ${bill.dateString}\n",
                    config = format.date,
                    charset = charset
                )
            }
            if (showTime) {
                writeStyledText(
                    stream = stream,
                    text = "Time: ${bill.timeString}\n",
                    config = format.time,
                    charset = charset
                )
            }
        }

        // 8. Order Type (Element 9: DINE IN / PARCEL)
        if (format.orderType.visible) {
            val orderTypeStr = when (bill.orderType) {
                OrderType.DINE_IN -> "TYPE: DINE IN"
                OrderType.PARCEL -> "TYPE: PARCEL (Takeaway)"
            }
            writeStyledText(
                stream = stream,
                text = "$orderTypeStr\n",
                config = format.orderType,
                charset = charset
            )
        }

        // 9. Table Number (Element 10: Table Number)
        if (format.tableNumber.visible && !bill.tableNumber.isNullOrBlank()) {
            writeStyledText(
                stream = stream,
                text = "Table: ${bill.tableNumber}\n",
                config = format.tableNumber,
                charset = charset
            )
        }

        // 10. Customer Name (Element 11: Customer Name)
        if (format.customerName.visible && !bill.customerName.isNullOrBlank()) {
            writeStyledText(
                stream = stream,
                text = "Customer: ${bill.customerName}\n",
                config = format.customerName,
                charset = charset
            )
        }

        // 11. Separator line before items (Element 20: Separator Lines)
        if (format.separatorLinesVisible) {
            stream.write(EscPosCommands.separatorLine(cols, format.separatorChar))
        }

        // 12. ITEM Heading (Element 12: ITEM heading)
        if (format.itemHeading.visible) {
            stream.write(EscPosCommands.ALIGN_LEFT)
            if (format.itemHeading.bold) stream.write(EscPosCommands.BOLD_ON)
            stream.write(format.itemHeading.fontSize.toEscPos())

            val headingRow = EscPosCommands.formatConfigurableItemRow(
                name = "ITEM",
                qty = "QTY",
                rate = "RATE",
                total = "TOTAL",
                showQty = format.colQty.visible,
                showRate = format.colRate.visible,
                showTotal = format.colTotal.visible,
                totalColumns = cols
            )
            stream.write(headingRow.toByteArray(charset))
            stream.write(EscPosCommands.TEXT_NORMAL)
            stream.write(EscPosCommands.BOLD_OFF)

            if (format.separatorLinesVisible) {
                stream.write(EscPosCommands.separatorLine(cols, format.separatorChar))
            }
        }

        // 13. Items (Element 13: Item Name, 14: QTY, 15: RATE, 16: TOTAL)
        for (item in bill.items) {
            val name = item.menuItem.name
            val qty = item.quantity.toString()
            val rate = "%.0f".format(item.menuItem.price)
            val total = "%.0f".format(item.total)

            if (format.itemName.fontSize != ReceiptFontSize.NORMAL) {
                // Item name rendered with special font size on separate row
                stream.write(format.itemName.alignment.toEscPos())
                if (format.itemName.bold) stream.write(EscPosCommands.BOLD_ON)
                stream.write(format.itemName.fontSize.toEscPos())
                stream.write("$name\n".toByteArray(charset))
                stream.write(EscPosCommands.TEXT_NORMAL)
                stream.write(EscPosCommands.BOLD_OFF)

                // Sub-row containing aligned numeric details
                val numericDetails = buildString {
                    if (format.colQty.visible) append("x$qty ")
                    if (format.colRate.visible) append("@Rs.$rate ")
                    if (format.colTotal.visible) append("= Rs.$total")
                }.trim()
                if (numericDetails.isNotEmpty()) {
                    val line = EscPosCommands.formatTwoColumns("", numericDetails, cols)
                    stream.write(line.toByteArray(charset))
                }
            } else {
                stream.write(EscPosCommands.ALIGN_LEFT)
                if (format.itemName.bold) stream.write(EscPosCommands.BOLD_ON)
                stream.write(EscPosCommands.TEXT_NORMAL)

                val row = EscPosCommands.formatConfigurableItemRow(
                    name = name,
                    qty = qty,
                    rate = rate,
                    total = total,
                    showQty = format.colQty.visible,
                    showRate = format.colRate.visible,
                    showTotal = format.colTotal.visible,
                    totalColumns = cols
                )
                stream.write(row.toByteArray(charset))
                stream.write(EscPosCommands.BOLD_OFF)
            }
        }

        // 14. Full-width separator before Grand Total (Element 20)
        if (format.separatorLinesVisible) {
            stream.write(EscPosCommands.separatorLine(cols, format.separatorChar))
        }

        // 15. Grand Total (Element 17: Grand Total)
        if (format.grandTotal.visible) {
            stream.write(format.grandTotal.alignment.toEscPos())
            if (format.grandTotal.bold) stream.write(EscPosCommands.BOLD_ON)
            stream.write(format.grandTotal.fontSize.toEscPos())
            val grandTotalStr = "TOTAL: Rs. %.0f".format(bill.grandTotal)
            stream.write("$grandTotalStr\n".toByteArray(charset))
            stream.write(EscPosCommands.TEXT_NORMAL)
            stream.write(EscPosCommands.BOLD_OFF)
        }

        // 16. Payment Mode (Element 18: Payment Mode)
        if (format.paymentMode.visible) {
            val paymentLine = if (format.paymentMode.alignment == ReceiptAlignment.LEFT) {
                EscPosCommands.formatTwoColumns(
                    left = "Payment: ${bill.paymentMode.name}",
                    right = "Items: ${bill.itemCount}",
                    totalColumns = cols
                )
            } else {
                "Payment: ${bill.paymentMode.name} (Items: ${bill.itemCount})\n"
            }
            writeStyledText(
                stream = stream,
                text = paymentLine,
                config = format.paymentMode,
                charset = charset
            )
        }

        // 17. Footer / Thank-you Message (Element 19: Footer / Thank-you Message)
        if (format.footerMessage.visible && settings.receiptFooter.isNotBlank()) {
            writeStyledText(
                stream = stream,
                text = "${settings.receiptFooter}\n",
                config = format.footerMessage,
                charset = charset
            )
        }

        // 18. Bottom separator after footer message (Element 20: Separator Lines)
        if (format.separatorLinesVisible) {
            stream.write(EscPosCommands.separatorLine(cols, format.separatorChar))
        }

        // 19. Feed lines and optional Auto Cut paper
        stream.write(EscPosCommands.feedLines(4))
        if (settings.autoCutPaper && settings.receiptFormat.autoCut) {
            stream.write(EscPosCommands.PAPER_CUT)
        }

        return stream.toByteArray()
    }

    private fun writeStyledText(
        stream: ByteArrayOutputStream,
        text: String,
        config: ElementConfig,
        charset: Charset
    ) {
        if (!config.visible || text.isBlank()) return
        stream.write(config.alignment.toEscPos())
        if (config.bold) stream.write(EscPosCommands.BOLD_ON) else stream.write(EscPosCommands.BOLD_OFF)
        stream.write(config.fontSize.toEscPos())
        stream.write(text.toByteArray(charset))
        stream.write(EscPosCommands.TEXT_NORMAL)
        stream.write(EscPosCommands.BOLD_OFF)
        stream.write(EscPosCommands.ALIGN_LEFT)
    }
}
