package dev.tbobm.mymymeal.app.fooddiary.domain.usecase

import dev.tbobm.mymymeal.app.common.domain.database.TransactionProvider
import dev.tbobm.mymymeal.app.common.domain.date.DateProvider
import dev.tbobm.mymymeal.app.common.domain.measurement.Measurement
import dev.tbobm.mymymeal.app.common.log.Logger
import dev.tbobm.mymymeal.app.common.log.logAndReturnFailure
import dev.tbobm.mymymeal.app.common.result.Ok
import dev.tbobm.mymymeal.app.common.result.Result
import dev.tbobm.mymymeal.app.common.result.onSuccess
import dev.tbobm.mymymeal.app.fooddiary.domain.entity.DiaryFood
import dev.tbobm.mymymeal.app.fooddiary.domain.entity.FoodDiaryEntryId
import dev.tbobm.mymymeal.app.fooddiary.domain.repository.FoodDiaryEntryRepository
import dev.tbobm.mymymeal.app.fooddiary.domain.repository.MealRepository
import dev.tbobm.mymymeal.app.usagelog.UsageCode
import dev.tbobm.mymymeal.app.usagelog.UsageLogger
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
    private val usageLogger: UsageLogger,
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
        val result = transactionProvider.withTransaction {
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
        // Outside the transaction: logging must not extend the writer lock.
        result.onSuccess { usageLogger.log(UsageCode.ACT_LOG_ENTRY, mealId.toInt()) }
        return result
    }

    private companion object {
        const val TAG = "CreateFoodDiaryEntryUseCase"
    }
}
