package dev.tbobm.mymymeal.app.app.ui

import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.tbobm.mymymeal.app.app.navigation.MymymealAppNavHost
import dev.tbobm.mymymeal.app.app.ui.changelog.AppUpdateChangelogModalBottomSheet
import dev.tbobm.mymymeal.app.app.ui.changelog.PreviewReleaseDialog
import dev.tbobm.mymymeal.app.app.ui.common.utility.EnergyFormatterProvider
import dev.tbobm.mymymeal.app.app.ui.common.utility.NutrientsOrderProvider
import dev.tbobm.mymymeal.app.app.ui.language.TranslationWarningStartupDialog
import dev.tbobm.mymymeal.app.app.ui.onboarding.Onboarding
import dev.tbobm.mymymeal.app.app.ui.theme.MymymealTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MymymealApp(onDatabaseBackup: () -> Unit) {
    val viewModel: AppViewModel = koinViewModel()
    val nutrientsOrder by viewModel.nutrientsOrder.collectAsStateWithLifecycle()
    val onboardingFinished by viewModel.onboardingFinished.collectAsStateWithLifecycle()
    val energyFormatter by viewModel.energyFormatter.collectAsStateWithLifecycle()

    NutrientsOrderProvider(nutrientsOrder) {
        EnergyFormatterProvider(energyFormatter) {
            MymymealTheme {
                PreviewReleaseDialog()
                TranslationWarningStartupDialog()

                if (onboardingFinished) {
                    Surface {
                        MymymealAppNavHost(onDatabaseBackup)
                        AppUpdateChangelogModalBottomSheet()
                    }
                } else {
                    Onboarding(onFinish = viewModel::finishOnboarding)
                }
            }
        }
    }
}
