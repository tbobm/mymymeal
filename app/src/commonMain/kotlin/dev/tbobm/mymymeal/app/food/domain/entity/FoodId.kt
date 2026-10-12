package dev.tbobm.mymymeal.app.food.domain.entity

sealed interface FoodId {
    data class Product(val id: Long) : FoodId

    data class Recipe(val id: Long) : FoodId
}
