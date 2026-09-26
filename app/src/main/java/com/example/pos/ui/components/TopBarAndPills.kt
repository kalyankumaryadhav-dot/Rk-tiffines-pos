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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.PrinterConnectedGreen
import com.example.ui.theme.PrinterConnectingYellow
import com.example.ui.theme.PrinterErrorRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosTopBar(
    shopName: String,
    printerState: PrinterConnectionState,
    syncState: SyncState,
    nextBillNumber: Long,
    nextTokenNumber: Int,
    onPrinterStatusClick: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column {
                    Text(
                        text = shopName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Text(
                        text = "Bill #$nextBillNumber • Token #$nextTokenNumber",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
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

            Spacer(modifier = Modifier.width(8.dp))
        }
    )
}

@Composable
fun PrinterStatusPill(
    printerState: PrinterConnectionState,
    onClick: () -> Unit
) {
    val (bgColor, textColor, statusText, icon) = when (printerState) {
        is PrinterConnectionState.Connected -> {
            val name = printerState.deviceName.take(10)
            Tuple4(
                PrinterConnectedGreen.copy(alpha = 0.15f),
                PrinterConnectedGreen,
                "$name OK",
                Icons.Default.BluetoothConnected
            )
        }
        is PrinterConnectionState.Connecting -> {
            Tuple4(
                PrinterConnectingYellow.copy(alpha = 0.15f),
                PrinterConnectingYellow,
                "Connecting...",
                Icons.Default.Bluetooth
            )
        }
        is PrinterConnectionState.Disconnected -> {
            Tuple4(
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.colorScheme.onSurfaceVariant,
                "POS-8380 Disconnected",
                Icons.Default.BluetoothDisabled
            )
        }
        is PrinterConnectionState.Error -> {
            Tuple4(
                PrinterErrorRed.copy(alpha = 0.15f),
                PrinterErrorRed,
                "Printer Error",
                Icons.Default.Print
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
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = textColor
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = "Printer status",
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
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
        is SyncState.Syncing -> Pair(Icons.Default.Cloud, MaterialTheme.colorScheme.primary)
        is SyncState.Success -> Pair(Icons.Default.CloudDone, PrinterConnectedGreen)
        is SyncState.Error -> Pair(Icons.Default.CloudOff, MaterialTheme.colorScheme.error)
        is SyncState.Idle -> Pair(Icons.Default.CloudDone, MaterialTheme.colorScheme.outline)
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
