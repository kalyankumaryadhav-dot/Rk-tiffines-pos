package com.example.pos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pos.model.CartItem
import com.example.pos.model.MenuCategories
import com.example.pos.model.MenuItem
import com.example.pos.model.OrderType
import com.example.pos.model.PaymentMode
import com.example.pos.ui.PosViewModel
import com.example.ui.theme.CardPurple
import com.example.ui.theme.CashGreen
import com.example.ui.theme.UpiBlue

@Composable
fun BillingScreen(
    viewModel: PosViewModel,
    cartItems: List<CartItem>,
    menuItems: List<MenuItem>,
    selectedCategory: String,
    orderType: OrderType,
    paymentMode: PaymentMode,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWide = maxWidth >= 840.dp

        if (isWide) {
            // Split Tablet / Landscape Layout: Menu on Left (60%), Cart on Right (40%)
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxHeight()
                        .padding(12.dp)
                ) {
                    OrderTypeBar(
                        orderType = orderType,
                        onOrderTypeChange = { viewModel.setOrderType(it) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    CategoryChipsBar(
                        selectedCategory = selectedCategory,
                        onCategorySelect = { viewModel.selectCategory(it) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val filteredItems = menuItems.filter { it.category == selectedCategory }
                    MenuItemsGrid(
                        items = filteredItems,
                        cartItems = cartItems,
                        onAddItem = { viewModel.addItemToCart(it) },
                        onDecrementItem = { viewModel.decrementItemQuantity(it) },
                        columns = 3
                    )
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                // Cart Pane
                CartPane(
                    cartItems = cartItems,
                    paymentMode = paymentMode,
                    orderType = orderType,
                    onPaymentModeChange = { viewModel.setPaymentMode(it) },
                    onIncrement = { viewModel.addItemToCart(it.menuItem) },
                    onDecrement = { viewModel.decrementItemQuantity(it.menuItem) },
                    onRemove = { viewModel.removeItemFromCart(it.menuItem) },
                    onClearCart = { viewModel.clearCart() },
                    onPrintAction = { choice -> viewModel.processOrderAndPrint(choice) },
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight()
                )
            }
        } else {
            // Compact Phone Layout: Top Order Info, Categories, Items, and Bottom Cart Sheet/Pane
            var showPhoneCartModal by remember { mutableStateOf(false) }

            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    OrderTypeBar(
                        orderType = orderType,
                        onOrderTypeChange = { viewModel.setOrderType(it) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    CategoryChipsBar(
                        selectedCategory = selectedCategory,
                        onCategorySelect = { viewModel.selectCategory(it) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val filteredItems = menuItems.filter { it.category == selectedCategory }
                    MenuItemsGrid(
                        items = filteredItems,
                        cartItems = cartItems,
                        onAddItem = { viewModel.addItemToCart(it) },
                        onDecrementItem = { viewModel.decrementItemQuantity(it) },
                        columns = 2
                    )
                }

                // Quick Cart Bottom Bar
                QuickBottomCartBar(
                    cartItems = cartItems,
                    paymentMode = paymentMode,
                    onPaymentModeChange = { viewModel.setPaymentMode(it) },
                    onOpenFullCart = { showPhoneCartModal = true },
                    onPrintBill = { viewModel.processOrderAndPrint(PosViewModel.PrintChoice.BILL_ONLY) },
                    onPrintBoth = { viewModel.processOrderAndPrint(PosViewModel.PrintChoice.BOTH) }
                )
            }

            if (showPhoneCartModal) {
                AlertDialog(
                    onDismissRequest = { showPhoneCartModal = false },
                    confirmButton = {},
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Current Order Bill", fontWeight = FontWeight.Bold)
                            IconButton(onClick = { showPhoneCartModal = false }) {
                                Icon(Icons.Default.Clear, contentDescription = "Close")
                            }
                        }
                    },
                    text = {
                        CartPane(
                            cartItems = cartItems,
                            paymentMode = paymentMode,
                            orderType = orderType,
                            onPaymentModeChange = { viewModel.setPaymentMode(it) },
                            onIncrement = { viewModel.addItemToCart(it.menuItem) },
                            onDecrement = { viewModel.decrementItemQuantity(it.menuItem) },
                            onRemove = { viewModel.removeItemFromCart(it.menuItem) },
                            onClearCart = { viewModel.clearCart() },
                            onPrintAction = { choice ->
                                showPhoneCartModal = false
                                viewModel.processOrderAndPrint(choice)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(500.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun OrderTypeBar(
    orderType: OrderType,
    onOrderTypeChange: (OrderType) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Dine In Button
            Button(
                onClick = { onOrderTypeChange(OrderType.DINE_IN) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (orderType == OrderType.DINE_IN) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    contentColor = if (orderType == OrderType.DINE_IN) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("dine_in_button")
            ) {
                Icon(Icons.Default.DinnerDining, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("DINE IN", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            // Parcel Button
            Button(
                onClick = { onOrderTypeChange(OrderType.PARCEL) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (orderType == OrderType.PARCEL) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    contentColor = if (orderType == OrderType.PARCEL) {
                        MaterialTheme.colorScheme.onSecondary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("parcel_button")
            ) {
                Icon(Icons.Default.LocalMall, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("PARCEL", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
fun CategoryChipsBar(
    selectedCategory: String,
    onCategorySelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MenuCategories.ALL_DEPARTMENTS.forEach { dept ->
            val isSelected = dept.name == selectedCategory
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier
                    .testTag("category_chip_${dept.name}")
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onCategorySelect(dept.name) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    ) {
                        Text(
                            text = dept.code,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                    Text(
                        text = dept.name,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.3.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun MenuItemsGrid(
    items: List<MenuItem>,
    cartItems: List<CartItem>,
    onAddItem: (MenuItem) -> Unit,
    onDecrementItem: (MenuItem) -> Unit,
    columns: Int = 3
) {
    if (items.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No items in this category",
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.outline)
            )
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items, key = { it.id }) { item ->
            val cartItem = cartItems.find { it.menuItem.id == item.id }
            val inCartQty = cartItem?.quantity ?: 0

            MenuItemCard(
                item = item,
                inCartQty = inCartQty,
                onAdd = { onAddItem(item) },
                onDecrement = { onDecrementItem(item) }
            )
        }
    }
}

@Composable
fun MenuItemCard(
    item: MenuItem,
    inCartQty: Int,
    onAdd: () -> Unit,
    onDecrement: () -> Unit
) {
    val isSelected = inCartQty > 0

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .height(105.dp)
            .testTag("menu_item_${item.id}")
            .clip(RoundedCornerShape(12.dp))
            .clickable { onAdd() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Item Name
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Price & Quantity control
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₹ %.0f".format(item.price),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                if (inCartQty > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .clickable { onDecrement() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                            }
                        }

                        Text(
                            text = inCartQty.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .clickable { onAdd() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .clickable { onAdd() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Add, contentDescription = "Add to cart", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartPane(
    cartItems: List<CartItem>,
    paymentMode: PaymentMode,
    orderType: OrderType,
    onPaymentModeChange: (PaymentMode) -> Unit,
    onIncrement: (CartItem) -> Unit,
    onDecrement: (CartItem) -> Unit,
    onRemove: (CartItem) -> Unit,
    onClearCart: () -> Unit,
    onPrintAction: (PosViewModel.PrintChoice) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalAmount = cartItems.sumOf { it.total }
    val totalItems = cartItems.sumOf { it.quantity }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Cart Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Current Bill",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (orderType == OrderType.DINE_IN) "DINE IN" else "PARCEL",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    )
                }

                if (cartItems.isNotEmpty()) {
                    TextButton(onClick = onClearCart) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Cart Items List
            if (cartItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ShoppingBag,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outlineVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Cart is empty", color = MaterialTheme.colorScheme.outline)
                        Text("Tap menu items to add to bill", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(cartItems, key = { it.menuItem.id }) { item ->
                        CartItemRow(
                            item = item,
                            onIncrement = { onIncrement(item) },
                            onDecrement = { onDecrement(item) },
                            onRemove = { onRemove(item) }
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Bill Total Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Items: $totalItems",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text("GRAND TOTAL", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold))
                    Text(
                        text = "₹ %.0f".format(totalAmount),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Payment Mode Selector
            Text("Payment Mode:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PaymentModeButton(
                    label = "CASH",
                    isSelected = paymentMode == PaymentMode.CASH,
                    selectedColor = CashGreen,
                    modifier = Modifier.weight(1f),
                    onClick = { onPaymentModeChange(PaymentMode.CASH) }
                )
                PaymentModeButton(
                    label = "UPI",
                    isSelected = paymentMode == PaymentMode.UPI,
                    selectedColor = UpiBlue,
                    modifier = Modifier.weight(1f),
                    onClick = { onPaymentModeChange(PaymentMode.UPI) }
                )
                PaymentModeButton(
                    label = "CARD",
                    isSelected = paymentMode == PaymentMode.CARD,
                    selectedColor = CardPurple,
                    modifier = Modifier.weight(1f),
                    onClick = { onPaymentModeChange(PaymentMode.CARD) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Print Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Print Bill Button
                Button(
                    onClick = { onPrintAction(PosViewModel.PrintChoice.BILL_ONLY) },
                    enabled = cartItems.isNotEmpty(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("print_bill_button")
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PRINT BILL", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Print Token Button (KOT)
                Button(
                    onClick = { onPrintAction(PosViewModel.PrintChoice.TOKEN_ONLY) },
                    enabled = cartItems.isNotEmpty(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("print_token_button")
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("KOT TOKEN", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // BILL + TOKEN Combined Action Button
            Button(
                onClick = { onPrintAction(PosViewModel.PrintChoice.BOTH) },
                enabled = cartItems.isNotEmpty(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LeafGreenDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("bill_and_token_button")
            ) {
                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("BILL + TOKEN (PRINT BOTH)", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            }
        }
    }
}

val LeafGreenDark = Color(0xFF1B5E20)

@Composable
fun CartItemRow(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.menuItem.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "₹ %.0f each".format(item.menuItem.price),
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .clickable { onDecrement() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Remove, contentDescription = "Minus", modifier = Modifier.size(14.dp))
                    }
                }

                Text(
                    text = item.quantity.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .clickable { onIncrement() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, contentDescription = "Plus", modifier = Modifier.size(14.dp))
                    }
                }

                Text(
                    text = "₹ %.0f".format(item.total),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.width(60.dp),
                    textAlign = TextAlign.End
                )

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentModeButton(
    label: String,
    isSelected: Boolean,
    selectedColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) selectedColor else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .height(40.dp)
            .testTag("payment_mode_$label")
    ) {
        Text(
            text = label,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.sp
        )
    }
}

@Composable
fun QuickBottomCartBar(
    cartItems: List<CartItem>,
    paymentMode: PaymentMode,
    onPaymentModeChange: (PaymentMode) -> Unit,
    onOpenFullCart: () -> Unit,
    onPrintBill: () -> Unit,
    onPrintBoth: () -> Unit
) {
    val totalAmount = cartItems.sumOf { it.total }
    val totalItems = cartItems.sumOf { it.quantity }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.clickable { onOpenFullCart() }
                ) {
                    Text(
                        text = "$totalItems Items • Tap to view bill",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Total: ₹ %.0f".format(totalAmount),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onPrintBill,
                        enabled = cartItems.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text("BILL", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onPrintBoth,
                        enabled = cartItems.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = LeafGreenDark),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text("BILL+TOKEN", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
