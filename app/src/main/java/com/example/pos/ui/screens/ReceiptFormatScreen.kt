package com.example.pos.ui.screens

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pos.model.ElementConfig
import com.example.pos.model.ItemColumnConfig
import com.example.pos.model.PrinterConnectionState
import com.example.pos.model.PrinterPaperWidth
import com.example.pos.model.ReceiptAlignment
import com.example.pos.model.ReceiptFontSize
import com.example.pos.model.ReceiptFormatConfig
import com.example.pos.model.ShopSettings
import com.example.pos.ui.PosViewModel
import com.example.ui.theme.PrinterConnectedGreen
import com.example.ui.theme.PrinterConnectingYellow
import com.example.ui.theme.PrinterErrorRed
import com.example.ui.theme.RkBlackBackground
import com.example.ui.theme.RkBorderGoldSubtle
import com.example.ui.theme.RkGoldPrimary
import com.example.ui.theme.RkOrangeSecondary
import com.example.ui.theme.RkSurfaceDark
import com.example.ui.theme.RkSurfaceVariantDark
import com.example.ui.theme.RkTextMuted
import com.example.ui.theme.RkTextOnGold
import com.example.ui.theme.RkTextPrimary
import com.example.ui.theme.RkTextSecondary
import com.example.ui.theme.RkYellowBright
import java.io.InputStream

@Composable
fun ReceiptFormatScreen(
    viewModel: PosViewModel,
    settings: ShopSettings,
    printerState: PrinterConnectionState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var formatState by remember(settings.receiptFormat) { mutableStateOf(settings.receiptFormat) }
    var paperWidth by remember(settings.paperWidth) { mutableStateOf(settings.paperWidth) }

    // Logo image picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    viewModel.saveUploadedLogo(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val logoBitmap = remember(settings.logoPath) { viewModel.repository.getLogoBitmap() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Top Action & Status Card ---
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = RkSurfaceDark),
            border = BorderStroke(1.dp, RkBorderGoldSubtle)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Thermal Receipt Format",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = RkTextPrimary)
                        )
                        Text(
                            text = "Configure all 20 printed receipt elements directly via ESC/POS",
                            style = MaterialTheme.typography.bodySmall,
                            color = RkTextSecondary
                        )
                    }

                    // Printer status indicator
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = when (printerState) {
                            is PrinterConnectionState.Connected -> PrinterConnectedGreen.copy(alpha = 0.2f)
                            is PrinterConnectionState.Connecting -> PrinterConnectingYellow.copy(alpha = 0.2f)
                            else -> PrinterErrorRed.copy(alpha = 0.2f)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (printerState) {
                                            is PrinterConnectionState.Connected -> PrinterConnectedGreen
                                            is PrinterConnectionState.Connecting -> PrinterConnectingYellow
                                            else -> PrinterErrorRed
                                        }
                                    )
                            )
                            Text(
                                text = when (printerState) {
                                    is PrinterConnectionState.Connected -> "POS-8380 Ready"
                                    is PrinterConnectionState.Connecting -> "Connecting..."
                                    else -> "Printer Offline"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                // Action Buttons: TEST PRINT & SAVE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.updateReceiptFormat(formatState)
                            viewModel.updatePaperWidth(paperWidth)
                            viewModel.testPrintCurrentFormat()
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RkGoldPrimary, contentColor = RkTextOnGold),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, tint = RkTextOnGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Test Print on POS-8380", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RkTextOnGold)
                    }

                    Button(
                        onClick = {
                            viewModel.updateReceiptFormat(formatState)
                            viewModel.updatePaperWidth(paperWidth)
                        },
                        modifier = Modifier
                            .weight(0.8f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RkOrangeSecondary, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Format", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                    }
                }

                // Paper Width Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Paper Width:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = RkTextPrimary))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = paperWidth == PrinterPaperWidth.WIDTH_80MM,
                            onClick = {
                                paperWidth = PrinterPaperWidth.WIDTH_80MM
                                viewModel.updatePaperWidth(PrinterPaperWidth.WIDTH_80MM)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RkGoldPrimary,
                                selectedLabelColor = RkTextOnGold,
                                containerColor = RkSurfaceVariantDark,
                                labelColor = RkTextSecondary
                            ),
                            label = { Text("80mm (POS-8380)", fontWeight = FontWeight.Bold) }
                        )
                        FilterChip(
                            selected = paperWidth == PrinterPaperWidth.WIDTH_58MM,
                            onClick = {
                                paperWidth = PrinterPaperWidth.WIDTH_58MM
                                viewModel.updatePaperWidth(PrinterPaperWidth.WIDTH_58MM)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RkGoldPrimary,
                                selectedLabelColor = RkTextOnGold,
                                containerColor = RkSurfaceVariantDark,
                                labelColor = RkTextSecondary
                            ),
                            label = { Text("58mm Standard", fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }
        }

        // =====================================================================
        // SECTION 1: LOGO & BRANDING (Elements 1 to 4)
        // =====================================================================
        SectionHeader(title = "1. BRANDING & HEADER")

        // 1. Logo
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("1. Shop Logo", fontWeight = FontWeight.Bold)
                        Text("Print monochrome raster logo on top of receipt", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                    Switch(
                        checked = formatState.logoVisible && settings.showLogo,
                        onCheckedChange = { checked ->
                            formatState = formatState.copy(logoVisible = checked)
                            viewModel.updateReceiptSettings(
                                paperWidth = settings.paperWidth,
                                billFontSize = settings.billItemFontSize,
                                kotFontSize = settings.kotItemFontSize,
                                footer = settings.receiptFooter,
                                showCustomer = settings.showCustomerName,
                                showTable = settings.showTableNumber,
                                showLogo = checked
                            )
                        }
                    )
                }

                if (formatState.logoVisible && settings.showLogo) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (logoBitmap != null) {
                            Image(
                                bitmap = logoBitmap.asImageBitmap(),
                                contentDescription = "Shop Logo Preview",
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (logoBitmap != null) "Change Logo" else "Upload Logo")
                            }

                            if (logoBitmap != null) {
                                OutlinedButton(
                                    onClick = { viewModel.removeLogo() },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Remove")
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Shop Name
        ElementConfigCard(
            title = "2. Shop Name",
            subtitle = settings.shopName.ifBlank { "RK TIFFINES" },
            config = formatState.shopName,
            onConfigChange = { formatState = formatState.copy(shopName = it) }
        )

        // 3. Address
        ElementConfigCard(
            title = "3. Address",
            subtitle = settings.address.ifBlank { "Not set" },
            config = formatState.address,
            onConfigChange = { formatState = formatState.copy(address = it) }
        )

        // 4. Phone Number
        ElementConfigCard(
            title = "4. Phone Number",
            subtitle = settings.phone.ifBlank { "Not set" },
            config = formatState.phone,
            onConfigChange = { formatState = formatState.copy(phone = it) }
        )

        // =====================================================================
        // SECTION 2: METADATA & ORDER DETAILS (Elements 5 to 11)
        // =====================================================================
        SectionHeader(title = "2. BILL & CUSTOMER METADATA")

        // 5. Bill Number
        ElementConfigCard(
            title = "5. Bill Number",
            subtitle = "Sequential Bill # (e.g. #1001)",
            config = formatState.billNumber,
            onConfigChange = { formatState = formatState.copy(billNumber = it) }
        )

        // 6. Token Number
        ElementConfigCard(
            title = "6. Token Number",
            subtitle = "Daily counter token (e.g. #1)",
            config = formatState.tokenNumber,
            onConfigChange = { formatState = formatState.copy(tokenNumber = it) }
        )

        // 7. Date
        ElementConfigCard(
            title = "7. Date",
            subtitle = "Bill print date (dd-MM-yyyy)",
            config = formatState.date,
            onConfigChange = { formatState = formatState.copy(date = it) }
        )

        // 8. Time
        ElementConfigCard(
            title = "8. Time",
            subtitle = "Bill print time (hh:mm a)",
            config = formatState.time,
            onConfigChange = { formatState = formatState.copy(time = it) }
        )

        // 9. Order Type (DINE IN / PARCEL)
        ElementConfigCard(
            title = "9. Order Type (DINE IN / PARCEL)",
            subtitle = "Prints TYPE: DINE IN or TYPE: PARCEL",
            config = formatState.orderType,
            onConfigChange = { formatState = formatState.copy(orderType = it) }
        )

        // 10. Table Number
        ElementConfigCard(
            title = "10. Table Number",
            subtitle = "Prints Table # for Dine-in orders",
            config = formatState.tableNumber,
            onConfigChange = { formatState = formatState.copy(tableNumber = it) }
        )

        // 11. Customer Name
        ElementConfigCard(
            title = "11. Customer Name",
            subtitle = "Prints customer name when entered",
            config = formatState.customerName,
            onConfigChange = { formatState = formatState.copy(customerName = it) }
        )

        // =====================================================================
        // SECTION 3: ITEMS TABLE & COLUMNS (Elements 12 to 16)
        // =====================================================================
        SectionHeader(title = "3. ITEMS TABLE & COLUMNS")

        // 12. ITEM Heading
        ElementConfigCard(
            title = "12. ITEM Heading Row",
            subtitle = "Header row: ITEM, QTY, RATE, TOTAL",
            config = formatState.itemHeading,
            onConfigChange = { formatState = formatState.copy(itemHeading = it) }
        )

        // 13. Item Name
        ElementConfigCard(
            title = "13. Item Name",
            subtitle = "Format for item titles (auto-wraps on long names)",
            config = formatState.itemName,
            onConfigChange = { formatState = formatState.copy(itemName = it) }
        )

        // 14. QTY column
        ItemColumnConfigCard(
            title = "14. QTY Column",
            subtitle = "Quantity column in items table",
            config = formatState.colQty,
            onConfigChange = { formatState = formatState.copy(colQty = it) }
        )

        // 15. RATE column
        ItemColumnConfigCard(
            title = "15. RATE Column",
            subtitle = "Item unit price in items table",
            config = formatState.colRate,
            onConfigChange = { formatState = formatState.copy(colRate = it) }
        )

        // 16. TOTAL column
        ItemColumnConfigCard(
            title = "16. TOTAL Column",
            subtitle = "Item total amount in items table",
            config = formatState.colTotal,
            onConfigChange = { formatState = formatState.copy(colTotal = it) }
        )

        // =====================================================================
        // SECTION 4: TOTAL & PAYMENT (Elements 17, 18, 19)
        // =====================================================================
        SectionHeader(title = "4. TOTAL & PAYMENT MODE")

        // 17. Grand Total
        ElementConfigCard(
            title = "17. Grand Total",
            subtitle = "Prominent bill total (Rs.)",
            config = formatState.grandTotal,
            onConfigChange = { formatState = formatState.copy(grandTotal = it) }
        )

        // 18. Separator Lines (moved directly after Total option)
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("18. Separator Lines", fontWeight = FontWeight.Bold)
                        Text("Printed horizontal divider lines between receipt sections", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                    Switch(
                        checked = formatState.separatorLinesVisible,
                        onCheckedChange = { formatState = formatState.copy(separatorLinesVisible = it) }
                    )
                }

                if (formatState.separatorLinesVisible) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Line Style:", style = MaterialTheme.typography.bodyMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = formatState.separatorChar == '-',
                                onClick = { formatState = formatState.copy(separatorChar = '-') },
                                label = { Text("Dashed (- -)") }
                            )
                            FilterChip(
                                selected = formatState.separatorChar == '=',
                                onClick = { formatState = formatState.copy(separatorChar = '=') },
                                label = { Text("Double (==)") }
                            )
                            FilterChip(
                                selected = formatState.separatorChar == '*',
                                onClick = { formatState = formatState.copy(separatorChar = '*') },
                                label = { Text("Star (**)") }
                            )
                        }
                    }
                }
            }
        }

        // 19. Payment Mode
        ElementConfigCard(
            title = "19. Payment Mode",
            subtitle = "CASH, UPI, or CARD label with items count",
            config = formatState.paymentMode,
            onConfigChange = { formatState = formatState.copy(paymentMode = it) }
        )

        // =====================================================================
        // SECTION 5: FOOTER (Element 20)
        // =====================================================================
        SectionHeader(title = "5. FOOTER & DECORATORS")

        // 20. Footer Message
        ElementConfigCard(
            title = "20. Footer / Thank-you Message",
            subtitle = settings.receiptFooter.ifBlank { "Thank you! Visit again" },
            config = formatState.footerMessage,
            onConfigChange = { formatState = formatState.copy(footerMessage = it) }
        )

        // Auto Cut Setting
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto Cut: ${if (formatState.autoCut) "ON" else "OFF"}",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Send ESC/POS cut command to thermal printer after printing customer bill",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Switch(
                        checked = formatState.autoCut,
                        onCheckedChange = { enabled ->
                            formatState = formatState.copy(autoCut = enabled)
                            viewModel.setAutoCutPaper(enabled)
                        }
                    )
                }
            }
        }

        // Bottom action save button
        Button(
            onClick = {
                viewModel.updateReceiptFormat(formatState)
                viewModel.updatePaperWidth(paperWidth)
                viewModel.setAutoCutPaper(formatState.autoCut)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RkGoldPrimary, contentColor = RkTextOnGold),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = RkTextOnGold)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save All Receipt Format Settings", fontWeight = FontWeight.Bold, color = RkTextOnGold)
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = RkGoldPrimary),
        modifier = Modifier.padding(top = 8.dp)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ElementConfigCard(
    title: String,
    subtitle: String,
    config: ElementConfig,
    onConfigChange: (ElementConfig) -> Unit,
    showAlignment: Boolean = true,
    showFontSize: Boolean = true,
    showBold: Boolean = true
) {
    val chipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = RkGoldPrimary,
        selectedLabelColor = RkTextOnGold,
        containerColor = RkSurfaceVariantDark,
        labelColor = RkTextSecondary
    )

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = RkSurfaceDark),
        border = BorderStroke(1.dp, RkBorderGoldSubtle)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Row: Title + Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, color = RkTextPrimary)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = RkTextSecondary)
                }
                Switch(
                    checked = config.visible,
                    onCheckedChange = { onConfigChange(config.copy(visible = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = RkTextOnGold,
                        checkedTrackColor = RkGoldPrimary,
                        uncheckedThumbColor = RkTextSecondary,
                        uncheckedTrackColor = RkSurfaceVariantDark
                    )
                )
            }

            if (config.visible) {
                HorizontalDivider(color = RkBorderGoldSubtle)

                // Options: Alignment, Font Size, Bold
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Alignment
                    if (showAlignment) {
                        Column {
                            Text("Alignment", style = MaterialTheme.typography.labelSmall, color = RkTextSecondary)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                FilterChip(
                                    selected = config.alignment == ReceiptAlignment.LEFT,
                                    onClick = { onConfigChange(config.copy(alignment = ReceiptAlignment.LEFT)) },
                                    label = { Text("Left") },
                                    colors = chipColors,
                                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.FormatAlignLeft, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                )
                                FilterChip(
                                    selected = config.alignment == ReceiptAlignment.CENTER,
                                    onClick = { onConfigChange(config.copy(alignment = ReceiptAlignment.CENTER)) },
                                    label = { Text("Center") },
                                    colors = chipColors,
                                    leadingIcon = { Icon(Icons.Default.FormatAlignCenter, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                )
                                FilterChip(
                                    selected = config.alignment == ReceiptAlignment.RIGHT,
                                    onClick = { onConfigChange(config.copy(alignment = ReceiptAlignment.RIGHT)) },
                                    label = { Text("Right") },
                                    colors = chipColors,
                                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.FormatAlignRight, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                )
                            }
                        }
                    }

                    // Font Size
                    if (showFontSize) {
                        Column {
                            Text("Font Size", style = MaterialTheme.typography.labelSmall, color = RkTextSecondary)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                ReceiptFontSize.values().forEach { size ->
                                    FilterChip(
                                        selected = config.fontSize == size,
                                        onClick = { onConfigChange(config.copy(fontSize = size)) },
                                        colors = chipColors,
                                        label = { Text(size.displayName) }
                                    )
                                }
                            }
                        }
                    }

                    // Bold Toggle
                    if (showBold) {
                        Column {
                            Text("Style", style = MaterialTheme.typography.labelSmall, color = RkTextSecondary)
                            FilterChip(
                                selected = config.bold,
                                onClick = { onConfigChange(config.copy(bold = !config.bold)) },
                                colors = chipColors,
                                label = { Text("Bold") },
                                leadingIcon = { Icon(Icons.Default.FormatBold, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemColumnConfigCard(
    title: String,
    subtitle: String,
    config: ItemColumnConfig,
    onConfigChange: (ItemColumnConfig) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = RkSurfaceDark),
        border = BorderStroke(1.dp, RkBorderGoldSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = RkTextPrimary)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = RkTextSecondary)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (config.visible) {
                    FilterChip(
                        selected = config.bold,
                        onClick = { onConfigChange(config.copy(bold = !config.bold)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RkGoldPrimary,
                            selectedLabelColor = RkTextOnGold,
                            containerColor = RkSurfaceVariantDark,
                            labelColor = RkTextSecondary
                        ),
                        label = { Text("Bold") },
                        leadingIcon = { Icon(Icons.Default.FormatBold, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )
                }
                Switch(
                    checked = config.visible,
                    onCheckedChange = { onConfigChange(config.copy(visible = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = RkTextOnGold,
                        checkedTrackColor = RkGoldPrimary,
                        uncheckedThumbColor = RkTextSecondary,
                        uncheckedTrackColor = RkSurfaceVariantDark
                    )
                )
            }
        }
    }
}
