package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// =========================================================================
// RK TIFFINES POS BRAND DESIGN SYSTEM
// Inspired directly by the RK Logo: Deep Black + Golden Yellow + Orange + Warm Red
// =========================================================================

// 1. Core Backgrounds & Surfaces (Deep Black & Dark Charcoal)
val RkBlackBackground = Color(0xFF0C0B0A)       // Deep black / near-black main background
val RkSurfaceDark = Color(0xFF161514)           // Dark charcoal card & surface
val RkSurfaceVariantDark = Color(0xFF22201D)    // Slightly lighter charcoal for chips/inputs
val RkSurfaceElevated = Color(0xFF2B2824)       // Dialogs & dropdown menu surface
val RkBorderGoldSubtle = Color(0x38FFB300)      // Subtle warm gold border
val RkBorderWarm = Color(0x28FF8F00)            // Subtle warm border
val RkBorderGoldHighlight = Color(0xFFFFB300)   // Active highlight border

// 2. Primary & Secondary Accents (Golden Yellow, Bright Yellow, Orange, Warm Red)
val RkGoldPrimary = Color(0xFFFFB300)           // Logo Golden Yellow
val RkYellowBright = Color(0xFFFFC107)          // Bright Yellow for totals and key numbers
val RkOrangeSecondary = Color(0xFFFF6D00)        // Vibrant Orange accent
val RkWarmRedTertiary = Color(0xFFE64A19)        // Warm Red/Orange accent
val RkAmber = Color(0xFFFFA000)                 // Warm Amber

// 3. Text & Typography Colors
val RkTextPrimary = Color(0xFFF9F6F0)           // Warm white / off-white primary text
val RkTextSecondary = Color(0xFFB8B0A6)         // Warm light grey secondary text
val RkTextMuted = Color(0xFF6E6760)             // Dark grey / muted text
val RkTextOnGold = Color(0xFF140D00)            // High contrast deep dark text on gold/orange

// 4. Subtle Brand Gradients (GOLD/YELLOW -> ORANGE -> WARM RED)
val RkBrandGradient = Brush.horizontalGradient(
    listOf(
        Color(0xFFFFB300), // Golden Yellow
        Color(0xFFFF6D00), // Orange
        Color(0xFFE64A19)  // Warm Red
    )
)

val RkGoldOrangeGradient = Brush.horizontalGradient(
    listOf(
        Color(0xFFFFC107),
        Color(0xFFFF8F00)
    )
)

// 5. Payment Modes & Status Indicators
val CashGreen = Color(0xFF2E7D32)
val UpiBlue = Color(0xFF1565C0)
val CardPurple = Color(0xFF6A1B9A)
val PrinterConnectedGreen = Color(0xFF4CAF50)
val PrinterConnectingYellow = Color(0xFFFFB300)
val PrinterErrorRed = Color(0xFFE53935)
val LeafGreenDark = Color(0xFF1B5E20)
val LeafGreenTertiary = Color(0xFF2E7D32)
