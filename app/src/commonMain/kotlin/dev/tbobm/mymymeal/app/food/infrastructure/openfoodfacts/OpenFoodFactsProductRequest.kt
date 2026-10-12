package dev.tbobm.mymymeal.app.food.infrastructure.openfoodfacts

import dev.tbobm.mymymeal.app.common.result.Err
import dev.tbobm.mymymeal.app.common.result.Ok
import dev.tbobm.mymymeal.app.common.result.Result
import dev.tbobm.mymymeal.app.food.domain.entity.RemoteFoodException
import dev.tbobm.mymymeal.app.food.domain.entity.RemoteProduct
import dev.tbobm.mymymeal.app.food.domain.entity.RemoteProductRequest

internal class OpenFoodFactsProductRequest(
    private val dataSource: OpenFoodFactsRemoteDataSource,
    private val barcode: String,
    private val mapper: OpenFoodFactsProductMapper,
) : RemoteProductRequest {
    override suspend fun execute(): Result<RemoteProduct, RemoteFoodException> =
        dataSource
            .getProduct(barcode)
            .map(mapper::toRemoteProduct)
            .fold(onSuccess = ::Ok, onFailure = { Err(RemoteFoodException.fromThrowable(it)) })
}
