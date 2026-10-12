package dev.tbobm.mymymeal.app.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import dev.tbobm.mymymeal.app.theme.Theme
import dev.tbobm.mymymeal.app.theme.ThemeContrast
import dev.tbobm.mymymeal.app.theme.ThemeOption
import dev.tbobm.mymymeal.app.theme.ThemeSettings
import dev.tbobm.mymymeal.app.theme.ThemeStyle
import com.materialkolor.Contrast
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme

internal fun ThemeStyle.toPaletteStyle(): PaletteStyle =
    when (this) {
        ThemeStyle.TonalSpot -> PaletteStyle.TonalSpot
        ThemeStyle.Neutral -> PaletteStyle.Neutral
        ThemeStyle.Vibrant -> PaletteStyle.Vibrant
        ThemeStyle.Expressive -> PaletteStyle.Expressive
        ThemeStyle.Rainbow -> PaletteStyle.Rainbow
        ThemeStyle.FruitSalad -> PaletteStyle.FruitSalad
        ThemeStyle.Monochrome -> PaletteStyle.Monochrome
        ThemeStyle.Fidelity -> PaletteStyle.Fidelity
        ThemeStyle.Content -> PaletteStyle.Content
    }

internal fun ThemeContrast.toContrastLevel(): Contrast =
    when (this) {
        ThemeContrast.Default -> Contrast.Default
        ThemeContrast.Medium -> Contrast.Medium
        ThemeContrast.High -> Contrast.High
        ThemeContrast.Reduced -> Contrast.Reduced
    }

@Composable
internal fun ThemeSettings.isDark(): Boolean =
    when (themeOption) {
        ThemeOption.System -> isSystemInDarkTheme()
        ThemeOption.Light -> false
        ThemeOption.Dark -> true
    }

@Composable
internal fun Theme.Custom.rememberColorScheme(isDark: Boolean): ColorScheme =
    rememberDynamicColorScheme(
        seedColor = Color(seedColor),
        isDark = isDark,
        isAmoled = isAmoled,
        style = style.toPaletteStyle(),
        contrastLevel = contrast.toContrastLevel().value,
    )
