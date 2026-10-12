package dev.tbobm.mymymeal.app.app.ui.database.healthconnect

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tbobm.mymymeal.app.app.infrastructure.android.healthconnect.HealthConnectAvailability
import dev.tbobm.mymymeal.app.app.infrastructure.android.healthconnect.HealthConnectSyncService
import dev.tbobm.mymymeal.app.app.infrastructure.android.healthconnect.checkHealthConnectAvailability
import dev.tbobm.mymymeal.app.common.domain.userpreferences.UserPreferencesRepository
import dev.tbobm.mymymeal.app.fooddiary.domain.healthexport.HealthConnectPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal enum class BackfillState {
    Idle,
    Running,
    Done,
    Failed,
}

internal class HealthConnectViewModel(
    private val context: Context,
    private val preferencesRepository: UserPreferencesRepository<HealthConnectPreferences>,
    private val syncService: HealthConnectSyncService,
) : ViewModel() {
    private val _availability = MutableStateFlow<HealthConnectAvailability?>(null)
    val availability = _availability.asStateFlow()

    val preferences =
        preferencesRepository
            .observe()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(2_000),
                HealthConnectPreferences.default,
            )

    private val _backfillState = MutableStateFlow(BackfillState.Idle)
    val backfillState = _backfillState.asStateFlow()

    init {
        refreshAvailability()
    }

    /** Call after returning from the permission request or the Health Connect install flow. */
    fun refreshAvailability() {
        viewModelScope.launch { _availability.value = checkHealthConnectAvailability(context) }
    }

    fun setSyncEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.update { copy(syncEnabled = enabled) } }
    }

    fun backfill(days: Int = 30) {
        viewModelScope.launch {
            _backfillState.value = BackfillState.Running
            _backfillState.value =
                if (syncService.backfill(days)) BackfillState.Done else BackfillState.Failed
        }
    }
}
