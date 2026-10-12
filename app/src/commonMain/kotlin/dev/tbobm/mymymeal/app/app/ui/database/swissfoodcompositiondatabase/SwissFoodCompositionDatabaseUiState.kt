package dev.tbobm.mymymeal.app.app.ui.database.swissfoodcompositiondatabase

internal sealed interface SwissFoodCompositionDatabaseUiState {
    data object LanguagePick : SwissFoodCompositionDatabaseUiState

    data class Importing(val progress: Float) : SwissFoodCompositionDatabaseUiState

    data object Finished : SwissFoodCompositionDatabaseUiState
}
