package dev.tbobm.mymymeal.app.poll.domain.entity

import dev.tbobm.mymymeal.app.common.domain.userpreferences.UserPreferences

data class PollPreferences(val dismissedPolls: Set<PollId>) : UserPreferences
