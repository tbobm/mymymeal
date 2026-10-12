package dev.tbobm.mymymeal.app.food.domain.usecase

import dev.tbobm.mymymeal.app.common.domain.database.TransactionProvider
import dev.tbobm.mymymeal.app.common.domain.food.FoodSource
import dev.tbobm.mymymeal.app.common.domain.food.NutritionFacts
import dev.tbobm.mymymeal.app.common.log.Logger
import dev.tbobm.mymymeal.app.common.log.logAndReturnFailure
import dev.tbobm.mymymeal.app.common.result.Ok
import dev.tbobm.mymymeal.app.common.result.Result
import dev.tbobm.mymymeal.app.food.domain.entity.FoodHistory
import dev.tbobm.mymymeal.app.food.domain.entity.FoodId
import dev.tbobm.mymymeal.app.food.domain.repository.FoodHistoryRepository
import dev.tbobm.mymymeal.app.food.domain.repository.ProductRepository

sealed interface CreateProductError {
    object NameEmpty : CreateProductError
}

class CreateProductUseCase(
    private val productRepository: ProductRepository,
    private val historyRepository: FoodHistoryRepository,
    private val transactionProvider: TransactionProvider,
    private val logger: Logger,
) {
    suspend fun create(
        name: String,
        brand: String?,
        barcode: String?,
        note: String?,
        isLiquid: Boolean,
        packageWeight: Double?,
        servingWeight: Double?,
        source: FoodSource,
        nutritionFacts: NutritionFacts,
        history: FoodHistory.CreationHistory,
    ): Result<FoodId.Product, CreateProductError> {
        if (name.isBlank()) {
            return logger.logAndReturnFailure(
                tag = TAG,
                throwable = null,
                error = CreateProductError.NameEmpty,
                message = { "Product name cannot be empty." },
            )
        }

        return transactionProvider.withTransaction {
            val productId =
                productRepository.insertProduct(
                    name = name,
                    brand = brand?.ifBlank { null },
                    barcode = barcode?.ifBlank { null },
                    note = note?.ifBlank { null },
                    isLiquid = isLiquid,
                    packageWeight = packageWeight,
                    servingWeight = servingWeight,
                    source = source,
                    nutritionFacts = nutritionFacts,
                )

            historyRepository.insert(productId, history)

            Ok(productId)
        }
    }

    private companion object {
        const val TAG = "CreateProductUseCase"
    }
}
