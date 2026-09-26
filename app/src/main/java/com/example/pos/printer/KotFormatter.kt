package com.example.pos.printer

import com.example.pos.model.BillRecord
import com.example.pos.model.ItemFontSize
import com.example.pos.model.OrderType
import com.example.pos.model.ShopSettings
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

object KotFormatter {

    fun buildKitchenTicket(
        bill: BillRecord,
        settings: ShopSettings
    ): ByteArray {
        val stream = ByteArrayOutputStream()
        val charset = Charset.forName("ISO-8859-1")
        val cols = settings.paperWidth.maxColumns

        // 1. Initialize
        stream.write(EscPosCommands.INIT)

        // 2. KOT Header
        stream.write(EscPosCommands.ALIGN_CENTER)
        stream.write(EscPosCommands.BOLD_ON)
        stream.write("*** KITCHEN ORDER TICKET ***\n".toByteArray(charset))
        stream.write(EscPosCommands.TEXT_DOUBLE_HEIGHT)
        stream.write("${settings.shopName}\n".toByteArray(charset))
        stream.write(EscPosCommands.TEXT_NORMAL)
        stream.write(EscPosCommands.BOLD_OFF)
        stream.write(EscPosCommands.feedLines(1))

        // 3. Huge Token Number
        stream.write(EscPosCommands.ALIGN_CENTER)
        stream.write(EscPosCommands.BOLD_ON)
        stream.write(EscPosCommands.TEXT_TRIPLE_BOTH)
        stream.write("TOKEN #${bill.tokenNumber}\n".toByteArray(charset))
        stream.write(EscPosCommands.TEXT_NORMAL)
        stream.write(EscPosCommands.BOLD_OFF)
        stream.write(EscPosCommands.feedLines(1))

        // 4. Order Type & Table
        stream.write(EscPosCommands.ALIGN_CENTER)
        stream.write(EscPosCommands.BOLD_ON)
        stream.write(EscPosCommands.TEXT_DOUBLE_HEIGHT)
        when (bill.orderType) {
            OrderType.DINE_IN -> {
                val tableInfo = if (!bill.tableNumber.isNullOrBlank()) {
                    "DINE IN - TABLE #${bill.tableNumber}"
                } else {
                    "DINE IN"
                }
                stream.write("$tableInfo\n".toByteArray(charset))
            }
            OrderType.PARCEL -> {
                stream.write("PARCEL / TAKEAWAY\n".toByteArray(charset))
            }
        }
        stream.write(EscPosCommands.TEXT_NORMAL)
        stream.write(EscPosCommands.BOLD_OFF)

        // 5. Date & Time
        stream.write(EscPosCommands.ALIGN_LEFT)
        val infoLine = EscPosCommands.formatTwoColumns(
            left = "Date: ${bill.dateString}",
            right = "Time: ${bill.timeString}",
            totalColumns = cols
        )
        stream.write(infoLine.toByteArray(charset))

        // 6. Separator
        stream.write(EscPosCommands.separatorLine(cols, '='))

        // 7. Column Headings
        stream.write(EscPosCommands.BOLD_ON)
        val heading = EscPosCommands.formatTwoColumns(
            left = "ITEM",
            right = "QTY",
            totalColumns = cols
        )
        stream.write(heading.toByteArray(charset))
        stream.write(EscPosCommands.separatorLine(cols, '-'))
        stream.write(EscPosCommands.BOLD_OFF)

        // 8. Items with configurable KOT item font size (Large or Normal)
        for (item in bill.items) {
            val name = item.menuItem.name
            val qty = item.quantity.toString()

            if (settings.kotItemFontSize == ItemFontSize.LARGE) {
                // Large, bold, easily readable from across the kitchen counter
                stream.write(EscPosCommands.BOLD_ON)
                stream.write(EscPosCommands.TEXT_DOUBLE_HEIGHT)
                val line = EscPosCommands.formatKotRow(name, qty, cols)
                stream.write(line.toByteArray(charset))
                stream.write(EscPosCommands.TEXT_NORMAL)
                stream.write(EscPosCommands.BOLD_OFF)
            } else {
                stream.write(EscPosCommands.BOLD_ON)
                val line = EscPosCommands.formatKotRow(name, qty, cols)
                stream.write(line.toByteArray(charset))
                stream.write(EscPosCommands.BOLD_OFF)
            }
        }

        // 9. Bottom summary
        stream.write(EscPosCommands.separatorLine(cols, '='))
        stream.write(EscPosCommands.ALIGN_RIGHT)
        stream.write(EscPosCommands.BOLD_ON)
        stream.write("Total Items: ${bill.itemCount}\n".toByteArray(charset))
        stream.write(EscPosCommands.BOLD_OFF)

        // 10. Feed & Cut
        stream.write(EscPosCommands.feedLines(4))
        stream.write(EscPosCommands.PAPER_CUT)

        return stream.toByteArray()
    }
}
