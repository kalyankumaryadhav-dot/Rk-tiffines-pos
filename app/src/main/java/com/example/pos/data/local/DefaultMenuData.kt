package com.example.pos.data.local

import com.example.pos.model.MenuCategories

object DefaultMenuData {
    val INITIAL_MENU_ITEMS = listOf(

        // Dept 1: SOUTH INDIAN (25 items)
        MenuItemEntity(name = "IDLI(4)", category = MenuCategories.SOUTH_INDIAN, price = 45.0, sortOrder = 1),
        MenuItemEntity(name = "VADA(1)", category = MenuCategories.SOUTH_INDIAN, price = 20.0, sortOrder = 2),
        MenuItemEntity(name = "POORI(2)", category = MenuCategories.SOUTH_INDIAN, price = 45.0, sortOrder = 3),
        MenuItemEntity(name = "PUNUGU(4)", category = MenuCategories.SOUTH_INDIAN, price = 45.0, sortOrder = 4),
        MenuItemEntity(name = "CHINNA PUNUGU(15)", category = MenuCategories.SOUTH_INDIAN, price = 35.0, sortOrder = 5),
        MenuItemEntity(name = "MYSORE BONDA(4)", category = MenuCategories.SOUTH_INDIAN, price = 45.0, sortOrder = 6),
        MenuItemEntity(name = "IDLI(2)VADA1", category = MenuCategories.SOUTH_INDIAN, price = 40.0, sortOrder = 78),
        MenuItemEntity(name = "IDLI(3)VADA1", category = MenuCategories.SOUTH_INDIAN, price = 50.0, sortOrder = 79),
        MenuItemEntity(name = "IDLI(3)", category = MenuCategories.SOUTH_INDIAN, price = 35.0, sortOrder = 80),
        MenuItemEntity(name = "BONDA(3)", category = MenuCategories.SOUTH_INDIAN, price = 35.0, sortOrder = 81),
        MenuItemEntity(name = "PUNUGU(3)", category = MenuCategories.SOUTH_INDIAN, price = 35.0, sortOrder = 82),
        MenuItemEntity(name = "CHOLE BATHURE", category = MenuCategories.SOUTH_INDIAN, price = 55.0, sortOrder = 83),
        MenuItemEntity(name = "IDLI KARAM PODI", category = MenuCategories.SOUTH_INDIAN, price = 50.0, sortOrder = 91),
        MenuItemEntity(name = "POORI(1)", category = MenuCategories.SOUTH_INDIAN, price = 25.0, sortOrder = 93),
        MenuItemEntity(name = "IDLI(1)", category = MenuCategories.SOUTH_INDIAN, price = 12.0, sortOrder = 94),
        MenuItemEntity(name = "PUNUGU(1)", category = MenuCategories.SOUTH_INDIAN, price = 12.0, sortOrder = 95),
        MenuItemEntity(name = "MYSORE BONDA(1)", category = MenuCategories.SOUTH_INDIAN, price = 12.0, sortOrder = 96),
        MenuItemEntity(name = "BONDA2", category = MenuCategories.SOUTH_INDIAN, price = 25.0, sortOrder = 101),
        MenuItemEntity(name = "PUNUGU2", category = MenuCategories.SOUTH_INDIAN, price = 25.0, sortOrder = 102),
        MenuItemEntity(name = "IDLI(2)", category = MenuCategories.SOUTH_INDIAN, price = 25.0, sortOrder = 103),
        MenuItemEntity(name = "LITER WATER", category = MenuCategories.SOUTH_INDIAN, price = 20.0, sortOrder = 104),
        MenuItemEntity(name = "HALF WATER", category = MenuCategories.SOUTH_INDIAN, price = 10.0, sortOrder = 105),
        MenuItemEntity(name = "IDLI(5)", category = MenuCategories.SOUTH_INDIAN, price = 60.0, sortOrder = 106),
        MenuItemEntity(name = "CHINNA PUNUGU(18)", category = MenuCategories.SOUTH_INDIAN, price = 40.0, sortOrder = 114),
        MenuItemEntity(name = "CHINNA PUNUGU(23)", category = MenuCategories.SOUTH_INDIAN, price = 50.0, sortOrder = 115),

        // Dept 2: NORMAL DOSA (18 items)
        MenuItemEntity(name = "PLAIN DOSA", category = MenuCategories.NORMAL_DOSA, price = 35.0, sortOrder = 7),
        MenuItemEntity(name = "KARAM DOSA", category = MenuCategories.NORMAL_DOSA, price = 40.0, sortOrder = 8),
        MenuItemEntity(name = "KARAM TOPI DOSA", category = MenuCategories.NORMAL_DOSA, price = 45.0, sortOrder = 9),
        MenuItemEntity(name = "PAPER DOSA", category = MenuCategories.NORMAL_DOSA, price = 60.0, sortOrder = 10),
        MenuItemEntity(name = "MASALA DOSA", category = MenuCategories.NORMAL_DOSA, price = 50.0, sortOrder = 11),
        MenuItemEntity(name = "ONION DOSA", category = MenuCategories.NORMAL_DOSA, price = 55.0, sortOrder = 12),
        MenuItemEntity(name = "UPMA DOSA", category = MenuCategories.NORMAL_DOSA, price = 50.0, sortOrder = 13),
        MenuItemEntity(name = "EGG DOSA", category = MenuCategories.NORMAL_DOSA, price = 50.0, sortOrder = 14),
        MenuItemEntity(name = "TOMATO DOSA", category = MenuCategories.NORMAL_DOSA, price = 60.0, sortOrder = 15),
        MenuItemEntity(name = "SET DOSA", category = MenuCategories.NORMAL_DOSA, price = 65.0, sortOrder = 16),
        MenuItemEntity(name = "VEGETABLE DOSA", category = MenuCategories.NORMAL_DOSA, price = 75.0, sortOrder = 17),
        MenuItemEntity(name = "UTTAPPAM", category = MenuCategories.NORMAL_DOSA, price = 65.0, sortOrder = 18),
        MenuItemEntity(name = "MASALA ONION", category = MenuCategories.NORMAL_DOSA, price = 60.0, sortOrder = 19),
        MenuItemEntity(name = "EGG MASALA", category = MenuCategories.NORMAL_DOSA, price = 60.0, sortOrder = 20),
        MenuItemEntity(name = "DOUBLE EGG DOSA", category = MenuCategories.NORMAL_DOSA, price = 60.0, sortOrder = 21),
        MenuItemEntity(name = "CARROT ONION DOSA", category = MenuCategories.NORMAL_DOSA, price = 65.0, sortOrder = 22),
        MenuItemEntity(name = "BEETROOT ONION DOSA", category = MenuCategories.NORMAL_DOSA, price = 65.0, sortOrder = 23),
        MenuItemEntity(name = "EGG ONION", category = MenuCategories.NORMAL_DOSA, price = 60.0, sortOrder = 113),

        // Dept 3: PESARATTU (5 items)
        MenuItemEntity(name = "PLAIN PESARA DOSA", category = MenuCategories.PESARATTU, price = 45.0, sortOrder = 24),
        MenuItemEntity(name = "KARAM PESARA DOSA", category = MenuCategories.PESARATTU, price = 50.0, sortOrder = 25),
        MenuItemEntity(name = "MASALA PESARA DOSA", category = MenuCategories.PESARATTU, price = 65.0, sortOrder = 26),
        MenuItemEntity(name = "ONION PESARA DOSA", category = MenuCategories.PESARATTU, price = 65.0, sortOrder = 27),
        MenuItemEntity(name = "UPMA PESARA DOSA", category = MenuCategories.PESARATTU, price = 65.0, sortOrder = 28),

        // Dept 4: GHEE IDLI (5 items)
        MenuItemEntity(name = "GHEE PODI IDLI(3)", category = MenuCategories.GHEE_IDLI, price = 65.0, sortOrder = 29),
        MenuItemEntity(name = "GHEE PODI MASALA", category = MenuCategories.GHEE_IDLI, price = 80.0, sortOrder = 30),
        MenuItemEntity(name = "GHEE PODI PUNUGULU", category = MenuCategories.GHEE_IDLI, price = 65.0, sortOrder = 31),
        MenuItemEntity(name = "GHEE PODI IDLI(2)", category = MenuCategories.GHEE_IDLI, price = 45.0, sortOrder = 97),
        MenuItemEntity(name = "GHEE PODI IDLI(4)", category = MenuCategories.GHEE_IDLI, price = 85.0, sortOrder = 98),

        // Dept 5: GHEE DOSA (9 items)
        MenuItemEntity(name = "GHEE PLAIN", category = MenuCategories.GHEE_DOSA, price = 55.0, sortOrder = 32),
        MenuItemEntity(name = "GHEE KARAM DOSA", category = MenuCategories.GHEE_DOSA, price = 65.0, sortOrder = 33),
        MenuItemEntity(name = "GHEE MASALA DOSA", category = MenuCategories.GHEE_DOSA, price = 75.0, sortOrder = 34),
        MenuItemEntity(name = "GHEE ONION DOSA", category = MenuCategories.GHEE_DOSA, price = 75.0, sortOrder = 35),
        MenuItemEntity(name = "GHEE UPMA DOSA", category = MenuCategories.GHEE_DOSA, price = 75.0, sortOrder = 36),
        MenuItemEntity(name = "GHEE UTTAPAM", category = MenuCategories.GHEE_DOSA, price = 85.0, sortOrder = 37),
        MenuItemEntity(name = "GHEE SET DOSA", category = MenuCategories.GHEE_DOSA, price = 85.0, sortOrder = 38),
        MenuItemEntity(name = "GHEE VEGETABLE DOSA", category = MenuCategories.GHEE_DOSA, price = 95.0, sortOrder = 39),
        MenuItemEntity(name = "GHEE EGG DOSA", category = MenuCategories.GHEE_DOSA, price = 75.0, sortOrder = 100),

        // Dept 6: BUTTER DOSA (9 items)
        MenuItemEntity(name = "PLAIN BUTTER DOSA", category = MenuCategories.BUTTER_DOSA, price = 55.0, sortOrder = 40),
        MenuItemEntity(name = "KARAM BUTTER DOSA", category = MenuCategories.BUTTER_DOSA, price = 65.0, sortOrder = 41),
        MenuItemEntity(name = "BUTTER MASALA DOSA", category = MenuCategories.BUTTER_DOSA, price = 75.0, sortOrder = 42),
        MenuItemEntity(name = "BUTTER ONION DOSA", category = MenuCategories.BUTTER_DOSA, price = 75.0, sortOrder = 43),
        MenuItemEntity(name = "BUTTER EGG DOSA", category = MenuCategories.BUTTER_DOSA, price = 75.0, sortOrder = 44),
        MenuItemEntity(name = "BUTTER UPMA DOSA", category = MenuCategories.BUTTER_DOSA, price = 75.0, sortOrder = 45),
        MenuItemEntity(name = "BUTTER UTTAPAM", category = MenuCategories.BUTTER_DOSA, price = 85.0, sortOrder = 46),
        MenuItemEntity(name = "BUTTER SET DOSA", category = MenuCategories.BUTTER_DOSA, price = 85.0, sortOrder = 47),
        MenuItemEntity(name = "BUTTER VEGETABLE DOSA", category = MenuCategories.BUTTER_DOSA, price = 95.0, sortOrder = 48),

        // Dept 7: CHEESE DOSA (5 items)
        MenuItemEntity(name = "CHEESE KARAM DOSA", category = MenuCategories.CHEESE_DOSA, price = 95.0, sortOrder = 49),
        MenuItemEntity(name = "CHEESE MASALA DOSA", category = MenuCategories.CHEESE_DOSA, price = 125.0, sortOrder = 50),
        MenuItemEntity(name = "CHEESE ONION DOSA", category = MenuCategories.CHEESE_DOSA, price = 125.0, sortOrder = 51),
        MenuItemEntity(name = "CHEESE EGG DOSA", category = MenuCategories.CHEESE_DOSA, price = 125.0, sortOrder = 52),
        MenuItemEntity(name = "CORN CHEESE DOSA", category = MenuCategories.CHEESE_DOSA, price = 165.0, sortOrder = 53),

        // Dept 8: PIZZA DOSA (6 items)
        MenuItemEntity(name = "PIZZA", category = MenuCategories.PIZZA_DOSA, price = 135.0, sortOrder = 54),
        MenuItemEntity(name = "CORN PIZZA", category = MenuCategories.PIZZA_DOSA, price = 165.0, sortOrder = 55),
        MenuItemEntity(name = "PANNER PIZZA", category = MenuCategories.PIZZA_DOSA, price = 165.0, sortOrder = 56),
        MenuItemEntity(name = "EGG PIZZA", category = MenuCategories.PIZZA_DOSA, price = 165.0, sortOrder = 57),
        MenuItemEntity(name = "KAJU PIZZA", category = MenuCategories.PIZZA_DOSA, price = 165.0, sortOrder = 58),
        MenuItemEntity(name = "ALL IN ONE PIZZA", category = MenuCategories.PIZZA_DOSA, price = 225.0, sortOrder = 59),

        // Dept 9: PANNER DOSA (7 items)
        MenuItemEntity(name = "PANNER PLAIN DOSA", category = MenuCategories.PANNER_DOSA, price = 85.0, sortOrder = 60),
        MenuItemEntity(name = "PANNER MASALA DOSA", category = MenuCategories.PANNER_DOSA, price = 135.0, sortOrder = 61),
        MenuItemEntity(name = "PANNER CHEESE MASALA", category = MenuCategories.PANNER_DOSA, price = 165.0, sortOrder = 62),
        MenuItemEntity(name = "PANNER KAJU DOSA", category = MenuCategories.PANNER_DOSA, price = 165.0, sortOrder = 63),
        MenuItemEntity(name = "PANNER BUTTER MASALA", category = MenuCategories.PANNER_DOSA, price = 165.0, sortOrder = 64),
        MenuItemEntity(name = "PANNER CORN DOSA", category = MenuCategories.PANNER_DOSA, price = 165.0, sortOrder = 65),
        MenuItemEntity(name = "PANNER KAJU CHEESE MASAL", category = MenuCategories.PANNER_DOSA, price = 205.0, sortOrder = 66),

        // Dept 10: DOSA ITEMS (11 items)
        MenuItemEntity(name = "KAJU MASALA", category = MenuCategories.DOSA_ITEMS, price = 135.0, sortOrder = 67),
        MenuItemEntity(name = "KAJU CORN MASALA", category = MenuCategories.DOSA_ITEMS, price = 165.0, sortOrder = 68),
        MenuItemEntity(name = "CORN MASALA DOSA", category = MenuCategories.DOSA_ITEMS, price = 135.0, sortOrder = 69),
        MenuItemEntity(name = "MUMBAI MASALA DOSA", category = MenuCategories.DOSA_ITEMS, price = 95.0, sortOrder = 70),
        MenuItemEntity(name = "SCHEZWAN DOSA", category = MenuCategories.DOSA_ITEMS, price = 65.0, sortOrder = 71),
        MenuItemEntity(name = "SCHEZWAN MAYONESE DOSA", category = MenuCategories.DOSA_ITEMS, price = 95.0, sortOrder = 72),
        MenuItemEntity(name = "SCHEZWZN PANEER DOSA", category = MenuCategories.DOSA_ITEMS, price = 135.0, sortOrder = 73),
        MenuItemEntity(name = "SCEZWAN CHEESEDOSA", category = MenuCategories.DOSA_ITEMS, price = 105.0, sortOrder = 74),
        MenuItemEntity(name = "KARAM CORN DOSA", category = MenuCategories.DOSA_ITEMS, price = 65.0, sortOrder = 75),
        MenuItemEntity(name = "CORN ONION DOSA", category = MenuCategories.DOSA_ITEMS, price = 75.0, sortOrder = 76),
        MenuItemEntity(name = "GHEE KAJU ONION DOSA", category = MenuCategories.DOSA_ITEMS, price = 135.0, sortOrder = 77),

        // Dept 11: MAGGI (9 items)
        MenuItemEntity(name = "CLASSIC MAGGI", category = MenuCategories.MAGGI, price = 40.0, sortOrder = 84),
        MenuItemEntity(name = "VEG MAGGI", category = MenuCategories.MAGGI, price = 50.0, sortOrder = 85),
        MenuItemEntity(name = "BUTTER MAGGI", category = MenuCategories.MAGGI, price = 60.0, sortOrder = 86),
        MenuItemEntity(name = "CHEESE MAGI", category = MenuCategories.MAGGI, price = 100.0, sortOrder = 87),
        MenuItemEntity(name = "PANEER MAGGI", category = MenuCategories.MAGGI, price = 100.0, sortOrder = 88),
        MenuItemEntity(name = "SCHEZWAN MAGGI", category = MenuCategories.MAGGI, price = 60.0, sortOrder = 89),
        MenuItemEntity(name = "EGG MAGGI", category = MenuCategories.MAGGI, price = 60.0, sortOrder = 90),
        MenuItemEntity(name = "SECHZWAN MAGGI", category = MenuCategories.MAGGI, price = 60.0, sortOrder = 92),
        MenuItemEntity(name = "OMLET", category = MenuCategories.MAGGI, price = 40.0, sortOrder = 99),

        // Dept 12: SNACKS (6 items)
        MenuItemEntity(name = "PONGANALU(7)", category = MenuCategories.SNACKS, price = 40.0, sortOrder = 107),
        MenuItemEntity(name = "EGG PONGANALU(7)", category = MenuCategories.SNACKS, price = 60.0, sortOrder = 108),
        MenuItemEntity(name = "GHEEPODI PONGANALU(7)", category = MenuCategories.SNACKS, price = 85.0, sortOrder = 109),
        MenuItemEntity(name = "GHEEPODI MULBHAGAL DOSA", category = MenuCategories.SNACKS, price = 85.0, sortOrder = 110),
        MenuItemEntity(name = "OPEN BUTTER PODI MULBHAG", category = MenuCategories.SNACKS, price = 85.0, sortOrder = 111),
        MenuItemEntity(name = "CHEESE MULBAGAL DOSA", category = MenuCategories.SNACKS, price = 135.0, sortOrder = 112),
    )
}
