package dev.tbobm.mymymeal.app.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tbobm.mymymeal.app.common.domain.userpreferences.UserPreferencesRepository
import dev.tbobm.mymymeal.app.food.search.domain.FoodSearchPreferences
import dev.tbobm.mymymeal.app.importexport.swissfoodcompositiondatabase.domain.ImportSwissFoodCompositionDatabaseUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class OnboardingViewModel(
    private val importSwissUseCase: ImportSwissFoodCompositionDatabaseUseCase,
    private val foodSearchPreferencesRepository: UserPreferencesRepository<FoodSearchPreferences>,
) : ViewModel() {
    private val eventBus = MutableStateFlow<OnboardingEvent?>(null)
    val events = eventBus.filterNotNull()

    private val mutex = Mutex()

    fun finish(state: OnboardingState) {
        if (mutex.isLocked) {
            return
        }

        val useOpenFoodFacts = state.useOpenFoodFacts
        val useUsda = state.useUsda
        val languages = state.swissLanguages

        viewModelScope.launch {
            mutex.withLock {
                foodSearchPreferencesRepository.update {
                    copy(
                        usda = usda.copy(enabled = useUsda),
                        openFoodFacts = openFoodFacts.copy(enabled = useOpenFoodFacts),
                    )
                }

                if (languages.isNotEmpty()) {
                    importSwissUseCase.import(languages).last()
                }

                eventBus.emit(OnboardingEvent.Finished)
            }
        }
    }
}
