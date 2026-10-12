package dev.tbobm.mymymeal.app.food.domain.entity

import dev.tbobm.mymymeal.app.common.result.Result

interface RemoteProductRequest {
    /** Executes the request to fetch a remote product. */
    suspend fun execute(): Result<RemoteProduct, RemoteFoodException>
}
