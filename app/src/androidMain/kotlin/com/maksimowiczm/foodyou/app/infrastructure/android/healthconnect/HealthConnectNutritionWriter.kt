package com.maksimowiczm.foodyou.app.infrastructure.android.healthconnect

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.MealType
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Mass
import com.maksimowiczm.foodyou.fooddiary.domain.healthexport.HealthMealType
import com.maksimowiczm.foodyou.fooddiary.domain.healthexport.NutritionExportRecord

/**
 * Writes [NutritionExportRecord]s to Health Connect.
 *
 * Every record's [Metadata.clientRecordId] is `meal:<mealId>:<epochDay>` (see
 * [NutritionExportRecord.clientRecordId]): re-inserting with the same id upserts in place rather
 * than duplicating, so a day can be re-synced freely, and a meal that becomes empty is deleted by
 * that same id — no Health Connect record id needs to be stored anywhere in mymymeal's own
 * database.
 *
 * `WRITE_NUTRITION` alone is sufficient here: Health Connect lets an app read, update, and delete
 * data it wrote itself (identified by `clientRecordId`/package) without also holding the
 * corresponding read permission.
 */
class HealthConnectNutritionWriter(private val client: HealthConnectClient) {
    suspend fun upsert(records: List<NutritionExportRecord>) {
        if (records.isEmpty()) return
        client.insertRecords(records.map { it.toNutritionRecord() })
    }

    suspend fun delete(clientRecordIds: List<String>) {
        if (clientRecordIds.isEmpty()) return
        client.deleteRecords(
            recordType = NutritionRecord::class,
            recordIdsList = emptyList(),
            clientRecordIdsList = clientRecordIds,
        )
    }

    private fun NutritionExportRecord.toNutritionRecord(): NutritionRecord =
        NutritionRecord(
            startTime = start.toJavaInstant(),
            // ponytail: zone offset left unspecified rather than threaded through from
            // NutritionExportRecord — the PRD's only user is single-device, so the instant alone
            // (already computed against the device's zone) is enough. Add real offsets if this
            // ever needs to read correctly across a device's DST boundary from another app.
            startZoneOffset = null,
            endTime = end.toJavaInstant(),
            endZoneOffset = null,
            name = mealName,
            mealType = mealType.toHealthConnectMealType(),
            energy = Energy.kilocalories(energyKcal),
            protein = Mass.grams(proteinGrams),
            totalCarbohydrate = Mass.grams(carbohydrateGrams),
            totalFat = Mass.grams(fatGrams),
            metadata = Metadata.manualEntry(clientRecordId = clientRecordId),
        )
}

/**
 * `kotlin.time.Instant.toJavaInstant()` (`kotlin.time.jdk8`) is `@SinceKotlin("2.3")` and isn't
 * resolvable at this project's configured API version — built manually instead of pulling in the
 * conversion helper.
 */
private fun kotlin.time.Instant.toJavaInstant(): java.time.Instant =
    java.time.Instant.ofEpochSecond(epochSeconds, nanosecondsOfSecond.toLong())

private fun HealthMealType.toHealthConnectMealType(): Int =
    when (this) {
        HealthMealType.Breakfast -> MealType.MEAL_TYPE_BREAKFAST
        HealthMealType.Lunch -> MealType.MEAL_TYPE_LUNCH
        HealthMealType.Dinner -> MealType.MEAL_TYPE_DINNER
        HealthMealType.Snack -> MealType.MEAL_TYPE_SNACK
        HealthMealType.Unknown -> MealType.MEAL_TYPE_UNKNOWN
    }
