package dev.tbobm.mymymeal.app.app.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dev.tbobm.mymymeal.app.app.navigation.DownloadProductAppNavHost.CreateProduct
import dev.tbobm.mymymeal.app.app.navigation.DownloadProductAppNavHost.OpenFoodFactsLogin
import dev.tbobm.mymymeal.app.app.navigation.DownloadProductAppNavHost.UsdaApiKey
import dev.tbobm.mymymeal.app.app.ui.database.externaldatabases.OpenFoodFactsLoginDialog
import dev.tbobm.mymymeal.app.app.ui.database.externaldatabases.UpdateUsdaApiKeyDialog
import dev.tbobm.mymymeal.app.app.ui.food.product.CreateProductScreen
import kotlinx.serialization.Serializable

@Composable
fun DownloadProductAppNavHost(
    onBack: () -> Unit,
    onCreate: () -> Unit,
    url: String,
    modifier: Modifier = Modifier.Companion,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = CreateProduct(url),
        modifier = modifier,
    ) {
        dialog<UsdaApiKey> {
            UpdateUsdaApiKeyDialog(
                onDismissRequest = { navController.popBackStackInclusive<UsdaApiKey>() },
                onSave = { navController.popBackStackInclusive<UsdaApiKey>() },
            )
        }
        dialog<OpenFoodFactsLogin> {
            OpenFoodFactsLoginDialog(
                onDismissRequest = { navController.popBackStackInclusive<OpenFoodFactsLogin>() },
                onSave = { navController.popBackStackInclusive<OpenFoodFactsLogin>() },
            )
        }
        forwardBackwardComposable<CreateProduct> {
            val (url) = it.toRoute<CreateProduct>()

            CreateProductScreen(
                onBack = onBack,
                onCreate = { onCreate() },
                onUpdateUsdaApiKey = { navController.navigateSingleTop(UsdaApiKey) },
                onUpdateOpenFoodFactsCredentials = {
                    navController.navigateSingleTop(OpenFoodFactsLogin)
                },
                url = url,
            )
        }
    }
}

private object DownloadProductAppNavHost {
    @Serializable object UsdaApiKey

    @Serializable data class CreateProduct(val url: String)

    @Serializable object OpenFoodFactsLogin
}
