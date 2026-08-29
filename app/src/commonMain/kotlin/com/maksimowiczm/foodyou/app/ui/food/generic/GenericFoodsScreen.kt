package com.maksimowiczm.foodyou.app.ui.food.generic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.app.ui.common.component.FoodErrorListItem
import com.maksimowiczm.foodyou.app.ui.common.component.FoodListItem
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalEnergyFormatter
import com.maksimowiczm.foodyou.common.compose.extension.add
import com.maksimowiczm.foodyou.common.compose.utility.formatClipZeros
import com.maksimowiczm.foodyou.food.domain.entity.FoodId
import com.maksimowiczm.foodyou.food.domain.entity.Product
import foodyou.app.generated.resources.*
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * "Generics" management screen: a browsable list of the user's own products (source = User), with
 * create/edit/delete. Create and edit reuse the existing product create/update screens (see nav
 * host); this screen only lists and offers delete.
 */
@Composable
fun GenericFoodsScreen(
    onBack: () -> Unit,
    onCreate: () -> Unit,
    onEdit: (FoodId.Product) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: GenericFoodsViewModel = koinViewModel()
    val foods = viewModel.foods.collectAsStateWithLifecycle().value

    if (foods == null) {
        // TODO loading state
        return
    }

    GenericFoodsScreen(
        onBack = onBack,
        foods = foods,
        onCreate = onCreate,
        onEdit = onEdit,
        onDelete = { viewModel.delete(it.id) },
        modifier = modifier,
    )
}

@Composable
private fun GenericFoodsScreen(
    onBack: () -> Unit,
    foods: List<Product>,
    onCreate: () -> Unit,
    onEdit: (FoodId.Product) -> Unit,
    onDelete: (Product) -> Unit,
    modifier: Modifier = Modifier,
) {
    var foodPendingDelete by remember { mutableStateOf<Product?>(null) }

    foodPendingDelete?.let { food ->
        AlertDialog(
            onDismissRequest = { foodPendingDelete = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(food)
                        foodPendingDelete = null
                    }
                ) {
                    Text(stringResource(Res.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { foodPendingDelete = null }) {
                    Text(stringResource(Res.string.action_cancel))
                }
            },
            icon = { Icon(imageVector = Icons.Default.Delete, contentDescription = null) },
            title = { Text(stringResource(Res.string.headline_delete_food)) },
            text = { Text(stringResource(Res.string.description_delete_food)) },
        )
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.headline_generic_foods)) },
                navigationIcon = { ArrowBackIconButton(onBack) },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreate) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(Res.string.headline_create_product),
                )
            }
        },
    ) { paddingValues ->
        if (foods.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(Res.string.neutral_no_generic_foods),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = paddingValues.add(vertical = 8.dp),
            ) {
                items(items = foods, key = { it.id.id }) { food ->
                    GenericFoodListItem(
                        food = food,
                        onClick = { onEdit(food.id) },
                        onDeleteClick = { foodPendingDelete = food },
                    )
                }
            }
        }
    }
}

@Composable
private fun GenericFoodListItem(food: Product, onClick: () -> Unit, onDeleteClick: () -> Unit) {
    val proteins = food.nutritionFacts.proteins.value
    val carbohydrates = food.nutritionFacts.carbohydrates.value
    val fats = food.nutritionFacts.fats.value
    val energy = food.nutritionFacts.energy.value

    if (proteins == null || carbohydrates == null || fats == null || energy == null) {
        FoodErrorListItem(
            headline = food.headline,
            errorMessage = stringResource(Res.string.error_food_is_missing_required_fields),
            onClick = onClick,
        )
        return
    }

    val g = stringResource(Res.string.unit_gram_short)

    FoodListItem(
        name = { Text(food.headline) },
        proteins = { Text("${proteins.formatClipZeros()} $g") },
        carbohydrates = { Text("${carbohydrates.formatClipZeros()} $g") },
        fats = { Text("${fats.formatClipZeros()} $g") },
        calories = { Text(LocalEnergyFormatter.current.formatEnergy(energy.roundToInt())) },
        measurement = { Text("100 $g") },
        isRecipe = false,
        onClick = onClick,
        trailingContent = {
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.action_delete_food),
                )
            }
        },
    )
}
