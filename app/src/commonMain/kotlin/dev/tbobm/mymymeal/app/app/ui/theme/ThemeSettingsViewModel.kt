package dev.tbobm.mymymeal.app.app.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tbobm.mymymeal.app.common.domain.userpreferences.UserPreferencesRepository
import dev.tbobm.mymymeal.app.theme.NutrientsColors
import dev.tbobm.mymymeal.app.theme.RandomizeThemeUseCase
import dev.tbobm.mymymeal.app.theme.Theme
import dev.tbobm.mymymeal.app.theme.ThemeOption
import dev.tbobm.mymymeal.app.theme.ThemeSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class ThemeSettingsViewModel(
    private val themeRepository: UserPreferencesRepository<ThemeSettings>,
    private val nutrientsColorsRepository: UserPreferencesRepository<NutrientsColors>,
    private val randomizeThemeUseCase: RandomizeThemeUseCase,
) : ViewModel() {
    val themeSettings =
        themeRepository
            .observe()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(2_000),
                initialValue = null,
            )

    val nutrientsColors =
        nutrientsColorsRepository
            .observe()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(2_000),
                initialValue = null,
            )

    fun updateNutrientsColors(nutrientsColors: NutrientsColors) {
        viewModelScope.launch { nutrientsColorsRepository.update { nutrientsColors } }
    }

    fun updateTheme(theme: Theme) {
        viewModelScope.launch {
            themeRepository.update { copy(randomizeOnLaunch = false, theme = theme) }
        }
    }

    fun updateThemeOption(themeOption: ThemeOption) {
        viewModelScope.launch { themeRepository.update { copy(themeOption = themeOption) } }
    }

    fun updateRandomizeTheme(randomize: Boolean) {
        viewModelScope.launch {
            if (randomize) {
                randomizeThemeUseCase.randomize()
            }

            themeRepository.update { copy(randomizeOnLaunch = randomize) }
        }
    }
}
