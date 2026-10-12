package dev.tbobm.mymymeal.app.fooddiary.domain.usecase

import dev.tbobm.mymymeal.app.common.domain.database.TransactionProvider
import dev.tbobm.mymymeal.app.common.domain.date.DateProvider
import dev.tbobm.mymymeal.app.common.domain.measurement.Measurement
import dev.tbobm.mymymeal.app.common.log.Logger
import dev.tbobm.mymymeal.app.common.log.logAndReturnFailure
import dev.tbobm.mymymeal.app.common.result.Ok
import dev.tbobm.mymymeal.app.common.result.Result
import dev.tbobm.mymymeal.app.fooddiary.domain.entity.FoodDiaryEntryId
import dev.tbobm.mymymeal.app.fooddiary.domain.repository.FoodDiaryEntryRepository
import dev.tbobm.mymymeal.app.fooddiary.domain.repository.MealRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.LocalDate

sealed interface UpdateFoodDiaryEntryError {
    data object EntryNotFoundFood : UpdateFoodDiaryEntryError

    data object InvalidMeasurement : UpdateFoodDiaryEntryError

    data object MealNotFound : UpdateFoodDiaryEntryError
}

class UpdateFoodDiaryEntryUseCase(
    private val mealRepository: MealRepository,
    private val entryRepository: FoodDiaryEntryRepository,
    private val dateProvider: DateProvider,
    private val transactionProvider: TransactionProvider,
    private val logger: Logger,
) {
    suspend fun update(
        id: FoodDiaryEntryId,
        measurement: Measurement,
        mealId: Long,
        date: LocalDate,
    ): Result<Unit, UpdateFoodDiaryEntryError> {
        return transactionProvider.withTransaction {
            val entry = entryRepository.observe(id).firstOrNull()

            if (entry == null) {
                return@withTransaction logger.logAndReturnFailure(
                    tag = TAG,
                    error = UpdateFoodDiaryEntryError.EntryNotFoundFood,
                    message = { "Diary entry with id $id not found" },
                )
            }

            when (measurement) {
                is Measurement.Gram,
                is Measurement.Ounce ->
                    if (entry.food.isLiquid) {
                        return@withTransaction logger.logAndReturnFailure(
                            tag = TAG,
                            error = UpdateFoodDiaryEntryError.InvalidMeasurement,
                            message = { "Food must not be liquid for gram measurement" },
                        )
                    }

                is Measurement.Milliliter,
                is Measurement.FluidOunce ->
                    if (!entry.food.isLiquid) {
                        return@withTransaction logger.logAndReturnFailure(
                            tag = TAG,
                            error = UpdateFoodDiaryEntryError.InvalidMeasurement,
                            message = { "Food must be liquid for milliliter measurement" },
                        )
                    }

                is Measurement.Package ->
                    if (entry.food.totalWeight == null) {
                        return@withTransaction logger.logAndReturnFailure(
                            tag = TAG,
                            error = UpdateFoodDiaryEntryError.InvalidMeasurement,
                            message = { "Total weight must be provided for package measurement" },
                        )
                    }

                is Measurement.Serving ->
                    if (entry.food.servingWeight == null) {
                        return@withTransaction logger.logAndReturnFailure(
                            tag = TAG,
                            error = UpdateFoodDiaryEntryError.InvalidMeasurement,
                            message = { "Total weight must be provided for serving measurement" },
                        )
                    }
            }

            val meal = mealRepository.observeMeal(mealId).firstOrNull()

            if (meal == null) {
                return@withTransaction logger.logAndReturnFailure(
                    tag = TAG,
                    error = UpdateFoodDiaryEntryError.MealNotFound,
                    message = { "Meal with ID $mealId not found" },
                )
            }

            val updated =
                entry.copy(
                    measurement = measurement,
                    mealId = mealId,
                    date = date,
                    updatedAt = dateProvider.now(),
                )

            entryRepository.update(updated)
            Ok(Unit)
        }
    }

    private companion object {
        const val TAG = "UpdateFoodDiaryEntryUseCase"
    }
}
