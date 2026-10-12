package dev.tbobm.mymymeal.app.app.ui.onboarding

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import dev.tbobm.mymymeal.app.importexport.swissfoodcompositiondatabase.domain.SwissFoodCompositionDatabaseRepository.Language

@Composable
internal fun rememberOnboardingState(): OnboardingState {
    val openFoodFactsState = rememberSaveable { mutableStateOf(false) }
    val usdaState = rememberSaveable { mutableStateOf(false) }
    val swissLanguagesState = rememberSaveable { mutableStateOf<Set<Language>>(emptySet()) }

    return remember(openFoodFactsState, usdaState, swissLanguagesState) {
        OnboardingState(
            openFoodFactsState = openFoodFactsState,
            usdaState = usdaState,
            swissLanguagesState = swissLanguagesState,
        )
    }
}

internal class OnboardingState(
    openFoodFactsState: MutableState<Boolean>,
    usdaState: MutableState<Boolean>,
    swissLanguagesState: MutableState<Set<Language>>,
) {
    var useOpenFoodFacts by openFoodFactsState
    var useUsda by usdaState
    var swissLanguages by swissLanguagesState
}
