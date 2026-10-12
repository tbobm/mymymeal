package dev.tbobm.mymymeal.app.app.ui.food.diary.update

internal sealed interface UpdateEntryEvent {
    data object Saved : UpdateEntryEvent
}
