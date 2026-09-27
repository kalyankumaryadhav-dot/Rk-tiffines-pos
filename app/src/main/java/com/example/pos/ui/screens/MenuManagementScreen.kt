package com.example.pos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.pos.model.DepartmentInfo
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
    val departments by viewModel.departments.collectAsState()
    var selectedCategoryIndex by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val safeCategoryIndex = if (departments.isNotEmpty()) {
        selectedCategoryIndex.coerceIn(0, departments.size - 1)
    } else 0
    val currentDepartment = departments.getOrNull(safeCategoryIndex)
    val currentCategory = currentDepartment?.name ?: ""

    // Department Management Dialog States
    var showEditDepartmentsModal by remember { mutableStateOf(false) }
    var showAddDepartmentDialog by remember { mutableStateOf(false) }
    var editingDepartment by remember { mutableStateOf<DepartmentInfo?>(null) }
    var deletingDepartment by remember { mutableStateOf<DepartmentInfo?>(null) }

    // Item Management Dialog States
    var showAddItemDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<MenuItem?>(null) }
    var deletingItem by remember { mutableStateOf<MenuItem?>(null) }

    // Instant Search Logic: filters across item name and department name
    val isSearching = searchQuery.isNotBlank()
    val searchFilteredItems = remember(menuItems, searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else {
            val query = searchQuery.trim().lowercase()
            menuItems.filter { item ->
                item.name.lowercase().contains(query) || item.category.lowercase().contains(query)
            }
        }
    }

    val departmentFilteredItems = remember(menuItems, currentCategory) {
        menuItems.filter { it.category.equals(currentCategory, ignoreCase = true) }
    }

    val displayedItems = if (isSearching) searchFilteredItems else departmentFilteredItems
    val categoryNames = departments.map { it.name }

    Scaffold(
        containerColor = RkBlackBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddItemDialog = true },
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
            // Header: Title and item count info (Reload 115 Items button is completely removed)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Menu & Department Management",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = RkTextPrimary
                    )
                )
                Text(
                    text = "${departments.size} Departments • ${menuItems.size} items in RK TIFFINES menu",
                    style = MaterialTheme.typography.bodySmall.copy(color = RkTextSecondary)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar: Search across item name and department name with instant results and clear (X) button
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text("Search menu items or departments…", color = RkTextMuted, fontSize = 13.sp)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = RkGoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.testTag("clear_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = RkTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RkGoldPrimary,
                    unfocusedBorderColor = RkBorderGoldSubtle,
                    focusedTextColor = RkTextPrimary,
                    unfocusedTextColor = RkTextPrimary,
                    cursorColor = RkGoldPrimary,
                    focusedContainerColor = RkSurfaceDark,
                    unfocusedContainerColor = RkSurfaceDark
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("menu_search_field")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Edit Departments Button Option
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { showEditDepartmentsModal = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RkSurfaceDark,
                        contentColor = RkGoldPrimary
                    ),
                    border = BorderStroke(1.dp, RkGoldPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("edit_departments_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = RkGoldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "✏ Edit Departments",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = RkGoldPrimary
                    )
                }

                if (isSearching) {
                    Text(
                        text = "${displayedItems.size} match(es)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = RkYellowBright,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scrollable Category / Department Tabs (visible when not searching)
            if (!isSearching && departments.isNotEmpty()) {
                ScrollableTabRow(
                    selectedTabIndex = safeCategoryIndex,
                    containerColor = RkSurfaceDark,
                    contentColor = RkGoldPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(BorderStroke(1.dp, RkBorderGoldSubtle), RoundedCornerShape(10.dp)),
                    edgePadding = 8.dp,
                    indicator = { tabPositions ->
                        if (safeCategoryIndex < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[safeCategoryIndex]),
                                color = RkGoldPrimary
                            )
                        }
                    }
                ) {
                    departments.forEachIndexed { index, dept ->
                        val isSelected = safeCategoryIndex == index
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
            }

            // Items count info / Search header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSearching) {
                    Text(
                        text = "Results for \"$searchQuery\" (${displayedItems.size} items)",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = RkGoldPrimary
                        )
                    )
                } else {
                    Text(
                        text = "$currentCategory (${displayedItems.size} items)",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = RkGoldPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Menu Items List
            if (displayedItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isSearching) "No menu items or departments match \"$searchQuery\""
                        else "No items yet in $currentCategory. Tap '+ Add Item' below to add.",
                        color = RkTextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayedItems, key = { it.id }) { item ->
                        MenuItemManagementRow(
                            item = item,
                            showCategoryBadge = isSearching,
                            onToggleAvailable = { viewModel.toggleItemAvailability(item) },
                            onEdit = { editingItem = item },
                            onDelete = { deletingItem = item }
                        )
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Department Management Dialog
    // -------------------------------------------------------------------------
    if (showEditDepartmentsModal) {
        DepartmentManagementDialog(
            departments = departments,
            menuItems = menuItems,
            onDismiss = { showEditDepartmentsModal = false },
            onAddClick = { showAddDepartmentDialog = true },
            onEditDepartment = { dept -> editingDepartment = dept },
            onDeleteDepartment = { dept -> deletingDepartment = dept },
            onMoveUp = { index ->
                if (index > 0) {
                    val list = departments.toMutableList()
                    val item = list.removeAt(index)
                    list.add(index - 1, item)
                    viewModel.reorderDepartments(list)
                }
            },
            onMoveDown = { index ->
                if (index < departments.size - 1) {
                    val list = departments.toMutableList()
                    val item = list.removeAt(index)
                    list.add(index + 1, item)
                    viewModel.reorderDepartments(list)
                }
            }
        )
    }

    // Add Department Dialog
    if (showAddDepartmentDialog) {
        DepartmentEditDialog(
            title = "Add New Department",
            initialName = "",
            initialCode = "",
            onDismiss = { showAddDepartmentDialog = false },
            onConfirm = { name, code ->
                viewModel.addDepartment(name, code)
                showAddDepartmentDialog = false
            }
        )
    }

    // Edit Department Dialog
    editingDepartment?.let { dept ->
        DepartmentEditDialog(
            title = "Edit Department",
            initialName = dept.name,
            initialCode = dept.code,
            onDismiss = { editingDepartment = null },
            onConfirm = { newName, newCode ->
                viewModel.renameDepartment(dept.name, newName, newCode)
                editingDepartment = null
            }
        )
    }

    // Delete Department Confirmation Dialog
    deletingDepartment?.let { dept ->
        val assignedItemCount = menuItems.count { it.category.equals(dept.name, ignoreCase = true) }
        AlertDialog(
            onDismissRequest = { deletingDepartment = null },
            containerColor = RkSurfaceElevated,
            titleContentColor = RkTextPrimary,
            textContentColor = RkTextPrimary,
            title = {
                Text("Delete Department \"${dept.name}\"?", fontWeight = FontWeight.Bold, color = RkTextPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Are you sure you want to remove the \"${dept.name}\" department?",
                        color = RkTextSecondary
                    )
                    if (assignedItemCount > 0) {
                        Text(
                            text = "Note: $assignedItemCount existing menu items will be preserved safely in the database.",
                            color = RkYellowBright,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDepartment(dept)
                        deletingDepartment = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrinterErrorRed, contentColor = Color.White)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingDepartment = null }) {
                    Text("Cancel", color = RkTextSecondary)
                }
            }
        )
    }

    // -------------------------------------------------------------------------
    // Item Management Dialogs
    // -------------------------------------------------------------------------
    if (showAddItemDialog) {
        MenuItemEditDialog(
            title = "Add New Item to Menu",
            initialCategory = currentCategory.ifBlank { categoryNames.firstOrNull() ?: "SOUTH INDIAN" },
            categories = categoryNames,
            onDismiss = { showAddItemDialog = false },
            onConfirm = { name, cat, price ->
                viewModel.addMenuItem(name, cat, price)
                showAddItemDialog = false
            }
        )
    }

    editingItem?.let { item ->
        MenuItemEditDialog(
            title = "Edit Menu Item",
            initialName = item.name,
            initialCategory = item.category,
            initialPrice = item.price.toString(),
            categories = categoryNames,
            onDismiss = { editingItem = null },
            onConfirm = { name, cat, price ->
                viewModel.updateMenuItem(item.copy(name = name, category = cat, price = price))
                editingItem = null
            }
        )
    }

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

// -----------------------------------------------------------------------------
// Department Management Dialog
// -----------------------------------------------------------------------------
@Composable
fun DepartmentManagementDialog(
    departments: List<DepartmentInfo>,
    menuItems: List<MenuItem>,
    onDismiss: () -> Unit,
    onAddClick: () -> Unit,
    onEditDepartment: (DepartmentInfo) -> Unit,
    onDeleteDepartment: (DepartmentInfo) -> Unit,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = RkSurfaceElevated,
            border = BorderStroke(1.dp, RkBorderGoldSubtle),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 600.dp)
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "✏ Edit Departments",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = RkTextPrimary
                            )
                        )
                        Text(
                            text = "Manage department names, reorder positions, or add categories",
                            style = MaterialTheme.typography.bodySmall.copy(color = RkTextSecondary)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = RkTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Add Department Button
                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RkGoldPrimary,
                        contentColor = RkTextOnGold
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_department_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = RkTextOnGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+ Add New Department", fontWeight = FontWeight.Bold, color = RkTextOnGold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Departments List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(departments, key = { _, dept -> dept.id }) { index, dept ->
                        val itemCount = menuItems.count { it.category.equals(dept.name, ignoreCase = true) }
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = RkSurfaceDark),
                            border = BorderStroke(1.dp, RkBorderGoldSubtle),
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
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Position tag
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = RkSurfaceVariantDark,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${index + 1}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = RkTextSecondary
                                            )
                                        }
                                    }

                                    // Code badge
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = RkGoldPrimary
                                    ) {
                                        Text(
                                            text = dept.code,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RkTextOnGold
                                        )
                                    }

                                    // Department Name & items count
                                    Column {
                                        Text(
                                            text = dept.name,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = RkTextPrimary
                                            )
                                        )
                                        Text(
                                            text = "$itemCount items assigned",
                                            style = MaterialTheme.typography.labelSmall.copy(color = RkTextSecondary)
                                        )
                                    }
                                }

                                // Action Buttons: Move Up, Move Down, Edit, Delete
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    IconButton(
                                        onClick = { onMoveUp(index) },
                                        enabled = index > 0,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowUpward,
                                            contentDescription = "Move Up",
                                            tint = if (index > 0) RkGoldPrimary else RkTextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onMoveDown(index) },
                                        enabled = index < departments.size - 1,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDownward,
                                            contentDescription = "Move Down",
                                            tint = if (index < departments.size - 1) RkGoldPrimary else RkTextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onEditDepartment(dept) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Department",
                                            tint = RkGoldPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDeleteDepartment(dept) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Department",
                                            tint = PrinterErrorRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Done Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RkSurfaceDark,
                        contentColor = RkGoldPrimary
                    ),
                    border = BorderStroke(1.dp, RkBorderGoldSubtle),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done", fontWeight = FontWeight.Bold, color = RkGoldPrimary)
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Department Add / Edit Dialog
// -----------------------------------------------------------------------------
@Composable
fun DepartmentEditDialog(
    title: String,
    initialName: String = "",
    initialCode: String = "",
    onDismiss: () -> Unit,
    onConfirm: (name: String, code: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var code by remember { mutableStateOf(initialCode) }
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
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Department Name (e.g. DOSA ITEMS, MAGGI)") },
                    singleLine = true,
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Short Code (e.g. DI, MG) - Optional") },
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
                        errorMessage = "Please enter a department name"
                        return@Button
                    }
                    onConfirm(name.trim().uppercase(), code.trim().uppercase())
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

// -----------------------------------------------------------------------------
// Menu Item Row Component
// -----------------------------------------------------------------------------
@Composable
fun MenuItemManagementRow(
    item: MenuItem,
    showCategoryBadge: Boolean = false,
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = RkTextPrimary
                        )
                    )
                    if (showCategoryBadge) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = RkSurfaceVariantDark,
                            border = BorderStroke(1.dp, RkBorderGoldSubtle)
                        ) {
                            Text(
                                text = item.category,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = RkGoldPrimary
                            )
                        }
                    }
                }
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

// -----------------------------------------------------------------------------
// Menu Item Add / Edit Dialog
// -----------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemEditDialog(
    title: String,
    initialName: String = "",
    initialCategory: String,
    initialPrice: String = "",
    categories: List<String>,
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

                // Category Selector (using dynamic departments)
                ExposedDropdownMenuBox(
                    expanded = isCategoryDropdownExpanded,
                    onExpandedChange = { isCategoryDropdownExpanded = !isCategoryDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Department / Category") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownExpanded)
                        },
                        colors = textFieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = isCategoryDropdownExpanded,
                        onDismissRequest = { isCategoryDropdownExpanded = false }
                    ) {
                        categories.forEach { cat ->
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
