package dev.tbobm.mymymeal.app.app.ui.onboarding

internal sealed interface OnboardingEvent {
    data object Finished : OnboardingEvent
}
