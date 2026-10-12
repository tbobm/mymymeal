package dev.tbobm.mymymeal.app.food.domain.repository

import dev.tbobm.mymymeal.app.food.domain.entity.RemoteProductRequest

fun interface RemoteProductRequestFactory {
    suspend fun create(url: String): RemoteProductRequest?
}
