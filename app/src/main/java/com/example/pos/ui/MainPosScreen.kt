package com.example.pos.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pos.ui.components.PosTopBar
import com.example.pos.ui.screens.BillingScreen
import com.example.pos.ui.screens.MenuManagementScreen
import com.example.pos.ui.screens.SalesScreen
import com.example.pos.ui.screens.SettingsScreen
import kotlinx.coroutines.flow.collectLatest

enum class PosTab(val title: String, val icon: ImageVector) {
    BILLING("Billing", Icons.Default.PointOfSale),
    SALES("Sales & Reports", Icons.Default.BarChart),
    MENU("Menu Items", Icons.Default.RestaurantMenu),
    SETTINGS("Settings", Icons.Default.Settings)
}

@Composable
fun MainPosScreen(viewModel: PosViewModel) {
    var currentTab by remember { mutableStateOf(PosTab.BILLING) }
    val snackbarHostState = remember { SnackbarHostState() }

    val settings by viewModel.settings.collectAsState()
    val printerState by viewModel.printerState.collectAsState()
    val syncState by viewModel.syncState.collectAsState()

    val cartItems by viewModel.cartItems.collectAsState()
    val allMenuItems by viewModel.allMenuItems.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val orderType by viewModel.orderType.collectAsState()
    val tableNumber by viewModel.tableNumber.collectAsState()
    val customerName by viewModel.customerName.collectAsState()
    val paymentMode by viewModel.paymentMode.collectAsState()

    val filteredOrders by viewModel.filteredOrders.collectAsState()
    val salesSummary by viewModel.salesSummary.collectAsState()
    val salesFilter by viewModel.salesFilter.collectAsState()

    // Bluetooth permission request launcher
    val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val connectGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions[Manifest.permission.BLUETOOTH_CONNECT] == true
        } else {
            true
        }
        if (connectGranted) {
            viewModel.printerManager.loadPairedDevices()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bluetoothPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            )
        } else {
            viewModel.printerManager.loadPairedDevices()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Adaptive layout detection: NavigationRail for tablets / wide screens, NavigationBar for phones
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 840.dp

        if (isWideScreen) {
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    PosTab.values().forEach { tab ->
                        NavigationRailItem(
                            selected = currentTab == tab,
                            onClick = { currentTab = tab },
                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                            label = { Text(tab.title, fontSize = 11.sp, fontWeight = if (currentTab == tab) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }

                Scaffold(
                    topBar = {
                        PosTopBar(
                            shopName = settings.shopName,
                            printerState = printerState,
                            syncState = syncState,
                            nextBillNumber = settings.nextBillNumber,
                            nextTokenNumber = settings.nextTokenNumber,
                            onPrinterStatusClick = { currentTab = PosTab.SETTINGS }
                        )
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    modifier = Modifier.weight(1f)
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
                            when (tab) {
                                PosTab.BILLING -> BillingScreen(
                                    viewModel = viewModel,
                                    cartItems = cartItems,
                                    menuItems = allMenuItems,
                                    selectedCategory = selectedCategory,
                                    orderType = orderType,
                                    tableNumber = tableNumber,
                                    customerName = customerName,
                                    paymentMode = paymentMode
                                )
                                PosTab.SALES -> SalesScreen(
                                    viewModel = viewModel,
                                    salesSummary = salesSummary,
                                    orders = filteredOrders,
                                    currentFilter = salesFilter,
                                    syncState = syncState
                                )
                                PosTab.MENU -> MenuManagementScreen(
                                    viewModel = viewModel,
                                    menuItems = allMenuItems
                                )
                                PosTab.SETTINGS -> SettingsScreen(
                                    viewModel = viewModel,
                                    settings = settings,
                                    printerState = printerState
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Scaffold(
                topBar = {
                    PosTopBar(
                        shopName = settings.shopName,
                        printerState = printerState,
                        syncState = syncState,
                        nextBillNumber = settings.nextBillNumber,
                        nextTokenNumber = settings.nextTokenNumber,
                        onPrinterStatusClick = { currentTab = PosTab.SETTINGS }
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        PosTab.values().forEach { tab ->
                            NavigationBarItem(
                                selected = currentTab == tab,
                                onClick = { currentTab = tab },
                                icon = { Icon(tab.icon, contentDescription = tab.title) },
                                label = { Text(tab.title, fontSize = 11.sp, fontWeight = if (currentTab == tab) FontWeight.Bold else FontWeight.Normal) },
                                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
                        when (tab) {
                            PosTab.BILLING -> BillingScreen(
                                viewModel = viewModel,
                                cartItems = cartItems,
                                menuItems = allMenuItems,
                                selectedCategory = selectedCategory,
                                orderType = orderType,
                                tableNumber = tableNumber,
                                customerName = customerName,
                                paymentMode = paymentMode
                            )
                            PosTab.SALES -> SalesScreen(
                                viewModel = viewModel,
                                salesSummary = salesSummary,
                                orders = filteredOrders,
                                currentFilter = salesFilter,
                                syncState = syncState
                            )
                            PosTab.MENU -> MenuManagementScreen(
                                viewModel = viewModel,
                                menuItems = allMenuItems
                            )
                            PosTab.SETTINGS -> SettingsScreen(
                                viewModel = viewModel,
                                settings = settings,
                                printerState = printerState
                            )
                        }
                    }
                }
            }
        }
    }
}
