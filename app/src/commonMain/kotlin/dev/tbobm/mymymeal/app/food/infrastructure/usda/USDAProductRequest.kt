package dev.tbobm.mymymeal.app.food.infrastructure.usda

import dev.tbobm.mymymeal.app.common.domain.userpreferences.UserPreferencesRepository
import dev.tbobm.mymymeal.app.common.result.Err
import dev.tbobm.mymymeal.app.common.result.Ok
import dev.tbobm.mymymeal.app.common.result.Result
import dev.tbobm.mymymeal.app.food.domain.entity.RemoteFoodException
import dev.tbobm.mymymeal.app.food.domain.entity.RemoteProduct
import dev.tbobm.mymymeal.app.food.domain.entity.RemoteProductRequest
import dev.tbobm.mymymeal.app.food.search.domain.FoodSearchPreferences
import kotlinx.coroutines.flow.first

internal class USDAProductRequest(
    private val dataSource: USDARemoteDataSource,
    private val id: String,
    private val mapper: USDAMapper,
    private val preferencesRepository: UserPreferencesRepository<FoodSearchPreferences>,
) : RemoteProductRequest {
    private suspend fun apiKey() = preferencesRepository.observe().first().usda.apiKey

    override suspend fun execute(): Result<RemoteProduct, RemoteFoodException> =
        dataSource
            .getFood(id, apiKey = apiKey())
            .map(mapper::toRemoteProduct)
            .fold(onSuccess = ::Ok, onFailure = { Err(RemoteFoodException.fromThrowable(it)) })
}
