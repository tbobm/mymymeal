package dev.tbobm.mymymeal.app.app.ui.food.product

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import dev.tbobm.mymymeal.app.app.ui.food.product.create.CreateProductApp
import dev.tbobm.mymymeal.app.app.ui.food.product.create.CreateProductEvent
import dev.tbobm.mymymeal.app.app.ui.food.product.create.CreateProductViewModel
import dev.tbobm.mymymeal.app.common.compose.extension.LaunchedCollectWithLifecycle
import dev.tbobm.mymymeal.app.food.domain.entity.FoodId
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreateProductScreen(
    onBack: () -> Unit,
    onCreate: (FoodId.Product) -> Unit,
    onUpdateUsdaApiKey: () -> Unit,
    onUpdateOpenFoodFactsCredentials: () -> Unit,
    modifier: Modifier = Modifier,
    url: String? = null,
) {
    val viewModel: CreateProductViewModel = koinViewModel()

    val latestOnCreate by rememberUpdatedState(onCreate)
    LaunchedCollectWithLifecycle(viewModel.events) { event ->
        when (event) {
            is CreateProductEvent.Created -> latestOnCreate(event.productId)
        }
    }

    CreateProductApp(
        onBack = onBack,
        onCreate = viewModel::createProduct,
        onUpdateUsdaApiKey = onUpdateUsdaApiKey,
        onUpdateOpenFoodFactsCredentials = onUpdateOpenFoodFactsCredentials,
        modifier = modifier,
        url = url,
    )
}
