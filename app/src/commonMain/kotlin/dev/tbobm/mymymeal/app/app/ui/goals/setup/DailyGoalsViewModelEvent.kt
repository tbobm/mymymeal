package dev.tbobm.mymymeal.app.app.ui.goals.setup

internal sealed interface DailyGoalsViewModelEvent {

    data object Updated : DailyGoalsViewModelEvent
}
