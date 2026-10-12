package dev.tbobm.mymymeal.app.app.ui.food.product.update

internal sealed interface UpdateProductEvent {

    data object Updated : UpdateProductEvent
}
