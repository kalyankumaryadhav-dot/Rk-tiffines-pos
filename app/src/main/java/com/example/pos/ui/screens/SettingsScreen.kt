package com.example.pos.ui.screens

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pos.model.BluetoothDeviceInfo
import com.example.pos.model.ItemFontSize
import com.example.pos.model.PrinterConnectionState
import com.example.pos.model.PrinterPaperWidth
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
import com.example.ui.theme.RkSurfaceElevated
import com.example.ui.theme.RkSurfaceVariantDark
import com.example.ui.theme.RkTextMuted
import com.example.ui.theme.RkTextOnGold
import com.example.ui.theme.RkTextPrimary
import com.example.ui.theme.RkTextSecondary
import com.example.ui.theme.RkYellowBright
import java.io.InputStream

enum class SettingsSection(val title: String) {
    BUSINESS("BUSINESS"),
    RECEIPT_FORMAT("RECEIPT FORMAT"),
    PRINTER("PRINTER"),
    BILLING("BILLING"),
    SALES("SALES")
}

@Composable
fun SettingsScreen(
    viewModel: PosViewModel,
    settings: ShopSettings,
    printerState: PrinterConnectionState,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableStateOf(SettingsSection.BUSINESS) }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text(
            text = "POS Terminal Settings",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = RkTextPrimary)
        )
        Text(
            text = "Configure business details, POS-8380 thermal printer, receipt layout, and numbering",
            style = MaterialTheme.typography.bodySmall.copy(color = RkTextSecondary)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Navigation Tabs for Settings
        TabRow(
            selectedTabIndex = selectedSection.ordinal,
            containerColor = RkSurfaceDark,
            contentColor = RkGoldPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSection.ordinal]),
                    color = RkGoldPrimary
                )
            },
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .border(BorderStroke(1.dp, RkBorderGoldSubtle), RoundedCornerShape(10.dp))
        ) {
            SettingsSection.values().forEach { section ->
                val isSelected = selectedSection == section
                Tab(
                    selected = isSelected,
                    onClick = { selectedSection = section },
                    selectedContentColor = RkGoldPrimary,
                    unselectedContentColor = RkTextSecondary,
                    text = {
                        Text(
                            text = section.title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (isSelected) RkGoldPrimary else RkTextSecondary
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (selectedSection) {
                SettingsSection.BUSINESS -> Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) { BusinessSettingsSection(viewModel, settings) }
                SettingsSection.RECEIPT_FORMAT -> ReceiptFormatScreen(viewModel, settings, printerState)
                SettingsSection.PRINTER -> Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) { PrinterSettingsSection(viewModel, settings, printerState) }
                SettingsSection.BILLING -> Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) { BillingSettingsSection(viewModel, settings) }
                SettingsSection.SALES -> Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) { SalesSyncSettingsSection(viewModel) }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 1. BUSINESS SETTINGS
// -----------------------------------------------------------------------------
@Composable
fun BusinessSettingsSection(viewModel: PosViewModel, settings: ShopSettings) {
    var shopName by remember(settings.shopName) { mutableStateOf(settings.shopName) }
    var address by remember(settings.address) { mutableStateOf(settings.address) }
    var phone by remember(settings.phone) { mutableStateOf(settings.phone) }
    val context = LocalContext.current

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = RkGoldPrimary,
        unfocusedBorderColor = RkBorderGoldSubtle,
        focusedTextColor = RkTextPrimary,
        unfocusedTextColor = RkTextPrimary,
        cursorColor = RkGoldPrimary,
        focusedLabelColor = RkGoldPrimary,
        unfocusedLabelColor = RkTextSecondary,
        focusedContainerColor = RkSurfaceDark,
        unfocusedContainerColor = RkSurfaceDark
    )

    // Photo picker launcher (M3 Android zero-permission picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    viewModel.saveUploadedLogo(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = RkSurfaceDark),
            border = BorderStroke(1.dp, RkBorderGoldSubtle)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Business Profile", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = RkTextPrimary))

                OutlinedTextField(
                    value = shopName,
                    onValueChange = { shopName = it },
                    label = { Text("Shop Name") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Shop Address") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = { viewModel.updateBusinessDetails(shopName, address, phone) },
                    colors = ButtonDefaults.buttonColors(containerColor = RkGoldPrimary, contentColor = RkTextOnGold),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Save Business Details", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Logo Upload Section
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = RkSurfaceDark),
            border = BorderStroke(1.dp, RkBorderGoldSubtle)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Thermal Receipt Logo", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = RkTextPrimary))
                Text(
                    text = "Upload a black & white or high-contrast logo. It will be converted into crisp monochrome raster ESC/POS format for thermal printing.",
                    style = MaterialTheme.typography.bodySmall,
                    color = RkTextSecondary
                )

                val logoBitmap = remember(settings.logoPath) { viewModel.repository.getLogoBitmap() }

                if (logoBitmap != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Image(
                            bitmap = logoBitmap.asImageBitmap(),
                            contentDescription = "Uploaded Logo",
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, RkBorderGoldSubtle, RoundedCornerShape(8.dp))
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Logo Active", fontWeight = FontWeight.Bold, color = PrinterConnectedGreen)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    border = BorderStroke(1.dp, RkBorderGoldSubtle),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Change", color = RkGoldPrimary)
                                }
                                Button(
                                    onClick = { viewModel.removeLogo() },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrinterErrorRed, contentColor = Color.White),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Remove")
                                }
                            }
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        border = BorderStroke(1.dp, RkBorderGoldSubtle),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null, tint = RkGoldPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upload Logo from Device Gallery", color = RkGoldPrimary)
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 2. PRINTER SETTINGS
// -----------------------------------------------------------------------------
@Composable
fun PrinterSettingsSection(
    viewModel: PosViewModel,
    settings: ShopSettings,
    printerState: PrinterConnectionState
) {
    val pairedDevices by viewModel.printerManager.pairedDevices.collectAsState()
    val discoveredDevices by viewModel.printerManager.discoveredDevices.collectAsState()
    val isScanning by viewModel.printerManager.isScanning.collectAsState()

    var autoBill by remember(settings.autoPrintBill) { mutableStateOf(settings.autoPrintBill) }
    var autoToken by remember(settings.autoPrintToken) { mutableStateOf(settings.autoPrintToken) }
    var autoBoth by remember(settings.autoPrintBoth) { mutableStateOf(settings.autoPrintBoth) }
    var autoReconnect by remember(settings.autoReconnectPrinter) { mutableStateOf(settings.autoReconnectPrinter) }

    LaunchedEffect(Unit) {
        viewModel.printerManager.loadPairedDevices()
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Printer Connection Status Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = RkSurfaceDark),
            border = BorderStroke(1.dp, RkBorderGoldSubtle)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("POS-8380 Bluetooth Classic SPP Status", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = RkTextPrimary))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (printerState) {
                        is PrinterConnectionState.Connected -> {
                            Icon(Icons.Default.BluetoothConnected, contentDescription = null, tint = PrinterConnectedGreen, modifier = Modifier.size(24.dp))
                            Column {
                                Text("CONNECTED to ${printerState.deviceName}", fontWeight = FontWeight.Bold, color = PrinterConnectedGreen)
                                Text("MAC: ${printerState.address} • RFCOMM Socket Active", style = MaterialTheme.typography.bodySmall, color = RkTextSecondary)
                            }
                        }
                        is PrinterConnectionState.Connecting -> {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = RkGoldPrimary)
                            Text("Connecting to saved printer...", fontWeight = FontWeight.SemiBold, color = PrinterConnectingYellow)
                        }
                        is PrinterConnectionState.Disconnected -> {
                            Icon(Icons.Default.BluetoothDisabled, contentDescription = null, tint = RkTextMuted, modifier = Modifier.size(24.dp))
                            Column {
                                Text("Disconnected", fontWeight = FontWeight.Bold, color = RkTextPrimary)
                                if (settings.savedPrinterMac.isNotBlank()) {
                                    Text("Saved Printer: ${settings.savedPrinterName} (${settings.savedPrinterMac})", style = MaterialTheme.typography.bodySmall, color = RkTextSecondary)
                                } else {
                                    Text("No printer saved yet. Select POS-8380 from paired devices below.", style = MaterialTheme.typography.bodySmall, color = RkTextSecondary)
                                }
                            }
                        }
                        is PrinterConnectionState.Error -> {
                            Icon(Icons.Default.Print, contentDescription = null, tint = PrinterErrorRed, modifier = Modifier.size(24.dp))
                            Text("Error: ${printerState.message}", color = PrinterErrorRed, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (printerState is PrinterConnectionState.Connected) {
                        Button(
                            onClick = { viewModel.testPrint() },
                            colors = ButtonDefaults.buttonColors(containerColor = RkGoldPrimary, contentColor = RkTextOnGold),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, tint = RkTextOnGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Print", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.disconnectPrinter() },
                            border = BorderStroke(1.dp, RkBorderGoldSubtle),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Disconnect", color = RkTextSecondary)
                        }
                    } else if (settings.savedPrinterMac.isNotBlank()) {
                        Button(
                            onClick = {
                                viewModel.connectPrinter(settings.savedPrinterMac, settings.savedPrinterName.ifBlank { "POS-8380" })
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RkGoldPrimary, contentColor = RkTextOnGold),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Connect POS-8380", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (settings.savedPrinterMac.isNotBlank()) {
                        OutlinedButton(
                            onClick = { viewModel.forgetPrinter() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrinterErrorRed),
                            border = BorderStroke(1.dp, PrinterErrorRed.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Forget Printer")
                        }
                    }
                }
            }
        }

        // Auto-print & Reconnect Preferences
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = RkSurfaceDark),
            border = BorderStroke(1.dp, RkBorderGoldSubtle)
        ) {
            val switchColors = SwitchDefaults.colors(
                checkedThumbColor = RkTextOnGold,
                checkedTrackColor = RkGoldPrimary,
                uncheckedThumbColor = RkTextSecondary,
                uncheckedTrackColor = RkSurfaceVariantDark
            )

            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Printing Automation", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = RkTextPrimary))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Auto-Reconnect to POS-8380", fontWeight = FontWeight.SemiBold, color = RkTextPrimary)
                        Text("Reconnects automatically on app launch and resume", style = MaterialTheme.typography.bodySmall, color = RkTextSecondary)
                    }
                    Switch(
                        checked = autoReconnect,
                        onCheckedChange = {
                            autoReconnect = it
                            viewModel.updateAutoPrintSettings(autoBill, autoToken, autoBoth, autoReconnect)
                        },
                        colors = switchColors
                    )
                }

                HorizontalDivider(color = RkBorderGoldSubtle)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Auto-Print Customer Bill on Save", color = RkTextPrimary)
                    Switch(
                        checked = autoBill,
                        onCheckedChange = {
                            autoBill = it
                            viewModel.updateAutoPrintSettings(autoBill, autoToken, autoBoth, autoReconnect)
                        },
                        colors = switchColors
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Auto-Print Kitchen Token (KOT) on Save", color = RkTextPrimary)
                    Switch(
                        checked = autoToken,
                        onCheckedChange = {
                            autoToken = it
                            viewModel.updateAutoPrintSettings(autoBill, autoToken, autoBoth, autoReconnect)
                        },
                        colors = switchColors
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Auto-Print Both Bill + Token on Save", color = RkTextPrimary)
                    Switch(
                        checked = autoBoth,
                        onCheckedChange = {
                            autoBoth = it
                            viewModel.updateAutoPrintSettings(autoBill, autoToken, autoBoth, autoReconnect)
                        },
                        colors = switchColors
                    )
                }

                HorizontalDivider(color = RkBorderGoldSubtle)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto Cut: ${if (settings.autoCutPaper) "ON" else "OFF"}",
                            fontWeight = FontWeight.SemiBold,
                            color = RkTextPrimary
                        )
                        Text(
                            text = "Send ESC/POS cut command to POS-8380 printer after printing",
                            style = MaterialTheme.typography.bodySmall,
                            color = RkTextSecondary
                        )
                    }
                    Switch(
                        checked = settings.autoCutPaper,
                        onCheckedChange = { enabled ->
                            viewModel.setAutoCutPaper(enabled)
                        },
                        colors = switchColors,
                        modifier = Modifier.testTag("switch_auto_cut")
                    )
                }
            }
        }

        // Paired Devices List Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = RkSurfaceDark),
            border = BorderStroke(1.dp, RkBorderGoldSubtle)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Paired Bluetooth Printers", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = RkTextPrimary))
                    IconButton(onClick = { viewModel.printerManager.loadPairedDevices() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh paired list", tint = RkGoldPrimary)
                    }
                }

                if (pairedDevices.isEmpty()) {
                    Text(
                        "No paired Bluetooth devices found. Please pair your POS-8380 thermal printer in Android Bluetooth Settings first.",
                        style = MaterialTheme.typography.bodySmall,
                        color = RkTextSecondary
                    )
                } else {
                    pairedDevices.forEach { dev ->
                        val isPos8380 = dev.name.contains("8380", ignoreCase = true) || dev.name.contains("POS", ignoreCase = true)
                        val isConnectedHere = printerState is PrinterConnectionState.Connected && printerState.address == dev.address

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isConnectedHere) PrinterConnectedGreen.copy(alpha = 0.15f) else RkSurfaceVariantDark,
                            border = if (isPos8380) BorderStroke(1.5.dp, RkGoldPrimary) else BorderStroke(1.dp, RkBorderGoldSubtle),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(dev.name, fontWeight = FontWeight.Bold, color = RkTextPrimary)
                                        if (isPos8380) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = RkGoldPrimary
                                            ) {
                                                Text("RECOMMENDED", color = RkTextOnGold, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                            }
                                        }
                                    }
                                    Text("MAC: ${dev.address}", style = MaterialTheme.typography.bodySmall, color = RkTextSecondary)
                                }

                                if (isConnectedHere) {
                                    Text("CONNECTED", color = PrinterConnectedGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                } else {
                                    Button(
                                        onClick = { viewModel.connectPrinter(dev.address, dev.name) },
                                        colors = ButtonDefaults.buttonColors(containerColor = RkGoldPrimary, contentColor = RkTextOnGold),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text("Select & Connect", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = RkBorderGoldSubtle)

                // Scan for Nearby Devices
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Scan for Nearby Printers", fontWeight = FontWeight.SemiBold, color = RkTextPrimary)
                    if (isScanning) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = RkGoldPrimary)
                    } else {
                        OutlinedButton(
                            onClick = { viewModel.printerManager.startScan() },
                            border = BorderStroke(1.dp, RkBorderGoldSubtle),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Scan Now", color = RkGoldPrimary)
                        }
                    }
                }

                if (discoveredDevices.isNotEmpty()) {
                    Text("Discovered Devices:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RkGoldPrimary))
                    discoveredDevices.forEach { dev ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = RkSurfaceVariantDark,
                            border = BorderStroke(1.dp, RkBorderGoldSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(dev.name, fontWeight = FontWeight.SemiBold, color = RkTextPrimary)
                                    Text(dev.address, style = MaterialTheme.typography.bodySmall, color = RkTextSecondary)
                                }
                                Button(
                                    onClick = { viewModel.connectPrinter(dev.address, dev.name) },
                                    colors = ButtonDefaults.buttonColors(containerColor = RkGoldPrimary, contentColor = RkTextOnGold),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Connect", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 4. BILLING SETTINGS
// -----------------------------------------------------------------------------
@Composable
fun BillingSettingsSection(viewModel: PosViewModel, settings: ShopSettings) {
    var billNumberInput by remember(settings.nextBillNumber) { mutableStateOf(settings.nextBillNumber.toString()) }
    var tokenNumberInput by remember(settings.nextTokenNumber) { mutableStateOf(settings.nextTokenNumber.toString()) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = RkGoldPrimary,
        unfocusedBorderColor = RkBorderGoldSubtle,
        focusedTextColor = RkTextPrimary,
        unfocusedTextColor = RkTextPrimary,
        cursorColor = RkGoldPrimary,
        focusedLabelColor = RkGoldPrimary,
        unfocusedLabelColor = RkTextSecondary,
        focusedContainerColor = RkSurfaceDark,
        unfocusedContainerColor = RkSurfaceDark
    )

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = RkSurfaceDark),
        border = BorderStroke(1.dp, RkBorderGoldSubtle)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Bill & Token Numbering Control", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = RkTextPrimary))
            Text(
                "Bill numbers and token numbers increment automatically and are persisted across app/phone restarts. You can customize or reset them below.",
                style = MaterialTheme.typography.bodySmall,
                color = RkTextSecondary
            )

            // Next Bill Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = billNumberInput,
                    onValueChange = { billNumberInput = it },
                    label = { Text("Next Bill Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = {
                        val num = billNumberInput.toLongOrNull()
                        if (num != null && num > 0) {
                            viewModel.resetNextBillNumber(num)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RkGoldPrimary, contentColor = RkTextOnGold),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Update", fontWeight = FontWeight.Bold)
                }
            }

            // Next Token Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = tokenNumberInput,
                    onValueChange = { tokenNumberInput = it },
                    label = { Text("Next Token Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = {
                        val num = tokenNumberInput.toIntOrNull()
                        if (num != null && num > 0) {
                            viewModel.resetNextTokenNumber(num)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RkGoldPrimary, contentColor = RkTextOnGold),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Update", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        viewModel.resetNextTokenNumber(1)
                        tokenNumberInput = "1"
                    },
                    border = BorderStroke(1.dp, RkBorderGoldSubtle),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Reset to 1", color = RkGoldPrimary)
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 5. SALES SYNC SETTINGS
// -----------------------------------------------------------------------------
@Composable
fun SalesSyncSettingsSection(viewModel: PosViewModel) {
    val syncState by viewModel.syncState.collectAsState()

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = RkSurfaceDark),
        border = BorderStroke(1.dp, RkBorderGoldSubtle)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Cloud Sales Synchronization (Firestore)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = RkTextPrimary))
            Text(
                "Sales records are always saved in the native Room database first for instant offline billing. When network is available, sales are synchronized to Firebase Firestore so all business devices can stay in sync without requiring login.",
                style = MaterialTheme.typography.bodySmall,
                color = RkTextSecondary
            )

            HorizontalDivider(color = RkBorderGoldSubtle)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sync Action", fontWeight = FontWeight.SemiBold, color = RkTextPrimary)
                Button(
                    onClick = { viewModel.triggerFirestoreSync() },
                    colors = ButtonDefaults.buttonColors(containerColor = RkGoldPrimary, contentColor = RkTextOnGold),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Cloud, contentDescription = null, tint = RkTextOnGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Synchronize Sales Now", fontWeight = FontWeight.Bold)
                }
            }

            Text(
                text = "Collection: sales • Prevents duplicate entries using persistent unique Bill UUIDs",
                style = MaterialTheme.typography.labelSmall,
                color = RkTextMuted
            )
        }
    }
}
