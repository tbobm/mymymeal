package dev.tbobm.mymymeal.app.food.infrastructure.openfoodfacts.model

interface OpenFoodPageResponse {
    val count: Int
    val page: Int
    val pageSize: Int
    val products: List<OpenFoodFactsProduct>
}
