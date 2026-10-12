package dev.tbobm.mymymeal.app.food.search.infrastructure

import dev.tbobm.mymymeal.app.food.search.domain.FoodRemoteMediatorFactoryAggregate
import dev.tbobm.mymymeal.app.food.search.infrastructure.openfoodfacts.OpenFoodFactsRemoteMediatorFactory
import dev.tbobm.mymymeal.app.food.search.infrastructure.usda.USDARemoteMediatorFactory

internal class FoodRemoteMediatorFactoryAggregateImpl(
    override val usdaRemoteMediatorFactory: USDARemoteMediatorFactory,
    override val openFoodFactsRemoteMediatorFactory: OpenFoodFactsRemoteMediatorFactory,
) : FoodRemoteMediatorFactoryAggregate
