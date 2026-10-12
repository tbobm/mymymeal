package dev.tbobm.mymymeal.app.fooddiary.infrastructure.room

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.tbobm.mymymeal.app.common.infrastructure.room.FoodSourceType
import dev.tbobm.mymymeal.app.common.infrastructure.room.Minerals
import dev.tbobm.mymymeal.app.common.infrastructure.room.Nutrients
import dev.tbobm.mymymeal.app.common.infrastructure.room.Vitamins

@Entity(tableName = "DiaryProduct")
data class DiaryProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @Embedded val nutrients: Nutrients,
    @Embedded val vitamins: Vitamins,
    @Embedded val minerals: Minerals,
    val packageWeight: Double?,
    val servingWeight: Double?,
    val isLiquid: Boolean,
    val sourceType: FoodSourceType,
    val sourceUrl: String?,
    val note: String?,

    // Cost snapshot (PRD 1.3), mirroring the nutrition-snapshot pattern: per-unit, immutable once
    // written, exactly like `nutrients` above.
    val unitCost: Double? = null,
    val currency: String? = null,
)
