package dev.tbobm.mymymeal.app.food.infrastructure.network

import dev.tbobm.mymymeal.app.food.domain.entity.RemoteProductRequest
import dev.tbobm.mymymeal.app.food.domain.repository.RemoteProductRequestFactory
import dev.tbobm.mymymeal.app.food.infrastructure.openfoodfacts.OpenFoodFactsFacade
import dev.tbobm.mymymeal.app.food.infrastructure.usda.USDAFacade

internal class RemoteProductRequestFactoryImpl(
    private val openFoodFacts: OpenFoodFactsFacade,
    private val usda: USDAFacade,
) : RemoteProductRequestFactory {
    override suspend fun create(url: String): RemoteProductRequest? =
        when {
            openFoodFacts.matches(url) ->
                openFoodFacts.extractBarcode(url)?.let(openFoodFacts::createRequest)

            usda.matches(url) -> usda.extractId(url)?.let { usda.createRequest(it) }

            else -> null
        }
}
