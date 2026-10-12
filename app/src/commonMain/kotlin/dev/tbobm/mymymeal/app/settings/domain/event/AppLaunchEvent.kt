package dev.tbobm.mymymeal.app.settings.domain.event

import dev.tbobm.mymymeal.app.common.domain.event.IntegrationEvent
import kotlin.time.Instant

data class AppLaunchEvent(val timestamp: Instant) : IntegrationEvent
