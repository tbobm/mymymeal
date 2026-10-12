package dev.tbobm.mymymeal.app.app.ui.theme

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.tbobm.mymymeal.app.app.ui.common.theme.NutrientsPalette
import dev.tbobm.mymymeal.app.theme.NutrientsColors
import dev.tbobm.mymymeal.app.theme.ThemeSettings
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MymymealTheme(content: @Composable () -> Unit) {
    val viewModel: ThemeSettingsViewModel = koinViewModel()
    val themeSettings by viewModel.themeSettings.collectAsStateWithLifecycle()
    val nutrientsColors by viewModel.nutrientsColors.collectAsStateWithLifecycle()

    MymymealTheme(themeSettings, nutrientsColors, content)
}

@Composable
internal expect fun MymymealTheme(
    themeSettings: ThemeSettings?,
    nutrientsColors: NutrientsColors?,
    content: @Composable () -> Unit,
)

internal fun NutrientsPalette.applyColors(nutrientsColors: NutrientsColors?) =
    copy(
        proteinsOnSurfaceContainer =
            nutrientsColors?.proteins?.let(::Color) ?: proteinsOnSurfaceContainer,
        carbohydratesOnSurfaceContainer =
            nutrientsColors?.carbohydrates?.let(::Color) ?: carbohydratesOnSurfaceContainer,
        fatsOnSurfaceContainer = nutrientsColors?.fats?.let(::Color) ?: fatsOnSurfaceContainer,
    )
