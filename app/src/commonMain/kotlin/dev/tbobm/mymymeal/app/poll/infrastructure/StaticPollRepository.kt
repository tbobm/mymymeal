package dev.tbobm.mymymeal.app.poll.infrastructure

import dev.tbobm.mymymeal.app.common.domain.date.DateProvider
import dev.tbobm.mymymeal.app.poll.domain.entity.LinkPoll
import dev.tbobm.mymymeal.app.poll.domain.entity.Poll
import dev.tbobm.mymymeal.app.poll.domain.entity.PollId
import dev.tbobm.mymymeal.app.poll.domain.repository.PollRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

internal class StaticPollRepository(private val dateProvider: DateProvider) : PollRepository {
    override fun observeActivePolls(): Flow<List<Poll>> {
        val activePolls =
            listOf<Poll>(FirstMymymeal3Poll).filter { it.expireDateTime > dateProvider.nowInstant() }

        return flowOf(activePolls)
    }
}

private val FirstMymymeal3Poll =
    LinkPoll(
        id = PollId("Food You 9.09.2025"),
        expireDateTime = LocalDateTime(2025, 9, 30, 23, 59).toInstant(TimeZone.UTC),
        title = "Your opinion matters!",
        description = "Help guide the app’s next steps by voting in Food You feature poll",
        url =
            "https://docs.google.com/forms/d/e/1FAIpQLSedg7Ofb2-r8mob_tUD1uxl9_PPMj7zfrHU6PB-w5_HNZAIHg/viewform?usp=header",
    )
