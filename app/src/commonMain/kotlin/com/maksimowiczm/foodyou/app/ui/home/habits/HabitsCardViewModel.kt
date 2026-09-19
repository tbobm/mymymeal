package com.maksimowiczm.foodyou.app.ui.home.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.habits.domain.entity.CoffeeType
import com.maksimowiczm.foodyou.habits.domain.repository.CoffeeRepository
import com.maksimowiczm.foodyou.habits.domain.repository.SupplementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

internal class HabitsCardViewModel(
    private val supplementRepository: SupplementRepository,
    private val coffeeRepository: CoffeeRepository,
) : ViewModel() {
    private val dateState = MutableStateFlow<LocalDate?>(null)

    fun setDate(date: LocalDate) {
        dateState.value = date
    }

    val model =
        dateState
            .filterNotNull()
            .flatMapLatest { date ->
                combine(
                    coffeeRepository.observeForDate(date),
                    coffeeRepository.observeCaffeineMgForDate(date),
                    supplementRepository.observeSupplements(),
                    supplementRepository.observeIntakeForDate(date),
                ) { coffees, caffeineMg, supplements, intake ->
                    HabitsCardModel(
                        coffeesToday = coffees.size,
                        caffeineMgToday = caffeineMg,
                        supplements =
                            supplements.map { supplement ->
                                val row = intake[supplement.id]
                                SupplementRow(
                                    id = supplement.id,
                                    name = supplement.name,
                                    tracksDose = supplement.tracksDose,
                                    taken = row != null,
                                    doseGrams = row?.doseGrams,
                                )
                            },
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(60_000),
                initialValue = null,
            )

    /** One-tap log of [type]. Not food -- see [CoffeeRepository]. */
    fun logCoffee(type: CoffeeType) {
        val date = dateState.value ?: return
        viewModelScope.launch { coffeeRepository.logCoffee(type, date) }
    }

    fun addSupplement(name: String, tracksDose: Boolean) {
        if (name.isBlank()) return
        viewModelScope.launch { supplementRepository.addSupplement(name.trim(), tracksDose) }
    }

    fun deleteSupplement(id: Long) {
        viewModelScope.launch { supplementRepository.deleteSupplement(id) }
    }

    fun setSupplementTaken(id: Long, taken: Boolean) {
        val date = dateState.value ?: return
        viewModelScope.launch { supplementRepository.setIntake(id, date, taken) }
    }

    /** Setting a dose implies taken = true. */
    fun setSupplementDose(id: Long, doseGrams: Double?) {
        val date = dateState.value ?: return
        viewModelScope.launch {
            supplementRepository.setIntake(id, date, taken = true, doseGrams = doseGrams)
        }
    }
}
