package com.example.pos.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pos.data.remote.SyncState
import com.example.pos.model.PrinterConnectionState
import com.example.pos.ui.PosTab
import com.example.ui.theme.PrinterConnectedGreen
import com.example.ui.theme.PrinterConnectingYellow
import com.example.ui.theme.PrinterErrorRed
import com.example.ui.theme.RkBlackBackground
import com.example.ui.theme.RkBorderGoldSubtle
import com.example.ui.theme.RkGoldPrimary
import com.example.ui.theme.RkOrangeSecondary
import com.example.ui.theme.RkSurfaceElevated
import com.example.ui.theme.RkTextPrimary
import com.example.ui.theme.RkYellowBright
import androidx.compose.foundation.border

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosTopBar(
    shopName: String,
    printerState: PrinterConnectionState,
    syncState: SyncState,
    nextBillNumber: Long,
    nextTokenNumber: Int,
    currentTab: PosTab = PosTab.BILLING,
    onNavigateTab: (PosTab) -> Unit = {},
    onBackToBilling: () -> Unit = {},
    onPrinterStatusClick: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = RkBlackBackground,
            titleContentColor = RkTextPrimary
        ),
        navigationIcon = {
            if (currentTab != PosTab.BILLING) {
                IconButton(
                    onClick = onBackToBilling,
                    modifier = Modifier.testTag("top_bar_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Billing",
                        tint = RkTextPrimary
                    )
                }
            }
        },
        title = {
            if (currentTab == PosTab.BILLING) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column {
                        Text(
                            text = shopName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = RkTextPrimary
                            )
                        )
                        Text(
                            text = "Bill #$nextBillNumber • Token #$nextTokenNumber",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = RkGoldPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            } else {
                val screenTitle = when (currentTab) {
                    PosTab.SALES -> "Sales & Reports"
                    PosTab.MENU -> "Menu Items"
                    PosTab.SETTINGS -> "Settings"
                    PosTab.BILLING -> shopName
                }
                Text(
                    text = screenTitle,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = RkTextPrimary
                    )
                )
            }
        },
        actions = {
            // Cloud Sync Indicator
            SyncStatusIndicator(syncState = syncState)

            Spacer(modifier = Modifier.width(6.dp))

            // Printer Status Pill
            PrinterStatusPill(
                printerState = printerState,
                onClick = onPrinterStatusClick
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Top-right 3-dot overflow menu (⋮)
            var menuExpanded by remember { mutableStateOf(false) }
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.testTag("top_overflow_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options menu",
                        tint = RkTextPrimary
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier
                        .background(RkSurfaceElevated)
                        .border(1.dp, RkBorderGoldSubtle, RoundedCornerShape(8.dp))
                        .widthIn(min = 210.dp)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Sales & Reports",
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.bodyMedium,
                                color = RkTextPrimary
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = RkGoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onNavigateTab(PosTab.SALES)
                        },
                        modifier = Modifier.testTag("menu_sales_reports")
                    )

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Menu Items",
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.bodyMedium,
                                color = RkTextPrimary
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.RestaurantMenu,
                                contentDescription = null,
                                tint = RkOrangeSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onNavigateTab(PosTab.MENU)
                        },
                        modifier = Modifier.testTag("menu_menu_items")
                    )

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Settings",
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.bodyMedium,
                                color = RkTextPrimary
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = RkYellowBright,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onNavigateTab(PosTab.SETTINGS)
                        },
                        modifier = Modifier.testTag("menu_settings")
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))
        }
    )
}

@Composable
fun PrinterStatusPill(
    printerState: PrinterConnectionState,
    onClick: () -> Unit
) {
    val (bgColor, dotColor, textColor, statusText) = when (printerState) {
        is PrinterConnectionState.Connected -> {
            Tuple4(
                PrinterConnectedGreen.copy(alpha = 0.15f),
                PrinterConnectedGreen,
                PrinterConnectedGreen,
                "Bluetooth Connected"
            )
        }
        is PrinterConnectionState.Connecting -> {
            Tuple4(
                PrinterConnectingYellow.copy(alpha = 0.15f),
                PrinterConnectingYellow,
                PrinterConnectingYellow,
                "Bluetooth Connecting…"
            )
        }
        is PrinterConnectionState.Disconnected -> {
            Tuple4(
                Color(0x28E64A19),
                PrinterErrorRed,
                Color(0xFFFF7043),
                "Bluetooth Disconnected"
            )
        }
        is PrinterConnectionState.Unavailable -> {
            Tuple4(
                Color(0x28E64A19),
                PrinterErrorRed,
                Color(0xFFFF7043),
                "Bluetooth Unavailable"
            )
        }
        is PrinterConnectionState.Error -> {
            Tuple4(
                PrinterErrorRed.copy(alpha = 0.15f),
                PrinterErrorRed,
                PrinterErrorRed,
                "Bluetooth Disconnected"
            )
        }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        modifier = Modifier
            .testTag("printer_status_pill")
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (printerState is PrinterConnectionState.Connecting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(10.dp),
                    strokeWidth = 2.dp,
                    color = dotColor
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            )
        }
    }
}

@Composable
fun SyncStatusIndicator(syncState: SyncState) {
    val (icon, tint) = when (syncState) {
        is SyncState.Syncing -> Pair(Icons.Default.Cloud, RkGoldPrimary)
        is SyncState.Success -> Pair(Icons.Default.CloudDone, PrinterConnectedGreen)
        is SyncState.AllSynced -> Pair(Icons.Default.CloudDone, PrinterConnectedGreen)
        is SyncState.Offline -> Pair(Icons.Default.CloudOff, RkOrangeSecondary)
        is SyncState.Failed -> Pair(Icons.Default.CloudOff, MaterialTheme.colorScheme.error)
        is SyncState.Pending -> Pair(Icons.Default.Cloud, RkGoldPrimary)
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        if (syncState is SyncState.Syncing) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = tint
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = "Cloud Sync Status",
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
