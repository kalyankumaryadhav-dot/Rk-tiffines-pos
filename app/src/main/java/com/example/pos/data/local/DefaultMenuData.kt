package com.example.pos.data.local

import com.example.pos.model.MenuCategories

object DefaultMenuData {
    val INITIAL_MENU_ITEMS = listOf(
        // 1. SOUTH INDIAN
        MenuItemEntity(name = "Idli (2 Pcs)", category = MenuCategories.SOUTH_INDIAN, price = 40.0, sortOrder = 1),
        MenuItemEntity(name = "Vada (2 Pcs)", category = MenuCategories.SOUTH_INDIAN, price = 50.0, sortOrder = 2),
        MenuItemEntity(name = "Idli (1) + Vada (1)", category = MenuCategories.SOUTH_INDIAN, price = 45.0, sortOrder = 3),
        MenuItemEntity(name = "Poori (2 Pcs)", category = MenuCategories.SOUTH_INDIAN, price = 50.0, sortOrder = 4),
        MenuItemEntity(name = "Upma", category = MenuCategories.SOUTH_INDIAN, price = 40.0, sortOrder = 5),
        MenuItemEntity(name = "Tomato Bath", category = MenuCategories.SOUTH_INDIAN, price = 45.0, sortOrder = 6),
        MenuItemEntity(name = "Ven Pongal", category = MenuCategories.SOUTH_INDIAN, price = 50.0, sortOrder = 7),
        MenuItemEntity(name = "Filter Coffee", category = MenuCategories.SOUTH_INDIAN, price = 25.0, sortOrder = 8),
        MenuItemEntity(name = "Special Tea", category = MenuCategories.SOUTH_INDIAN, price = 20.0, sortOrder = 9),
        MenuItemEntity(name = "Badam Milk", category = MenuCategories.SOUTH_INDIAN, price = 30.0, sortOrder = 10),

        // 2. NORMAL DOSA
        MenuItemEntity(name = "Plain Dosa", category = MenuCategories.NORMAL_DOSA, price = 50.0, sortOrder = 1),
        MenuItemEntity(name = "Masala Dosa", category = MenuCategories.NORMAL_DOSA, price = 60.0, sortOrder = 2),
        MenuItemEntity(name = "Onion Dosa", category = MenuCategories.NORMAL_DOSA, price = 60.0, sortOrder = 3),
        MenuItemEntity(name = "Karam Dosa", category = MenuCategories.NORMAL_DOSA, price = 60.0, sortOrder = 4),
        MenuItemEntity(name = "Egg Dosa", category = MenuCategories.NORMAL_DOSA, price = 70.0, sortOrder = 5),
        MenuItemEntity(name = "Rava Dosa", category = MenuCategories.NORMAL_DOSA, price = 65.0, sortOrder = 6),
        MenuItemEntity(name = "Onion Rava Dosa", category = MenuCategories.NORMAL_DOSA, price = 75.0, sortOrder = 7),

        // 3. PESARATTU
        MenuItemEntity(name = "Plain Pesarattu", category = MenuCategories.PESARATTU, price = 60.0, sortOrder = 1),
        MenuItemEntity(name = "Onion Pesarattu", category = MenuCategories.PESARATTU, price = 70.0, sortOrder = 2),
        MenuItemEntity(name = "Upma Pesarattu (MLA)", category = MenuCategories.PESARATTU, price = 80.0, sortOrder = 3),
        MenuItemEntity(name = "Ghee Pesarattu", category = MenuCategories.PESARATTU, price = 80.0, sortOrder = 4),
        MenuItemEntity(name = "Ghee Onion Pesarattu", category = MenuCategories.PESARATTU, price = 90.0, sortOrder = 5),

        // 4. GHEE IDLI
        MenuItemEntity(name = "Ghee Sambar Idli (2 Pcs)", category = MenuCategories.GHEE_IDLI, price = 60.0, sortOrder = 1),
        MenuItemEntity(name = "Ghee Karam Button Idli", category = MenuCategories.GHEE_IDLI, price = 70.0, sortOrder = 2),
        MenuItemEntity(name = "Ghee Podi Idli (Thatte)", category = MenuCategories.GHEE_IDLI, price = 65.0, sortOrder = 3),
        MenuItemEntity(name = "Ghee Fried Idli", category = MenuCategories.GHEE_IDLI, price = 75.0, sortOrder = 4),

        // 5. GHEE DOSA
        MenuItemEntity(name = "Ghee Plain Dosa", category = MenuCategories.GHEE_DOSA, price = 70.0, sortOrder = 1),
        MenuItemEntity(name = "Ghee Karam Dosa", category = MenuCategories.GHEE_DOSA, price = 75.0, sortOrder = 2),
        MenuItemEntity(name = "Ghee Masala Dosa", category = MenuCategories.GHEE_DOSA, price = 80.0, sortOrder = 3),
        MenuItemEntity(name = "Ghee Onion Dosa", category = MenuCategories.GHEE_DOSA, price = 80.0, sortOrder = 4),
        MenuItemEntity(name = "Ghee Roast Dosa", category = MenuCategories.GHEE_DOSA, price = 85.0, sortOrder = 5),

        // 6. BUTTER DOSA
        MenuItemEntity(name = "Butter Plain Dosa", category = MenuCategories.BUTTER_DOSA, price = 70.0, sortOrder = 1),
        MenuItemEntity(name = "Butter Masala Dosa", category = MenuCategories.BUTTER_DOSA, price = 80.0, sortOrder = 2),
        MenuItemEntity(name = "Butter Karam Dosa", category = MenuCategories.BUTTER_DOSA, price = 80.0, sortOrder = 3),
        MenuItemEntity(name = "Butter Onion Dosa", category = MenuCategories.BUTTER_DOSA, price = 80.0, sortOrder = 4),

        // 7. CHEESE DOSA
        MenuItemEntity(name = "Cheese Plain Dosa", category = MenuCategories.CHEESE_DOSA, price = 80.0, sortOrder = 1),
        MenuItemEntity(name = "Cheese Masala Dosa", category = MenuCategories.CHEESE_DOSA, price = 90.0, sortOrder = 2),
        MenuItemEntity(name = "Cheese Corn Dosa", category = MenuCategories.CHEESE_DOSA, price = 95.0, sortOrder = 3),
        MenuItemEntity(name = "Cheese Burst Dosa", category = MenuCategories.CHEESE_DOSA, price = 110.0, sortOrder = 4),

        // 8. PIZZA DOSA
        MenuItemEntity(name = "Classic Pizza Dosa", category = MenuCategories.PIZZA_DOSA, price = 99.0, sortOrder = 1),
        MenuItemEntity(name = "Veggie Delight Pizza Dosa", category = MenuCategories.PIZZA_DOSA, price = 110.0, sortOrder = 2),
        MenuItemEntity(name = "Cheese Corn Pizza Dosa", category = MenuCategories.PIZZA_DOSA, price = 120.0, sortOrder = 3),
        MenuItemEntity(name = "Special Paneer Pizza Dosa", category = MenuCategories.PIZZA_DOSA, price = 130.0, sortOrder = 4),

        // 9. PANNER DOSA
        MenuItemEntity(name = "Paneer Dosa", category = MenuCategories.PANNER_DOSA, price = 85.0, sortOrder = 1),
        MenuItemEntity(name = "Paneer Masala Dosa", category = MenuCategories.PANNER_DOSA, price = 95.0, sortOrder = 2),
        MenuItemEntity(name = "Butter Paneer Dosa", category = MenuCategories.PANNER_DOSA, price = 105.0, sortOrder = 3),
        MenuItemEntity(name = "Ghee Paneer Dosa", category = MenuCategories.PANNER_DOSA, price = 110.0, sortOrder = 4),
        MenuItemEntity(name = "Spicy Schezwan Paneer Dosa", category = MenuCategories.PANNER_DOSA, price = 115.0, sortOrder = 5),

        // 10. DOSA ITEMS
        MenuItemEntity(name = "Set Dosa (3 Pcs)", category = MenuCategories.DOSA_ITEMS, price = 60.0, sortOrder = 1),
        MenuItemEntity(name = "Spring Dosa", category = MenuCategories.DOSA_ITEMS, price = 85.0, sortOrder = 2),
        MenuItemEntity(name = "Schezwan Dosa", category = MenuCategories.DOSA_ITEMS, price = 75.0, sortOrder = 3),
        MenuItemEntity(name = "Chocolate Dosa", category = MenuCategories.DOSA_ITEMS, price = 70.0, sortOrder = 4),
        MenuItemEntity(name = "Mysore Masala Dosa", category = MenuCategories.DOSA_ITEMS, price = 75.0, sortOrder = 5),

        // 11. MAGGI
        MenuItemEntity(name = "Plain Maggi", category = MenuCategories.MAGGI, price = 45.0, sortOrder = 1),
        MenuItemEntity(name = "Veg Maggi", category = MenuCategories.MAGGI, price = 60.0, sortOrder = 2),
        MenuItemEntity(name = "Butter Maggi", category = MenuCategories.MAGGI, price = 65.0, sortOrder = 3),
        MenuItemEntity(name = "Cheese Maggi", category = MenuCategories.MAGGI, price = 75.0, sortOrder = 4),
        MenuItemEntity(name = "Paneer Maggi", category = MenuCategories.MAGGI, price = 80.0, sortOrder = 5),
        MenuItemEntity(name = "Egg Maggi", category = MenuCategories.MAGGI, price = 70.0, sortOrder = 6),

        // 12. SNACKS
        MenuItemEntity(name = "Mirchi Bajji (4 Pcs)", category = MenuCategories.SNACKS, price = 40.0, sortOrder = 1),
        MenuItemEntity(name = "Mysore Bonda (4 Pcs)", category = MenuCategories.SNACKS, price = 45.0, sortOrder = 2),
        MenuItemEntity(name = "Onion Pakoda", category = MenuCategories.SNACKS, price = 40.0, sortOrder = 3),
        MenuItemEntity(name = "Punugulu", category = MenuCategories.SNACKS, price = 40.0, sortOrder = 4),
        MenuItemEntity(name = "Samosa (2 Pcs)", category = MenuCategories.SNACKS, price = 30.0, sortOrder = 5),
        MenuItemEntity(name = "Bread Omelette", category = MenuCategories.SNACKS, price = 50.0, sortOrder = 6)
    )
}
