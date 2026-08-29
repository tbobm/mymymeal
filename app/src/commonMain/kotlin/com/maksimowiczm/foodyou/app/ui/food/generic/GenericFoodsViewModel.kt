package com.maksimowiczm.foodyou.app.ui.food.generic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.common.domain.food.FoodSource
import com.maksimowiczm.foodyou.food.domain.entity.FoodId
import com.maksimowiczm.foodyou.food.domain.entity.Product
import com.maksimowiczm.foodyou.food.domain.repository.ProductRepository
import com.maksimowiczm.foodyou.food.domain.usecase.DeleteFoodUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Backs the "Generics" management screen: a browsable list of the user's own products (source =
 * User) -- quick-add ingredients with typed-in average nutriments, no third-party source. Create
 * and edit reuse the existing product create/update screens; this view model only lists and
 * deletes.
 */
internal class GenericFoodsViewModel(
    productRepository: ProductRepository,
    private val deleteFoodUseCase: DeleteFoodUseCase,
) : ViewModel() {

    val foods: StateFlow<List<Product>?> =
        productRepository
            .observeProducts(FoodSource.Type.User)
            .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(2_000), initialValue = null)

    fun delete(id: FoodId.Product) {
        viewModelScope.launch { deleteFoodUseCase.delete(id) }
    }
}
