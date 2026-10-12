package dev.tbobm.mymymeal.app.food.search.domain

import androidx.paging.ExperimentalPagingApi
import androidx.paging.RemoteMediator
import dev.tbobm.mymymeal.app.common.domain.search.SearchQuery

@OptIn(ExperimentalPagingApi::class)
interface ProductRemoteMediatorFactory {
    suspend fun <K : Any, T : Any> create(query: SearchQuery, pageSize: Int): RemoteMediator<K, T>?
}
