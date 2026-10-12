package dev.tbobm.mymymeal.app.poll.domain.repository

import dev.tbobm.mymymeal.app.poll.domain.entity.Poll
import kotlinx.coroutines.flow.Flow

interface PollRepository {
    fun observeActivePolls(): Flow<List<Poll>>
}
