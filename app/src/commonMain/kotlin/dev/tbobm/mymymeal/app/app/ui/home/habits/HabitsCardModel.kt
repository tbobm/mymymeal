package dev.tbobm.mymymeal.app.app.ui.home.habits

internal data class HabitsCardModel(
    val coffeesToday: Int,
    val caffeineMgToday: Int,
    val supplements: List<SupplementRow>,
)

internal data class SupplementRow(
    val id: Long,
    val name: String,
    val tracksDose: Boolean,
    val taken: Boolean,
    val doseGrams: Double?,
)
