package com.example.pos.model

import com.example.pos.printer.EscPosCommands
import org.json.JSONObject

enum class ReceiptAlignment(val displayName: String) {
    LEFT("Left"),
    CENTER("Center"),
    RIGHT("Right");

    fun toEscPos(): ByteArray = when (this) {
        LEFT -> EscPosCommands.ALIGN_LEFT
        CENTER -> EscPosCommands.ALIGN_CENTER
        RIGHT -> EscPosCommands.ALIGN_RIGHT
    }
}

enum class ReceiptFontSize(val displayName: String) {
    NORMAL("Normal"),
    DOUBLE_HEIGHT("Tall"),
    DOUBLE_WIDTH("Wide"),
    DOUBLE_BOTH("Large");

    fun toEscPos(): ByteArray = when (this) {
        NORMAL -> EscPosCommands.TEXT_NORMAL
        DOUBLE_HEIGHT -> EscPosCommands.TEXT_DOUBLE_HEIGHT
        DOUBLE_WIDTH -> EscPosCommands.TEXT_DOUBLE_WIDTH
        DOUBLE_BOTH -> EscPosCommands.TEXT_DOUBLE_BOTH
    }
}

/**
 * Configuration for a single receipt printable text element.
 */
data class ElementConfig(
    val visible: Boolean = true,
    val bold: Boolean = false,
    val fontSize: ReceiptFontSize = ReceiptFontSize.NORMAL,
    val alignment: ReceiptAlignment = ReceiptAlignment.LEFT
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("visible", visible)
        put("bold", bold)
        put("fontSize", fontSize.name)
        put("alignment", alignment.name)
    }

    companion object {
        fun fromJson(obj: JSONObject?, default: ElementConfig): ElementConfig {
            if (obj == null) return default
            val vis = obj.optBoolean("visible", default.visible)
            val bld = obj.optBoolean("bold", default.bold)
            val fSize = try {
                ReceiptFontSize.valueOf(obj.optString("fontSize", default.fontSize.name))
            } catch (e: Exception) {
                default.fontSize
            }
            val align = try {
                ReceiptAlignment.valueOf(obj.optString("alignment", default.alignment.name))
            } catch (e: Exception) {
                default.alignment
            }
            return ElementConfig(visible = vis, bold = bld, fontSize = fSize, alignment = align)
        }
    }
}

/**
 * Column configuration for items table columns (QTY, RATE, TOTAL).
 */
data class ItemColumnConfig(
    val visible: Boolean = true,
    val bold: Boolean = false
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("visible", visible)
        put("bold", bold)
    }

    companion object {
        fun fromJson(obj: JSONObject?, default: ItemColumnConfig): ItemColumnConfig {
            if (obj == null) return default
            return ItemColumnConfig(
                visible = obj.optBoolean("visible", default.visible),
                bold = obj.optBoolean("bold", default.bold)
            )
        }
    }
}

/**
 * Complete receipt format configuration supporting all 20 independent receipt elements.
 */
data class ReceiptFormatConfig(
    // 1. Logo
    val logoVisible: Boolean = true,

    // 2. Shop Name
    val shopName: ElementConfig = ElementConfig(visible = true, bold = true, fontSize = ReceiptFontSize.DOUBLE_BOTH, alignment = ReceiptAlignment.CENTER),

    // 3. Address
    val address: ElementConfig = ElementConfig(visible = true, bold = false, fontSize = ReceiptFontSize.NORMAL, alignment = ReceiptAlignment.CENTER),

    // 4. Phone Number
    val phone: ElementConfig = ElementConfig(visible = true, bold = false, fontSize = ReceiptFontSize.NORMAL, alignment = ReceiptAlignment.CENTER),

    // 5. Bill Number
    val billNumber: ElementConfig = ElementConfig(visible = true, bold = true, fontSize = ReceiptFontSize.NORMAL, alignment = ReceiptAlignment.LEFT),

    // 6. Token Number
    val tokenNumber: ElementConfig = ElementConfig(visible = true, bold = true, fontSize = ReceiptFontSize.NORMAL, alignment = ReceiptAlignment.RIGHT),

    // 7. Date
    val date: ElementConfig = ElementConfig(visible = true, bold = false, fontSize = ReceiptFontSize.NORMAL, alignment = ReceiptAlignment.LEFT),

    // 8. Time
    val time: ElementConfig = ElementConfig(visible = true, bold = false, fontSize = ReceiptFontSize.NORMAL, alignment = ReceiptAlignment.RIGHT),

    // 9. Order Type (DINE IN / PARCEL)
    val orderType: ElementConfig = ElementConfig(visible = true, bold = true, fontSize = ReceiptFontSize.NORMAL, alignment = ReceiptAlignment.LEFT),

    // 10. Table Number
    val tableNumber: ElementConfig = ElementConfig(visible = true, bold = true, fontSize = ReceiptFontSize.NORMAL, alignment = ReceiptAlignment.LEFT),

    // 11. Customer Name
    val customerName: ElementConfig = ElementConfig(visible = true, bold = false, fontSize = ReceiptFontSize.NORMAL, alignment = ReceiptAlignment.LEFT),

    // 12. ITEM heading
    val itemHeading: ElementConfig = ElementConfig(visible = true, bold = true, fontSize = ReceiptFontSize.NORMAL, alignment = ReceiptAlignment.LEFT),

    // 13. Item Name
    val itemName: ElementConfig = ElementConfig(visible = true, bold = false, fontSize = ReceiptFontSize.NORMAL, alignment = ReceiptAlignment.LEFT),

    // 14. QTY column
    val colQty: ItemColumnConfig = ItemColumnConfig(visible = true, bold = false),

    // 15. RATE column
    val colRate: ItemColumnConfig = ItemColumnConfig(visible = true, bold = false),

    // 16. TOTAL column
    val colTotal: ItemColumnConfig = ItemColumnConfig(visible = true, bold = false),

    // 17. Grand Total
    val grandTotal: ElementConfig = ElementConfig(visible = true, bold = true, fontSize = ReceiptFontSize.DOUBLE_BOTH, alignment = ReceiptAlignment.RIGHT),

    // 18. Payment Mode
    val paymentMode: ElementConfig = ElementConfig(visible = true, bold = false, fontSize = ReceiptFontSize.NORMAL, alignment = ReceiptAlignment.LEFT),

    // 19. Footer / Thank-you Message
    val footerMessage: ElementConfig = ElementConfig(visible = true, bold = false, fontSize = ReceiptFontSize.NORMAL, alignment = ReceiptAlignment.CENTER),

    // 20. Separator Lines
    val separatorLinesVisible: Boolean = true,
    val separatorChar: Char = '-',

    // Auto Cut paper after receipt
    val autoCut: Boolean = true
) {
    fun toJsonString(): String {
        val root = JSONObject()
        root.put("logoVisible", logoVisible)
        root.put("shopName", shopName.toJson())
        root.put("address", address.toJson())
        root.put("phone", phone.toJson())
        root.put("billNumber", billNumber.toJson())
        root.put("tokenNumber", tokenNumber.toJson())
        root.put("date", date.toJson())
        root.put("time", time.toJson())
        root.put("orderType", orderType.toJson())
        root.put("tableNumber", tableNumber.toJson())
        root.put("customerName", customerName.toJson())
        root.put("itemHeading", itemHeading.toJson())
        root.put("itemName", itemName.toJson())
        root.put("colQty", colQty.toJson())
        root.put("colRate", colRate.toJson())
        root.put("colTotal", colTotal.toJson())
        root.put("grandTotal", grandTotal.toJson())
        root.put("paymentMode", paymentMode.toJson())
        root.put("footerMessage", footerMessage.toJson())
        root.put("separatorLinesVisible", separatorLinesVisible)
        root.put("separatorChar", separatorChar.toString())
        root.put("autoCut", autoCut)
        return root.toString()
    }

    companion object {
        fun fromJsonString(jsonStr: String?): ReceiptFormatConfig {
            if (jsonStr.isNullOrBlank()) return ReceiptFormatConfig()
            return try {
                val root = JSONObject(jsonStr)
                val def = ReceiptFormatConfig()
                ReceiptFormatConfig(
                    logoVisible = root.optBoolean("logoVisible", def.logoVisible),
                    shopName = ElementConfig.fromJson(root.optJSONObject("shopName"), def.shopName),
                    address = ElementConfig.fromJson(root.optJSONObject("address"), def.address),
                    phone = ElementConfig.fromJson(root.optJSONObject("phone"), def.phone),
                    billNumber = ElementConfig.fromJson(root.optJSONObject("billNumber"), def.billNumber),
                    tokenNumber = ElementConfig.fromJson(root.optJSONObject("tokenNumber"), def.tokenNumber),
                    date = ElementConfig.fromJson(root.optJSONObject("date"), def.date),
                    time = ElementConfig.fromJson(root.optJSONObject("time"), def.time),
                    orderType = ElementConfig.fromJson(root.optJSONObject("orderType"), def.orderType),
                    tableNumber = ElementConfig.fromJson(root.optJSONObject("tableNumber"), def.tableNumber),
                    customerName = ElementConfig.fromJson(root.optJSONObject("customerName"), def.customerName),
                    itemHeading = ElementConfig.fromJson(root.optJSONObject("itemHeading"), def.itemHeading),
                    itemName = ElementConfig.fromJson(root.optJSONObject("itemName"), def.itemName),
                    colQty = ItemColumnConfig.fromJson(root.optJSONObject("colQty"), def.colQty),
                    colRate = ItemColumnConfig.fromJson(root.optJSONObject("colRate"), def.colRate),
                    colTotal = ItemColumnConfig.fromJson(root.optJSONObject("colTotal"), def.colTotal),
                    grandTotal = ElementConfig.fromJson(root.optJSONObject("grandTotal"), def.grandTotal),
                    paymentMode = ElementConfig.fromJson(root.optJSONObject("paymentMode"), def.paymentMode),
                    footerMessage = ElementConfig.fromJson(root.optJSONObject("footerMessage"), def.footerMessage),
                    separatorLinesVisible = root.optBoolean("separatorLinesVisible", def.separatorLinesVisible),
                    separatorChar = root.optString("separatorChar", "-").firstOrNull() ?: '-',
                    autoCut = root.optBoolean("autoCut", def.autoCut)
                )
            } catch (e: Exception) {
                ReceiptFormatConfig()
            }
        }
    }
}
