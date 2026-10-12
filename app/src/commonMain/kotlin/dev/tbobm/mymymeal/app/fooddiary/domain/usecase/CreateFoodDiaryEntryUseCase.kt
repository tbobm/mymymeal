package dev.tbobm.mymymeal.app.fooddiary.domain.usecase

import dev.tbobm.mymymeal.app.common.domain.database.TransactionProvider
import dev.tbobm.mymymeal.app.common.domain.date.DateProvider
import dev.tbobm.mymymeal.app.common.domain.measurement.Measurement
import dev.tbobm.mymymeal.app.common.log.Logger
import dev.tbobm.mymymeal.app.common.log.logAndReturnFailure
import dev.tbobm.mymymeal.app.common.result.Ok
import dev.tbobm.mymymeal.app.common.result.Result
import dev.tbobm.mymymeal.app.fooddiary.domain.entity.DiaryFood
import dev.tbobm.mymymeal.app.fooddiary.domain.entity.FoodDiaryEntryId
import dev.tbobm.mymymeal.app.fooddiary.domain.repository.FoodDiaryEntryRepository
import dev.tbobm.mymymeal.app.fooddiary.domain.repository.MealRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate

sealed interface CreateFoodDiaryEntryError {
    data object MealNotFound : CreateFoodDiaryEntryError

    data object InvalidMeasurement : CreateFoodDiaryEntryError
}

class CreateFoodDiaryEntryUseCase(
    private val mealRepository: MealRepository,
    private val entryRepository: FoodDiaryEntryRepository,
    private val transactionProvider: TransactionProvider,
    private val dateProvider: DateProvider,
    private val logger: Logger,
) {
    suspend fun createDiaryEntry(
        measurement: Measurement,
        mealId: Long,
        date: LocalDate,
        food: DiaryFood,
    ): Result<FoodDiaryEntryId, CreateFoodDiaryEntryError> {
        when (measurement) {
            is Measurement.Gram,
            is Measurement.Ounce ->
                if (food.isLiquid) {
                    return logger.logAndReturnFailure(
                        tag = TAG,
                        throwable = null,
                        error = CreateFoodDiaryEntryError.InvalidMeasurement,
                        message = { "Food must not be liquid for gram measurement" },
                    )
                }

            is Measurement.Milliliter,
            is Measurement.FluidOunce ->
                if (!food.isLiquid) {
                    return logger.logAndReturnFailure(
                        tag = TAG,
                        throwable = null,
                        error = CreateFoodDiaryEntryError.InvalidMeasurement,
                        message = { "Food must be liquid for milliliter measurement" },
                    )
                }

            is Measurement.Package ->
                if (food.totalWeight == null) {
                    return logger.logAndReturnFailure(
                        tag = TAG,
                        throwable = null,
                        error = CreateFoodDiaryEntryError.InvalidMeasurement,
                        message = { "Total weight must be provided for package measurement" },
                    )
                }

            is Measurement.Serving ->
                if (food.servingWeight == null) {
                    return logger.logAndReturnFailure(
                        tag = TAG,
                        throwable = null,
                        error = CreateFoodDiaryEntryError.InvalidMeasurement,
                        message = { "Total weight must be provided for serving measurement" },
                    )
                }
        }

        val now = dateProvider.now()
        return transactionProvider.withTransaction {
            val meal = mealRepository.observeMeal(mealId).first()

            if (meal == null) {
                return@withTransaction logger.logAndReturnFailure(
                    tag = TAG,
                    throwable = null,
                    error = CreateFoodDiaryEntryError.MealNotFound,
                    message = { "Meal with id $mealId not found" },
                )
            }

            val entryId =
                entryRepository.insert(
                    measurement = measurement,
                    mealId = mealId,
                    date = date,
                    food = food,
                    createdAt = now,
                )

            Ok(entryId)
        }
    }

    private companion object {
        const val TAG = "CreateFoodDiaryEntryUseCase"
    }
}
