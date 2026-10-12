package dev.tbobm.mymymeal.app.app.ui.common.utility

import androidx.compose.runtime.*
import dev.tbobm.mymymeal.app.settings.domain.entity.NutrientsOrder

val LocalNutrientsOrder = staticCompositionLocalOf { NutrientsOrder.defaultOrder }

@Composable
fun NutrientsOrderProvider(order: List<NutrientsOrder>, content: @Composable () -> Unit) {

    CompositionLocalProvider(LocalNutrientsOrder provides order) { content() }
}
