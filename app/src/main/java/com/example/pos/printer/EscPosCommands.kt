package com.example.pos.printer

import android.graphics.Bitmap
import android.graphics.Color
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

object EscPosCommands {
    val INIT = byteArrayOf(0x1B, 0x40)
    val ALIGN_LEFT = byteArrayOf(0x1B, 0x61, 0x00)
    val ALIGN_CENTER = byteArrayOf(0x1B, 0x61, 0x01)
    val ALIGN_RIGHT = byteArrayOf(0x1B, 0x61, 0x02)

    val BOLD_ON = byteArrayOf(0x1B, 0x45, 0x01)
    val BOLD_OFF = byteArrayOf(0x1B, 0x45, 0x00)

    val TEXT_NORMAL = byteArrayOf(0x1D, 0x21, 0x00)
    val TEXT_DOUBLE_HEIGHT = byteArrayOf(0x1D, 0x21, 0x01)
    val TEXT_DOUBLE_WIDTH = byteArrayOf(0x1D, 0x21, 0x10)
    val TEXT_DOUBLE_BOTH = byteArrayOf(0x1D, 0x21, 0x11)
    val TEXT_TRIPLE_HEIGHT = byteArrayOf(0x1D, 0x21, 0x02)
    val TEXT_TRIPLE_BOTH = byteArrayOf(0x1D, 0x21, 0x22)

    val PAPER_CUT = byteArrayOf(0x1D, 0x56, 0x42, 0x00) // GS V B 0
    val LINE_FEED = byteArrayOf(0x0A)

    fun feedLines(n: Int): ByteArray {
        val count = n.coerceIn(1, 20).toByte()
        return byteArrayOf(0x1B, 0x64, count)
    }

    /**
     * Creates a repeated separator line matching the printer's column width.
     */
    fun separatorLine(columns: Int, char: Char = '-'): ByteArray {
        val line = char.toString().repeat(columns) + "\n"
        return line.toByteArray(Charset.forName("ISO-8859-1"))
    }

    /**
     * Formats 2 columns: Left text and Right text aligned.
     */
    fun formatTwoColumns(left: String, right: String, totalColumns: Int): String {
        val available = totalColumns - right.length
        if (available <= 0) {
            return (left + " " + right).take(totalColumns) + "\n"
        }
        val paddedLeft = if (left.length > available) {
            left.take(available - 1) + " "
        } else {
            left.padEnd(available, ' ')
        }
        return paddedLeft + right + "\n"
    }

    /**
     * Formats 4 columns for Bill Item: Name, Qty, Rate, Total
     * Standard layout:
     * 58mm (32 cols): Name (14), Qty (4), Rate (6), Total (8)
     * 80mm (48 cols): Name (24), Qty (6), Rate (8), Total (10)
     */
    fun formatItemRow(name: String, qty: String, rate: String, total: String, totalColumns: Int): String {
        val (nameWidth, qtyWidth, rateWidth, totalWidth) = if (totalColumns <= 32) {
            listOf(14, 4, 6, 8)
        } else {
            listOf(24, 6, 8, 10)
        }

        val paddedQty = qty.padStart(qtyWidth, ' ')
        val paddedRate = rate.padStart(rateWidth, ' ')
        val paddedTotal = total.padStart(totalWidth, ' ')

        return if (name.length <= nameWidth) {
            name.padEnd(nameWidth, ' ') + paddedQty + paddedRate + paddedTotal + "\n"
        } else {
            // Multiline wrapping for long item names
            val lines = name.chunked(nameWidth)
            val firstLine = lines[0].padEnd(nameWidth, ' ') + paddedQty + paddedRate + paddedTotal + "\n"
            val restLines = lines.drop(1).joinToString("") { it.padEnd(nameWidth, ' ') + "\n" }
            firstLine + restLines
        }
    }

    /**
     * Formats item row with dynamically enabled/disabled columns and word wrapping.
     */
    fun formatConfigurableItemRow(
        name: String,
        qty: String,
        rate: String,
        total: String,
        showQty: Boolean,
        showRate: Boolean,
        showTotal: Boolean,
        totalColumns: Int
    ): String {
        val qtyWidth = if (showQty) (if (totalColumns <= 32) 4 else 6) else 0
        val rateWidth = if (showRate) (if (totalColumns <= 32) 6 else 8) else 0
        val totalWidth = if (showTotal) (if (totalColumns <= 32) 8 else 10) else 0
        val numericWidth = qtyWidth + rateWidth + totalWidth
        val nameWidth = (totalColumns - numericWidth).coerceAtLeast(8)

        val paddedQty = if (showQty) qty.padStart(qtyWidth, ' ') else ""
        val paddedRate = if (showRate) rate.padStart(rateWidth, ' ') else ""
        val paddedTotal = if (showTotal) total.padStart(totalWidth, ' ') else ""
        val rightCols = paddedQty + paddedRate + paddedTotal

        val emptyRightPadding = " ".repeat(numericWidth)

        return if (name.length <= nameWidth) {
            name.padEnd(nameWidth, ' ') + rightCols + "\n"
        } else {
            val lines = name.chunked(nameWidth)
            val firstLine = lines[0].padEnd(nameWidth, ' ') + rightCols + "\n"
            val restLines = lines.drop(1).joinToString("") {
                it.padEnd(nameWidth, ' ') + emptyRightPadding + "\n"
            }
            firstLine + restLines
        }
    }

    /**
     * Formats KOT row: Item Name and Quantity (bold and prominent)
     */
    fun formatKotRow(name: String, qty: String, totalColumns: Int): String {
        val qtyStr = "x$qty"
        val available = totalColumns - qtyStr.length - 1
        val paddedName = if (name.length > available) {
            name.take(available)
        } else {
            name.padEnd(available, ' ')
        }
        return "$paddedName $qtyStr\n"
    }

    /**
     * Converts an Android Bitmap into standard monochrome ESC/POS raster image command
     * Command: GS v 0 m xL xH yL yH d1...dk
     */
    fun bitmapToEscPosRaster(originalBitmap: Bitmap, maxPixelWidth: Int = 384): ByteArray {
        val stream = ByteArrayOutputStream()

        // Scale bitmap to fit printer width while maintaining aspect ratio
        val scaledBitmap = if (originalBitmap.width > maxPixelWidth) {
            val scale = maxPixelWidth.toFloat() / originalBitmap.width
            val targetHeight = (originalBitmap.height * scale).toInt().coerceAtLeast(1)
            Bitmap.createScaledBitmap(originalBitmap, maxPixelWidth, targetHeight, true)
        } else {
            originalBitmap
        }

        val width = scaledBitmap.width
        val height = scaledBitmap.height

        // Width in bytes (8 pixels per byte)
        val widthBytes = (width + 7) / 8
        val xL = (widthBytes % 256).toByte()
        val xH = (widthBytes / 256).toByte()
        val yL = (height % 256).toByte()
        val yH = (height / 256).toByte()

        // Header for GS v 0 0 xL xH yL yH
        stream.write(byteArrayOf(0x1D, 0x76, 0x30, 0x00, xL, xH, yL, yH))

        // Dithering / Thresholding conversion
        // Pre-calculate luminance array
        val gray = Array(height) { IntArray(width) }
        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = scaledBitmap.getPixel(x, y)
                val alpha = Color.alpha(pixel)
                if (alpha < 128) {
                    gray[y][x] = 255 // White background for transparent pixels
                } else {
                    val r = Color.red(pixel)
                    val g = Color.green(pixel)
                    val b = Color.blue(pixel)
                    gray[y][x] = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                }
            }
        }

        // Generate raster bytes (1 = black/dot printed, 0 = white)
        val threshold = 160
        for (y in 0 until height) {
            for (byteX in 0 until widthBytes) {
                var currentByte = 0
                for (bit in 0 until 8) {
                    val pixelX = byteX * 8 + bit
                    if (pixelX < width) {
                        if (gray[y][pixelX] < threshold) {
                            currentByte = currentByte or (1 shl (7 - bit))
                        }
                    }
                }
                stream.write(currentByte)
            }
        }

        return stream.toByteArray()
    }
}
