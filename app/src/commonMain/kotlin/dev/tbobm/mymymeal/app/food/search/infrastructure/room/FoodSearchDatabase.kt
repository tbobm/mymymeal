package dev.tbobm.mymymeal.app.food.search.infrastructure.room

interface FoodSearchDatabase {
    val foodSearchDao: FoodSearchDao
    val usdaPagingKeyDao: USDAPagingKeyDao
    val openFoodFactsPagingKeyDao: OpenFoodFactsPagingKeyDao
}
