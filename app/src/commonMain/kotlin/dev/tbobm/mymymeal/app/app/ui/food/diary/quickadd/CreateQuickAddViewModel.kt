package dev.tbobm.mymymeal.app.app.ui.food.diary.quickadd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tbobm.mymymeal.app.common.domain.date.DateProvider
import dev.tbobm.mymymeal.app.common.domain.food.NutrientValue.Companion.toNutrientValue
import dev.tbobm.mymymeal.app.common.domain.food.NutritionFacts
import dev.tbobm.mymymeal.app.fooddiary.domain.repository.ManualDiaryEntryRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

internal class CreateQuickAddViewModel(
    private val mealId: Long,
    private val date: LocalDate,
    private val manualDiaryEntryRepository: ManualDiaryEntryRepository,
    private val dateProvider: DateProvider,
) : ViewModel() {

    private val eventChannel = Channel<QuickAddUiEvent>()
    val uiEvents = eventChannel.receiveAsFlow()

    fun addEntry(
        name: String,
        energy: Double,
        proteins: Double,
        carbohydrates: Double,
        fats: Double,
    ) {
        viewModelScope.launch {
            manualDiaryEntryRepository.insert(
                name = name,
                mealId = mealId,
                date = date,
                nutritionFacts =
                    NutritionFacts(
                        energy = energy.toNutrientValue(),
                        proteins = proteins.toNutrientValue(),
                        carbohydrates = carbohydrates.toNutrientValue(),
                        fats = fats.toNutrientValue(),
                    ),
                createdAt = dateProvider.now(),
            )

            eventChannel.send(QuickAddUiEvent.Saved)
        }
    }
}
