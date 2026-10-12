package dev.tbobm.mymymeal.app.app.infrastructure.android.widget

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import dev.tbobm.mymymeal.app.app.infrastructure.android.MainActivity
import dev.tbobm.mymymeal.app.app.ui.common.theme.DarkNutrientsPalette
import dev.tbobm.mymymeal.app.app.ui.common.theme.LightNutrientsPalette
import dev.tbobm.mymymeal.app.common.domain.date.DateProvider
import dev.tbobm.mymymeal.app.common.domain.food.NutritionFactsField
import dev.tbobm.mymymeal.app.common.domain.food.sum
import dev.tbobm.mymymeal.app.fooddiary.domain.usecase.ObserveDiaryMealsUseCase
import dev.tbobm.mymymeal.app.goals.domain.repository.GoalsRepository
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import org.koin.core.context.GlobalContext

/**
 * Read-only home-screen widget: today's energy remaining and macro progress. Data reuses the same
 * Koin-injected use cases as the in-app Goals card; DI resolves from the process-global Koin
 * context (MymymealApplication.onCreate runs in this process).
 */
class DiaryWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: androidx.glance.GlanceId) {
        val koin = GlobalContext.get()
        val observeDiaryMeals = koin.get<ObserveDiaryMealsUseCase>()
        val goals = koin.get<GoalsRepository>()
        val dateProvider = koin.get<DateProvider>()

        val today = dateProvider.observeDate().first()
        val summary =
            combine(observeDiaryMeals.observe(today), goals.observeDailyGoals(today)) { meals, goal ->
                    val facts = meals.map { it.nutritionFacts }.sum()
                    WidgetSummary(
                        energy = facts.energy.value?.roundToInt() ?: 0,
                        energyGoal = goal[NutritionFactsField.Energy].roundToInt(),
                        proteins = facts.proteins.value?.roundToInt() ?: 0,
                        proteinsGoal = goal[NutritionFactsField.Proteins].roundToInt(),
                        carbohydrates = facts.carbohydrates.value?.roundToInt() ?: 0,
                        carbohydratesGoal = goal[NutritionFactsField.Carbohydrates].roundToInt(),
                        fats = facts.fats.value?.roundToInt() ?: 0,
                        fatsGoal = goal[NutritionFactsField.Fats].roundToInt(),
                    )
                }
                .first()

        provideContent { Content(summary) }
    }

    @Composable
    private fun Content(s: WidgetSummary) {
        GlanceTheme {
            val onBackground = GlanceTheme.colors.onBackground
            val outline = GlanceTheme.colors.outline
            val context = LocalContext.current
            val isDark =
                context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
                    Configuration.UI_MODE_NIGHT_YES
            val palette = if (isDark) DarkNutrientsPalette else LightNutrientsPalette

            Column(
                modifier =
                    GlanceModifier.fillMaxSize()
                        .background(GlanceTheme.colors.background)
                        .padding(12.dp)
                        .clickable(
                            actionStartActivity(Intent(context, MainActivity::class.java))
                        )
            ) {
                val remaining = s.energyGoal - s.energy

                Row {
                    Text(
                        text = "${s.energy}",
                        style =
                            TextStyle(
                                color = onBackground,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                    )
                    Spacer(GlanceModifier.width(4.dp))
                    Text(
                        text = "/ ${s.energyGoal} kcal",
                        style = TextStyle(color = outline, fontSize = 14.sp),
                    )
                }
                Text(
                    text =
                        if (remaining >= 0) "$remaining kcal left"
                        else "${-remaining} kcal over",
                    style =
                        TextStyle(
                            color = outline,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                )

                Spacer(GlanceModifier.height(12.dp))

                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    // Res.string.nutriment_*_short can't be resolved here: Compose Multiplatform's
                    // stringResource() calls isSystemInDarkTheme(), which needs LocalConfiguration
                    // (a Compose-UI composition local Glance's widget tree doesn't provide).
                    NutrientBar(
                        label = "P",
                        current = s.proteins,
                        goal = s.proteinsGoal,
                        color = palette.proteinsOnSurfaceContainer,
                        labelColor = outline,
                        modifier = GlanceModifier.defaultWeight(),
                    )
                    Spacer(GlanceModifier.width(12.dp))
                    NutrientBar(
                        label = "C",
                        current = s.carbohydrates,
                        goal = s.carbohydratesGoal,
                        color = palette.carbohydratesOnSurfaceContainer,
                        labelColor = outline,
                        modifier = GlanceModifier.defaultWeight(),
                    )
                    Spacer(GlanceModifier.width(12.dp))
                    NutrientBar(
                        label = "F",
                        current = s.fats,
                        goal = s.fatsGoal,
                        color = palette.fatsOnSurfaceContainer,
                        labelColor = outline,
                        modifier = GlanceModifier.defaultWeight(),
                    )
                }
            }
        }
    }

    @Composable
    private fun NutrientBar(
        label: String,
        current: Int,
        goal: Int,
        color: Color,
        labelColor: ColorProvider,
        modifier: GlanceModifier = GlanceModifier,
    ) {
        val barHeight = 40.dp

        Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            MacroBar(
                progress = current.toFloat() / goal,
                barHeight = barHeight,
                color = color,
                modifier = GlanceModifier.fillMaxWidth(),
            )
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text = "$label · $current/$goal g",
                style = TextStyle(color = labelColor, fontSize = 11.sp),
            )
        }
    }

    @Composable
    private fun MacroBar(
        progress: Float,
        barHeight: Dp,
        color: Color,
        modifier: GlanceModifier = GlanceModifier,
    ) {
        val filledHeight = barHeight * progress.coerceIn(0f, 1f)

        Box(
            modifier =
                modifier
                    .height(barHeight)
                    .background(color.copy(alpha = .25f))
                    .cornerRadius(6.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Box(
                modifier =
                    GlanceModifier.fillMaxWidth()
                        .height(filledHeight)
                        .background(color)
                        .cornerRadius(6.dp)
            ) {}
        }
    }
}

private data class WidgetSummary(
    val energy: Int,
    val energyGoal: Int,
    val proteins: Int,
    val proteinsGoal: Int,
    val carbohydrates: Int,
    val carbohydratesGoal: Int,
    val fats: Int,
    val fatsGoal: Int,
)
