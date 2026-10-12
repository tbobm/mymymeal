package dev.tbobm.mymymeal.app.food.domain.usecase

import dev.tbobm.mymymeal.app.common.domain.database.TransactionProvider
import dev.tbobm.mymymeal.app.common.domain.food.FoodSource
import dev.tbobm.mymymeal.app.common.domain.food.NutritionFacts
import dev.tbobm.mymymeal.app.common.log.Logger
import dev.tbobm.mymymeal.app.common.log.logAndReturnFailure
import dev.tbobm.mymymeal.app.common.result.Ok
import dev.tbobm.mymymeal.app.common.result.Result
import dev.tbobm.mymymeal.app.food.domain.entity.FoodId
import dev.tbobm.mymymeal.app.food.domain.repository.ProductRepository
import kotlinx.coroutines.flow.first

sealed interface UpdateProductError {
    data object NameEmpty : UpdateProductError

    data class ProductNotFound(val id: FoodId.Product) : UpdateProductError
}

class UpdateProductUseCase(
    private val productRepository: ProductRepository,
    private val transactionProvider: TransactionProvider,
    private val logger: Logger,
) {
    suspend fun update(
        id: FoodId.Product,
        name: String,
        brand: String?,
        nutritionFacts: NutritionFacts,
        barcode: String?,
        packageWeight: Double?,
        servingWeight: Double?,
        note: String?,
        source: FoodSource,
        isLiquid: Boolean,
    ): Result<Unit, UpdateProductError> {
        if (name.isBlank()) {
            return logger.logAndReturnFailure(
                tag = TAG,
                throwable = null,
                error = UpdateProductError.NameEmpty,
                message = { "Product name cannot be empty." },
            )
        }

        return transactionProvider.withTransaction {
            val product = productRepository.observeProduct(id).first()
            if (product == null) {
                return@withTransaction logger.logAndReturnFailure(
                    tag = TAG,
                    throwable = null,
                    error = UpdateProductError.ProductNotFound(id),
                    message = { "Product with ID $id not found." },
                )
            }

            val updatedProduct =
                product.copy(
                    name = name,
                    brand = brand?.ifBlank { null },
                    barcode = barcode?.ifBlank { null },
                    nutritionFacts = nutritionFacts,
                    packageWeight = packageWeight,
                    servingWeight = servingWeight,
                    note = note?.ifBlank { null },
                    source = source,
                    isLiquid = isLiquid,
                )

            productRepository.updateProduct(updatedProduct)

            Ok(Unit)
        }
    }

    private companion object {
        const val TAG = "UpdateProductUseCase"
    }
}
