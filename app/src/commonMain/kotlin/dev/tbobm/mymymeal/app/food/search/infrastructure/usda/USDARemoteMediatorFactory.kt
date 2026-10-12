package dev.tbobm.mymymeal.app.food.search.infrastructure.usda

import androidx.paging.ExperimentalPagingApi
import androidx.paging.RemoteMediator
import dev.tbobm.mymymeal.app.common.domain.database.TransactionProvider
import dev.tbobm.mymymeal.app.common.domain.date.DateProvider
import dev.tbobm.mymymeal.app.common.domain.search.SearchQuery
import dev.tbobm.mymymeal.app.common.domain.userpreferences.UserPreferencesRepository
import dev.tbobm.mymymeal.app.common.log.Logger
import dev.tbobm.mymymeal.app.food.domain.repository.FoodHistoryRepository
import dev.tbobm.mymymeal.app.food.domain.repository.ProductRepository
import dev.tbobm.mymymeal.app.food.infrastructure.network.RemoteProductMapper
import dev.tbobm.mymymeal.app.food.infrastructure.usda.USDAMapper
import dev.tbobm.mymymeal.app.food.infrastructure.usda.USDARemoteDataSource
import dev.tbobm.mymymeal.app.food.search.domain.FoodSearchPreferences
import dev.tbobm.mymymeal.app.food.search.domain.ProductRemoteMediatorFactory
import dev.tbobm.mymymeal.app.food.search.infrastructure.room.USDAPagingKeyDao
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalPagingApi::class)
internal class USDARemoteMediatorFactory(
    private val foodSearchPreferencesRepository: UserPreferencesRepository<FoodSearchPreferences>,
    private val transactionProvider: TransactionProvider,
    private val productRepository: ProductRepository,
    private val historyRepository: FoodHistoryRepository,
    private val remoteDataSource: USDARemoteDataSource,
    private val pagingKeyDao: USDAPagingKeyDao,
    private val usdaMapper: USDAMapper,
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

        return USDARemoteMediator(
            query = query.query,
            apiKey = foodSearchPreferencesRepository.observe().first().usda.apiKey,
            transactionProvider = transactionProvider,
            productRepository = productRepository,
            historyRepository = historyRepository,
            remoteDataSource = remoteDataSource,
            pagingKeyDao = pagingKeyDao,
            productMapper = usdaMapper,
            remoteMapper = remoteMapper,
            dateProvider = dateProvider,
            logger = logger,
        )
    }
}
