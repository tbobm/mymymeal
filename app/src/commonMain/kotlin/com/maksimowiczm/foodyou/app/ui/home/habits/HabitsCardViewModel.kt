package com.maksimowiczm.foodyou.app.ui.home.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.app.ui.food.diary.add.toDiaryFood
import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import com.maksimowiczm.foodyou.common.domain.event.EventBus
import com.maksimowiczm.foodyou.common.domain.food.sum
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.domain.measurement.from
import com.maksimowiczm.foodyou.common.domain.measurement.rawValue
import com.maksimowiczm.foodyou.common.domain.measurement.type
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import com.maksimowiczm.foodyou.common.result.onSuccess
import com.maksimowiczm.foodyou.food.domain.entity.FoodId
import com.maksimowiczm.foodyou.food.domain.usecase.ObserveFoodUseCase
import com.maksimowiczm.foodyou.fooddiary.domain.event.FoodDiaryEntryCreatedEvent
import com.maksimowiczm.foodyou.fooddiary.domain.repository.FoodDiaryEntryRepository
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.CreateFoodDiaryEntryUseCase
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.ObserveDiaryMealsUseCase
import com.maksimowiczm.foodyou.habits.domain.entity.HabitsPreferences
import com.maksimowiczm.foodyou.habits.domain.repository.SupplementRepository
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

internal class HabitsCardViewModel(
    private val supplementRepository: SupplementRepository,
    private val habitsPreferencesRepository: UserPreferencesRepository<HabitsPreferences>,
    private val foodDiaryEntryRepository: FoodDiaryEntryRepository,
    private val observeDiaryMealsUseCase: ObserveDiaryMealsUseCase,
    private val observeFoodUseCase: ObserveFoodUseCase,
    private val createFoodDiaryEntryUseCase: CreateFoodDiaryEntryUseCase,
    private val eventBus: EventBus,
    private val dateProvider: DateProvider,
) : ViewModel() {
    private val dateState = MutableStateFlow<LocalDate?>(null)

    fun setDate(date: LocalDate) {
        dateState.value = date
    }

    val model =
        dateState
            .filterNotNull()
            .flatMapLatest { date ->
                // Cups-today depends on the configured default coffee, so the preferences flow is
                // pre-flattened into a (prefs, cupsToday) pair before joining the rest of the
                // sources in a single flat `combine`.
                val prefsWithCups =
                    habitsPreferencesRepository.observe().flatMapLatest { prefs ->
                        val defaultCoffeeFoodId = prefs.toDefaultCoffeeFoodId()
                        val cupsToday =
                            if (defaultCoffeeFoodId == null) {
                                flowOf(0)
                            } else {
                                observeFoodUseCase.observe(defaultCoffeeFoodId).flatMapLatest { food
                                    ->
                                    if (food == null) {
                                        flowOf(0)
                                    } else {
                                        // ponytail: matches by current catalog name against each
                                        // entry's snapshot name -- renaming the default coffee
                                        // mid-day drops already-logged cups from the count, and two
                                        // catalog foods sharing a name would double-count. Acceptable
                                        // for a single-user local tracker; revisit if that changes.
                                        foodDiaryEntryRepository.observeEntryCountByFoodName(
                                            name = food.headline,
                                            isRecipe = defaultCoffeeFoodId is FoodId.Recipe,
                                            date = date,
                                        )
                                    }
                                }
                            }
                        cupsToday.map { cups -> prefs to cups }
                    }

                combine(
                    observeDiaryMealsUseCase.observe(date),
                    supplementRepository.observeSupplements(),
                    supplementRepository.observeTakenIdsForDate(date),
                    prefsWithCups,
                ) { meals, supplements, takenIds, (prefs, cupsToday) ->
                    val caffeineMg =
                        ((meals.map { it.nutritionFacts }.sum().caffeine.value ?: 0.0) * 1000.0)
                            .roundToInt()

                    HabitsCardModel(
                        cupsToday = cupsToday,
                        caffeineMgToday = caffeineMg,
                        hasDefaultCoffee = prefs.toDefaultCoffeeFoodId() != null,
                        supplements =
                            supplements.map { SupplementRow(it.id, it.name, it.id in takenIds) },
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(60_000),
                initialValue = null,
            )

    fun addSupplement(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { supplementRepository.addSupplement(name.trim()) }
    }

    fun deleteSupplement(id: Long) {
        viewModelScope.launch { supplementRepository.deleteSupplement(id) }
    }

    fun setSupplementTaken(id: Long, taken: Boolean) {
        val date = dateState.value ?: return
        viewModelScope.launch { supplementRepository.setTaken(id, date, taken) }
    }

    /** One-tap re-log of the configured default coffee. No-op if none is configured yet. */
    fun logCoffee() {
        viewModelScope.launch {
            val prefs = habitsPreferencesRepository.observe().firstOrNull() ?: return@launch
            val foodId = prefs.toDefaultCoffeeFoodId() ?: return@launch
            val measurementType = prefs.defaultCoffeeMeasurementType ?: return@launch
            val measurementValue = prefs.defaultCoffeeMeasurementValue ?: return@launch
            val targetMealId = prefs.defaultCoffeeMealId ?: return@launch
            val date = dateState.value ?: return@launch

            val food = observeFoodUseCase.observe(foodId).firstOrNull() ?: return@launch
            val measurement = Measurement.from(type = measurementType, rawValue = measurementValue)

            createFoodDiaryEntryUseCase
                .createDiaryEntry(
                    measurement = measurement,
                    mealId = targetMealId,
                    date = date,
                    food = food.toDiaryFood(),
                )
                .onSuccess {
                    eventBus.publish(
                        FoodDiaryEntryCreatedEvent(
                            foodId = food.id,
                            timestamp = dateProvider.nowInstant(),
                            measurement = measurement,
                        )
                    )
                }
        }
    }

    /** Called after the one-time "pick default coffee" flow completes. */
    fun setDefaultCoffee(foodId: FoodId, measurement: Measurement, mealId: Long) {
        viewModelScope.launch {
            habitsPreferencesRepository.update {
                copy(
                    defaultCoffeeFoodId =
                        when (foodId) {
                            is FoodId.Product -> foodId.id
                            is FoodId.Recipe -> foodId.id
                        },
                    defaultCoffeeIsRecipe = foodId is FoodId.Recipe,
                    defaultCoffeeMealId = mealId,
                    defaultCoffeeMeasurementType = measurement.type,
                    defaultCoffeeMeasurementValue = measurement.rawValue,
                )
            }
        }
    }
}

private fun HabitsPreferences.toDefaultCoffeeFoodId(): FoodId? {
    val id = defaultCoffeeFoodId ?: return null
    return if (defaultCoffeeIsRecipe) FoodId.Recipe(id) else FoodId.Product(id)
}
