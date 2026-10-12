package dev.tbobm.mymymeal.app.fooddiary.infrastructure.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.tbobm.mymymeal.app.common.domain.measurement.MeasurementType

@Entity(
    tableName = "Measurement",
    foreignKeys =
        [
            ForeignKey(
                entity = MealEntity::class,
                parentColumns = ["id"],
                childColumns = ["mealId"],
                onDelete = ForeignKey.CASCADE,
            )
        ],
    indices = [Index(value = ["mealId"]), Index(value = ["epochDay"])],
)
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mealId: Long,
    val epochDay: Long,

    // Product or Recipe
    val productId: Long?,
    val recipeId: Long?,
    val measurement: MeasurementType,
    val quantity: Double,

    /** Epoch seconds */
    val createdAt: Long,
    /** Epoch seconds */
    val updatedAt: Long,

    // Provenance (PRD 1.2). Null for every row logged before this column existed -- there is no
    // historical record of which UI action was used, except sourceKind = "recipe" for rows with
    // a non-null recipeId, which is backfilled on migration.
    val sourceKind: String? = null,
    val confidence: String? = null,

    // Soft reference to the catalog food this entry originated from, for convenience only.
    // Deliberately not a foreign key: nothing on a read path may depend on it resolving.
    val originProductId: Long? = null,
    val originRecipeId: Long? = null,
)
