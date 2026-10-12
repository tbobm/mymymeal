package dev.tbobm.mymymeal.app.food.search.infrastructure.openfoodfacts

import androidx.paging.ExperimentalPagingApi
import androidx.paging.RemoteMediator
import dev.tbobm.mymymeal.app.common.domain.database.TransactionProvider
import dev.tbobm.mymymeal.app.common.domain.date.DateProvider
import dev.tbobm.mymymeal.app.common.domain.search.SearchQuery
import dev.tbobm.mymymeal.app.common.log.Logger
import dev.tbobm.mymymeal.app.food.domain.repository.FoodHistoryRepository
import dev.tbobm.mymymeal.app.food.domain.repository.ProductRepository
import dev.tbobm.mymymeal.app.food.infrastructure.network.RemoteProductMapper
import dev.tbobm.mymymeal.app.food.infrastructure.openfoodfacts.OpenFoodFactsProductMapper
import dev.tbobm.mymymeal.app.food.infrastructure.openfoodfacts.OpenFoodFactsRemoteDataSource
import dev.tbobm.mymymeal.app.food.search.domain.ProductRemoteMediatorFactory
import dev.tbobm.mymymeal.app.food.search.infrastructure.room.OpenFoodFactsPagingKeyDao

@OptIn(ExperimentalPagingApi::class)
internal class OpenFoodFactsRemoteMediatorFactory(
    private val transactionProvider: TransactionProvider,
    private val productRepository: ProductRepository,
    private val foodHistoryRepository: FoodHistoryRepository,
    private val remoteDataSource: OpenFoodFactsRemoteDataSource,
    private val pagingKeyDao: OpenFoodFactsPagingKeyDao,
    private val offMapper: OpenFoodFactsProductMapper,
    private val remoteMapper: RemoteProductMapper,
    private val dateProvider: DateProvider,
    private val logger: Logger,
) : ProductRemoteMediatorFactory {
    override suspend fun <K : Any, T : Any> create(
        query: SearchQuery,
        pageSize: Int,
    ): RemoteMediator<K, T>? {
        if (query !is SearchQuery.NotBlank) {
            return null
        }

        return OpenFoodFactsRemoteMediator(
            query = query.query,
            country = null,
            isBarcode = query is SearchQuery.Barcode,
            transactionProvider = transactionProvider,
            productRepository = productRepository,
            foodHistoryRepository = foodHistoryRepository,
            remoteDataSource = remoteDataSource,
            pagingKeyDao = pagingKeyDao,
            offMapper = offMapper,
            remoteMapper = remoteMapper,
            dateProvider = dateProvider,
            logger = logger,
        )
    }
}
