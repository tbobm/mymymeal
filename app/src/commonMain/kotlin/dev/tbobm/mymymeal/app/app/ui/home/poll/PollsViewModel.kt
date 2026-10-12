package dev.tbobm.mymymeal.app.app.ui.home.poll

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tbobm.mymymeal.app.common.domain.userpreferences.UserPreferencesRepository
import dev.tbobm.mymymeal.app.poll.domain.entity.PollId
import dev.tbobm.mymymeal.app.poll.domain.entity.PollPreferences
import dev.tbobm.mymymeal.app.poll.domain.usecase.ObserveActivePollUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class PollsViewModel(
    observeActivePollUseCase: ObserveActivePollUseCase,
    private val pollPreferencesRepository: UserPreferencesRepository<PollPreferences>,
) : ViewModel() {
    val polls =
        observeActivePollUseCase
            .observe()
            .stateIn(
                initialValue = emptyList(),
                started = SharingStarted.WhileSubscribed(5_000),
                scope = viewModelScope,
            )

    fun dismissPoll(pollId: PollId) {
        viewModelScope.launch {
            pollPreferencesRepository.update { copy(dismissedPolls = dismissedPolls + pollId) }
        }
    }
}
