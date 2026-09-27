package com.example.pos.model

data class DepartmentInfo(
    val id: Int,
    val name: String,
    val code: String,
    val isActive: Boolean = true
)

object MenuCategories {
    const val SOUTH_INDIAN = "SOUTH INDIAN"
    const val NORMAL_DOSA = "NORMAL DOSA"
    const val PESARATTU = "PESARATTU"
    const val GHEE_IDLI = "GHEE IDLI"
    const val GHEE_DOSA = "GHEE DOSA"
    const val BUTTER_DOSA = "BUTTER DOSA"
    const val CHEESE_DOSA = "CHEESE DOSA"
    const val PIZZA_DOSA = "PIZZA DOSA"
    const val PANNER_DOSA = "PANNER DOSA"
    const val DOSA_ITEMS = "DOSA ITEMS"
    const val MAGGI = "MAGGI"
    const val SNACKS = "SNACKS"

    val ALL_DEPARTMENTS = listOf(
        DepartmentInfo(1, SOUTH_INDIAN, "SI"),
        DepartmentInfo(2, NORMAL_DOSA, "ND"),
        DepartmentInfo(3, PESARATTU, "PS"),
        DepartmentInfo(4, GHEE_IDLI, "GI"),
        DepartmentInfo(5, GHEE_DOSA, "GD"),
        DepartmentInfo(6, BUTTER_DOSA, "BD"),
        DepartmentInfo(7, CHEESE_DOSA, "CD"),
        DepartmentInfo(8, PIZZA_DOSA, "PD"),
        DepartmentInfo(9, PANNER_DOSA, "PND"),
        DepartmentInfo(10, DOSA_ITEMS, "DI"),
        DepartmentInfo(11, MAGGI, "MG"),
        DepartmentInfo(12, SNACKS, "SN")
    )

    val ALL_CATEGORIES = ALL_DEPARTMENTS.map { it.name }

    fun getDepartmentByCode(code: String): DepartmentInfo? =
        ALL_DEPARTMENTS.firstOrNull { it.code.equals(code, ignoreCase = true) }

    fun getDepartmentById(id: Int): DepartmentInfo? =
        ALL_DEPARTMENTS.firstOrNull { it.id == id }

    fun getCodeForCategory(name: String): String =
        ALL_DEPARTMENTS.firstOrNull { it.name.equals(name, ignoreCase = true) }?.code ?: ""
}
