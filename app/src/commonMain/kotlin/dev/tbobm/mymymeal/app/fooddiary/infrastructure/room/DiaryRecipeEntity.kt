package dev.tbobm.mymymeal.app.fooddiary.infrastructure.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "DiaryRecipe")
data class DiaryRecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val servings: Int,
    val isLiquid: Boolean,
    val note: String?,

    // Cost snapshot (PRD 1.3), mirroring the nutrition-snapshot pattern.
    val unitCost: Double? = null,
    val currency: String? = null,
)
