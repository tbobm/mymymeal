package dev.tbobm.mymymeal.app.app.ui.food.diary.quickadd

internal sealed interface QuickAddUiEvent {
    data object Saved : QuickAddUiEvent
}
