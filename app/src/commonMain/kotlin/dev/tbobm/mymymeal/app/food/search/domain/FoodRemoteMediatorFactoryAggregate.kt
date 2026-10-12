package dev.tbobm.mymymeal.app.food.search.domain

interface FoodRemoteMediatorFactoryAggregate {
    val openFoodFactsRemoteMediatorFactory: ProductRemoteMediatorFactory
    val usdaRemoteMediatorFactory: ProductRemoteMediatorFactory
}
