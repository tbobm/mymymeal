package dev.tbobm.mymymeal.app.app.ui.food.product.create

import dev.tbobm.mymymeal.app.food.domain.entity.FoodId

internal sealed interface CreateProductEvent {

    data class Created(val productId: FoodId.Product) : CreateProductEvent
}
