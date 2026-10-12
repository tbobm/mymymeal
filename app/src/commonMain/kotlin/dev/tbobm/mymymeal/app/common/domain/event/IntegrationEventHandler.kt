package dev.tbobm.mymymeal.app.common.domain.event

fun interface IntegrationEventHandler<E : IntegrationEvent> {
    suspend fun handle(event: E)
}
