package com.example.pos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pos.model.MenuCategories
import com.example.pos.model.MenuItem
import com.example.pos.ui.PosViewModel
import com.example.ui.theme.PrinterConnectedGreen
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

@Composable
fun MenuManagementScreen(
    viewModel: PosViewModel,
    menuItems: List<MenuItem>,
    modifier: Modifier = Modifier
) {
    var selectedCategoryIndex by remember { mutableStateOf(0) }
    val categories = MenuCategories.ALL_CATEGORIES
    val currentCategory = categories[selectedCategoryIndex]

    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<MenuItem?>(null) }
    var deletingItem by remember { mutableStateOf<MenuItem?>(null) }

    val filteredItems = menuItems.filter { it.category == currentCategory }

    Scaffold(
        containerColor = RkBlackBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = RkGoldPrimary,
                contentColor = RkTextOnGold,
                modifier = Modifier.testTag("add_menu_item_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = RkTextOnGold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Item", fontWeight = FontWeight.Bold, color = RkTextOnGold)
                }
            }
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Menu & Department Management",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = RkTextPrimary)
                    )
                    Text(
                        text = "12 Departments • ${menuItems.size} items in RK TIFFINES menu",
                        style = MaterialTheme.typography.bodySmall.copy(color = RkTextSecondary)
                    )
                }
                OutlinedButton(
                    onClick = { viewModel.resetToDefaultDepartmentsAndMenu() },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, RkBorderGoldSubtle)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = RkGoldPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reload 115 Items", fontSize = 12.sp, color = RkGoldPrimary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scrollable Category / Department Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedCategoryIndex,
                containerColor = RkSurfaceDark,
                contentColor = RkGoldPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(BorderStroke(1.dp, RkBorderGoldSubtle), RoundedCornerShape(10.dp)),
                edgePadding = 8.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedCategoryIndex]),
                        color = RkGoldPrimary
                    )
                }
            ) {
                MenuCategories.ALL_DEPARTMENTS.forEachIndexed { index, dept ->
                    val isSelected = selectedCategoryIndex == index
                    Tab(
                        selected = isSelected,
                        onClick = { selectedCategoryIndex = index },
                        selectedContentColor = RkGoldPrimary,
                        unselectedContentColor = RkTextSecondary,
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isSelected) RkGoldPrimary else RkSurfaceVariantDark,
                                    border = if (!isSelected) BorderStroke(1.dp, RkBorderGoldSubtle) else null
                                ) {
                                    Text(
                                        text = dept.code,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) RkTextOnGold else RkGoldPrimary
                                    )
                                }
                                Text(
                                    text = dept.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) RkGoldPrimary else RkTextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Items count info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$currentCategory (${filteredItems.size} items)",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = RkGoldPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Menu Items List
            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No items yet in $currentCategory. Tap '+ Add Item' below to add.",
                        color = RkTextSecondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        MenuItemManagementRow(
                            item = item,
                            onToggleAvailable = { viewModel.toggleItemAvailability(item) },
                            onEdit = { editingItem = item },
                            onDelete = { deletingItem = item }
                        )
                    }
                }
            }
        }
    }

    // Add Item Dialog
    if (showAddDialog) {
        MenuItemEditDialog(
            title = "Add New Item to Menu",
            initialCategory = currentCategory,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, cat, price ->
                viewModel.addMenuItem(name, cat, price)
                showAddDialog = false
            }
        )
    }

    // Edit Item Dialog
    editingItem?.let { item ->
        MenuItemEditDialog(
            title = "Edit Menu Item",
            initialName = item.name,
            initialCategory = item.category,
            initialPrice = item.price.toString(),
            onDismiss = { editingItem = null },
            onConfirm = { name, cat, price ->
                viewModel.updateMenuItem(item.copy(name = name, category = cat, price = price))
                editingItem = null
            }
        )
    }

    // Delete Confirmation Dialog
    deletingItem?.let { item ->
        AlertDialog(
            onDismissRequest = { deletingItem = null },
            containerColor = RkSurfaceElevated,
            titleContentColor = RkTextPrimary,
            textContentColor = RkTextPrimary,
            title = { Text("Delete Menu Item?", fontWeight = FontWeight.Bold, color = RkTextPrimary) },
            text = { Text("Are you sure you want to remove \"${item.name}\" from the menu?", color = RkTextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMenuItem(item.id)
                        deletingItem = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrinterErrorRed, contentColor = Color.White)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingItem = null }) {
                    Text("Cancel", color = RkTextSecondary)
                }
            }
        )
    }
}

@Composable
fun MenuItemManagementRow(
    item: MenuItem,
    onToggleAvailable: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = RkSurfaceDark),
        border = BorderStroke(1.dp, RkBorderGoldSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = RkTextPrimary)
                )
                Text(
                    text = "Rate: ₹ %.0f".format(item.price),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = RkYellowBright
                    )
                )
                Text(
                    text = if (item.isAvailable) "In Stock (Available for billing)" else "Out of Stock (Hidden)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (item.isAvailable) PrinterConnectedGreen else PrinterErrorRed
                    )
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Switch(
                    checked = item.isAvailable,
                    onCheckedChange = { onToggleAvailable() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = RkTextOnGold,
                        checkedTrackColor = RkGoldPrimary,
                        uncheckedThumbColor = RkTextSecondary,
                        uncheckedTrackColor = RkSurfaceVariantDark
                    )
                )

                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Item", tint = RkGoldPrimary)
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Item", tint = PrinterErrorRed)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemEditDialog(
    title: String,
    initialName: String = "",
    initialCategory: String,
    initialPrice: String = "",
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: String, price: Double) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var priceText by remember { mutableStateOf(initialPrice) }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
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

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RkSurfaceElevated,
        titleContentColor = RkTextPrimary,
        textContentColor = RkTextPrimary,
        title = { Text(title, fontWeight = FontWeight.Bold, color = RkTextPrimary) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Item Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name") },
                    singleLine = true,
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Selector
                ExposedDropdownMenuBox(
                    expanded = isCategoryDropdownExpanded,
                    onExpandedChange = { isCategoryDropdownExpanded = !isCategoryDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownExpanded) },
                        colors = textFieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = isCategoryDropdownExpanded,
                        onDismissRequest = { isCategoryDropdownExpanded = false }
                    ) {
                        MenuCategories.ALL_CATEGORIES.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, color = RkTextPrimary) },
                                onClick = {
                                    selectedCategory = cat
                                    isCategoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Price (₹)
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Price (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(errorMessage ?: "", color = PrinterErrorRed, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Please enter an item name"
                        return@Button
                    }
                    val price = priceText.toDoubleOrNull()
                    if (price == null || price < 0) {
                        errorMessage = "Please enter a valid price"
                        return@Button
                    }
                    onConfirm(name.trim(), selectedCategory, price)
                },
                colors = ButtonDefaults.buttonColors(containerColor = RkGoldPrimary, contentColor = RkTextOnGold)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = RkTextSecondary)
            }
        }
    )
}
