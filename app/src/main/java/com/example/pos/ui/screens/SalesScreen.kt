package com.example.pos.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pos.data.remote.SyncState
import com.example.pos.model.BillRecord
import com.example.pos.model.OrderType
import com.example.pos.model.PaymentMode
import com.example.pos.ui.PosViewModel
import com.example.pos.ui.SalesSummary
import com.example.pos.ui.SalesTimeFilter
import com.example.ui.theme.CardPurple
import com.example.ui.theme.CashGreen
import com.example.ui.theme.LeafGreenTertiary
import com.example.ui.theme.PrinterConnectedGreen
import com.example.ui.theme.UpiBlue

@Composable
fun SalesScreen(
    viewModel: PosViewModel,
    salesSummary: SalesSummary,
    orders: List<BillRecord>,
    currentFilter: SalesTimeFilter,
    syncState: SyncState,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedOrderForDetail by remember { mutableStateOf<BillRecord?>(null) }

    val filteredOrders = if (searchQuery.isBlank()) {
        orders
    } else {
        orders.filter {
            it.billNumber.toString().contains(searchQuery) ||
            it.tokenNumber.toString().contains(searchQuery) ||
            (it.customerName?.contains(searchQuery, ignoreCase = true) == true) ||
            (it.tableNumber?.contains(searchQuery) == true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Filter Tabs: TODAY, WEEKLY, MONTHLY, ALL
        TabRow(
            selectedTabIndex = currentFilter.ordinal,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(10.dp)
        ) {
            SalesTimeFilter.values().forEach { filter ->
                Tab(
                    selected = currentFilter == filter,
                    onClick = { viewModel.setSalesFilter(filter) },
                    text = {
                        Text(
                            text = filter.name,
                            fontWeight = if (currentFilter == filter) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard(
                title = "TOTAL REVENUE",
                amount = "₹ %.0f".format(salesSummary.totalRevenue),
                subtitle = "${salesSummary.billCount} Bills",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1.3f)
            )

            SummaryCard(
                title = "CASH",
                amount = "₹ %.0f".format(salesSummary.cashTotal),
                subtitle = "Total Cash",
                color = CashGreen,
                icon = Icons.Default.Payments,
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "UPI",
                amount = "₹ %.0f".format(salesSummary.upiTotal),
                subtitle = "Total UPI",
                color = UpiBlue,
                icon = Icons.Default.QrCode2,
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "CARD",
                amount = "₹ %.0f".format(salesSummary.cardTotal),
                subtitle = "Total Card",
                color = CardPurple,
                icon = Icons.Default.CreditCard,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Cloud Synchronization Bar
        CloudSyncBar(
            syncState = syncState,
            onSyncClick = { viewModel.triggerFirestoreSync() }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Orders Header & Search
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Past Bills (${filteredOrders.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search Bill / Token #", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                singleLine = true,
                modifier = Modifier
                    .width(220.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Orders List
        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No bills found for selected period",
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredOrders, key = { it.billId }) { order ->
                    OrderHistoryItem(
                        order = order,
                        onClick = { selectedOrderForDetail = order },
                        onReprintBill = { viewModel.reprintBill(order) },
                        onReprintToken = { viewModel.reprintToken(order) }
                    )
                }
            }
        }
    }

    // Order Details & Reprint Dialog
    selectedOrderForDetail?.let { order ->
        OrderDetailDialog(
            order = order,
            onDismiss = { selectedOrderForDetail = null },
            onReprintBill = {
                viewModel.reprintBill(order)
                selectedOrderForDetail = null
            },
            onReprintToken = {
                viewModel.reprintToken(order)
                selectedOrderForDetail = null
            }
        )
    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: String,
    subtitle: String,
    color: Color,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                )
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    }
}

@Composable
fun CloudSyncBar(
    syncState: SyncState,
    onSyncClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (syncState) {
                    is SyncState.Syncing -> {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text("Synchronizing sales to Firestore...", fontSize = 13.sp)
                    }
                    is SyncState.Success -> {
                        Icon(Icons.Default.CloudDone, contentDescription = null, tint = PrinterConnectedGreen, modifier = Modifier.size(20.dp))
                        Text(syncState.message, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                    is SyncState.Error -> {
                        Icon(Icons.Default.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                        Text("Sync error: ${syncState.message}", fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
                    }
                    is SyncState.Idle -> {
                        Icon(Icons.Default.CloudDone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Text("Cloud Sales Synchronization: Online", fontSize = 13.sp)
                    }
                }
            }

            OutlinedButton(
                onClick = onSyncClick,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Sync Now", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun OrderHistoryItem(
    order: BillRecord,
    onClick: () -> Unit,
    onReprintBill: () -> Unit,
    onReprintToken: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Bill #${order.billNumber}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "Token #${order.tokenNumber}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = if (order.orderType == OrderType.DINE_IN) "Dine In (T${order.tableNumber ?: "-"})" else "Parcel",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                val itemsText = order.items.joinToString(", ") { "${it.menuItem.name} (${it.quantity})" }
                Text(
                    text = itemsText,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${order.dateString} at ${order.timeString} • Mode: ${order.paymentMode.name}",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline)
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "₹ %.0f".format(order.grandTotal),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedButton(
                        onClick = onReprintBill,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(30.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Bill", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = onReprintToken,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(30.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("KOT", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderDetailDialog(
    order: BillRecord,
    onDismiss: () -> Unit,
    onReprintBill: () -> Unit,
    onReprintToken: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Bill #${order.billNumber} Details", fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        "Token #${order.tokenNumber}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Date & Time: ${order.dateString} ${order.timeString}")
                Text("Order Type: ${order.orderType.name} ${if (!order.tableNumber.isNullOrBlank()) "(Table ${order.tableNumber})" else ""}")
                if (!order.customerName.isNullOrBlank()) {
                    Text("Customer: ${order.customerName}")
                }
                Text("Payment Mode: ${order.paymentMode.name}")

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Items:", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                for (item in order.items) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${item.menuItem.name} x ${item.quantity}")
                        Text("₹ %.0f".format(item.total), fontWeight = FontWeight.SemiBold)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Grand Total:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("₹ %.0f".format(order.grandTotal), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onReprintBill,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reprint Bill")
                }
                Button(
                    onClick = onReprintToken,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reprint KOT")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
