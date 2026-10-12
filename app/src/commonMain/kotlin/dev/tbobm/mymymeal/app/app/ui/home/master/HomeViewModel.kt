package dev.tbobm.mymymeal.app.app.ui.home.master

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tbobm.mymymeal.app.common.domain.userpreferences.UserPreferencesRepository
import dev.tbobm.mymymeal.app.settings.domain.entity.Settings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking

internal class HomeViewModel(settingsRepository: UserPreferencesRepository<Settings>) :
    ViewModel() {

    private val _homeOrder = settingsRepository.observe().map { it.homeCardOrder }
    val homeOrder =
        _homeOrder.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(2_000),
            initialValue = runBlocking { _homeOrder.first() },
        )
}
