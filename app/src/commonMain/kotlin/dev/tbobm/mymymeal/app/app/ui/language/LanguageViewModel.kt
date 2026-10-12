package dev.tbobm.mymymeal.app.app.ui.language

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tbobm.mymymeal.app.common.domain.userpreferences.UserPreferencesRepository
import dev.tbobm.mymymeal.app.settings.domain.entity.Settings
import dev.tbobm.mymymeal.app.settings.domain.entity.Translation
import dev.tbobm.mymymeal.app.settings.domain.repository.TranslationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

internal class LanguageViewModel(
    private val translationRepository: TranslationRepository,
    private val settingsRepository: UserPreferencesRepository<Settings>,
) : ViewModel() {

    private val translationsFlow = translationRepository.observe()

    val translations =
        translationsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(2_000),
            initialValue = runBlocking { translationsFlow.first() },
        )

    private val currentTranslationFlow = translationRepository.observeCurrent()

    val translation =
        currentTranslationFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = runBlocking { currentTranslationFlow.first() },
        )

    val showTranslationWarning =
        settingsRepository
            .observe()
            .map { it.showTranslationWarning }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = false,
            )

    fun selectTranslation(translation: Translation?) {
        viewModelScope.launch { translationRepository.setTranslation(translation) }
    }

    fun hideTranslationWarning() {
        viewModelScope.launch { settingsRepository.update { copy(showTranslationWarning = false) } }
    }
}
