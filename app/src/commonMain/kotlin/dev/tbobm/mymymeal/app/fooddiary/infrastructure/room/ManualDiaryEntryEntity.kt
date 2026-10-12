package dev.tbobm.mymymeal.app.fooddiary.infrastructure.room

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.tbobm.mymymeal.app.common.infrastructure.room.Minerals
import dev.tbobm.mymymeal.app.common.infrastructure.room.Nutrients
import dev.tbobm.mymymeal.app.common.infrastructure.room.Vitamins

@Entity(
    tableName = "ManualDiaryEntry",
    foreignKeys =
        [
            ForeignKey(
                entity = MealEntity::class,
                parentColumns = ["id"],
                childColumns = ["mealId"],
                onDelete = ForeignKey.CASCADE,
            )
        ],
    indices = [Index(value = ["mealId"]), Index(value = ["dateEpochDay"])],
)
data class ManualDiaryEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mealId: Long,
    val dateEpochDay: Long,
    val name: String,
    @Embedded val nutrients: Nutrients,
    @Embedded val vitamins: Vitamins,
    @Embedded val minerals: Minerals,
    val createdEpochSeconds: Long,
    val updatedEpochSeconds: Long,

    // Provenance (PRD 1.2). Every row in this table is a manual estimate by definition.
    val sourceKind: String = "manual_estimate",
    val confidence: String = "estimated",

    // Cost snapshot (PRD 1.3), mirroring the nutrition-snapshot pattern -- per-unit, not a
    // pre-resolved total, since this table has no separate "diary copy" to hold it instead.
    val unitCost: Double? = null,
    val currency: String? = null,
)
