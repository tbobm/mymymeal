package com.maksimowiczm.foodyou.app.ui.home.habits

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.food.search.FoodSearchApp
import com.maksimowiczm.foodyou.fooddiary.domain.repository.MealRepository
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun PickDefaultCoffeeScreen(
    onDone: () -> Unit,
    onUpdateUsdaApiKey: () -> Unit,
    onUpdateOpenFoodFactsCredentials: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: HabitsCardViewModel = koinViewModel()
    val mealRepository: MealRepository = koinInject()
    val meals by
        remember { mealRepository.observeMeals() }.collectAsStateWithLifecycle(initialValue = emptyList())

    FoodSearchApp(
        onFoodClick = { food, measurement ->
            val mealId = meals.firstOrNull()?.id ?: return@FoodSearchApp
            // `food.id` is already a `FoodId` (see FoodSearch.kt: `val id: FoodId`) -- no
            // reconstruction needed, unlike the DataStore-persisted form in HabitsPreferences.
            viewModel.setDefaultCoffee(foodId = food.id, measurement = measurement, mealId = mealId)
            onDone()
        },
        onUpdateUsdaApiKey = onUpdateUsdaApiKey,
        onUpdateOpenFoodFactsCredentials = onUpdateOpenFoodFactsCredentials,
        modifier = modifier,
    )
}
