package dev.tbobm.mymymeal.app.app.ui

import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.tbobm.mymymeal.app.app.navigation.DownloadProductAppNavHost
import dev.tbobm.mymymeal.app.app.ui.onboarding.Onboarding
import dev.tbobm.mymymeal.app.app.ui.theme.MymymealTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DownloadProductApp(onBack: () -> Unit, onCreate: () -> Unit, url: String) {
    val viewModel: AppViewModel = koinViewModel()
    val onboardingFinished by viewModel.onboardingFinished.collectAsStateWithLifecycle()

    MymymealTheme {
        Surface {
            if (!onboardingFinished) {
                Onboarding(onFinish = viewModel::finishOnboarding)
            } else {
                DownloadProductAppNavHost(onBack = onBack, onCreate = onCreate, url = url)
            }
        }
    }
}
