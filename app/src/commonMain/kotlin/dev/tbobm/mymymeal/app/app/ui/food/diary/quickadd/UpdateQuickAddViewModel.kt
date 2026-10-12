package dev.tbobm.mymymeal.app.app.ui.food.diary.quickadd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tbobm.mymymeal.app.common.domain.date.DateProvider
import dev.tbobm.mymymeal.app.common.domain.food.NutrientValue.Companion.toNutrientValue
import dev.tbobm.mymymeal.app.fooddiary.domain.entity.ManualDiaryEntryId
import dev.tbobm.mymymeal.app.fooddiary.domain.repository.ManualDiaryEntryRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class UpdateQuickAddViewModel(
    id: ManualDiaryEntryId,
    private val manualDiaryEntryRepository: ManualDiaryEntryRepository,
    private val dateProvider: DateProvider,
) : ViewModel() {

    private val eventChannel = Channel<QuickAddUiEvent>()
    val uiEvents = eventChannel.receiveAsFlow()

    val entry =
        manualDiaryEntryRepository
            .observe(id)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(2_000),
                initialValue = null,
            )

    fun updateEntry(
        name: String,
        energy: Double,
        proteins: Double,
        carbohydrates: Double,
        fats: Double,
    ) {
        val entry = entry.value

        if (entry == null) {
            return
        }

        viewModelScope.launch {
            val updatedEntry =
                entry.copy(
                    name = name,
                    nutritionFacts =
                        entry.nutritionFacts.copy(
                            energy = energy.toNutrientValue(),
                            proteins = proteins.toNutrientValue(),
                            carbohydrates = carbohydrates.toNutrientValue(),
                            fats = fats.toNutrientValue(),
                        ),
                    updatedAt = dateProvider.now(),
                )

            manualDiaryEntryRepository.update(updatedEntry)

            eventChannel.send(QuickAddUiEvent.Saved)
        }
    }
}
