package dev.tbobm.mymymeal.app.food.infrastructure.room

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.tbobm.mymymeal.app.common.infrastructure.room.FoodSourceType
import dev.tbobm.mymymeal.app.common.infrastructure.room.Minerals
import dev.tbobm.mymymeal.app.common.infrastructure.room.Nutrients
import dev.tbobm.mymymeal.app.common.infrastructure.room.Vitamins

@Entity(tableName = "Product")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val brand: String?,
    val barcode: String?,
    @Embedded val nutrients: Nutrients,
    @Embedded val vitamins: Vitamins,
    @Embedded val minerals: Minerals,
    val packageWeight: Double?,
    val servingWeight: Double?,
    val note: String?,
    val sourceType: FoodSourceType,
    val sourceUrl: String? = null,
    val isLiquid: Boolean,

    // PRD 1.3. No cost reporting built on these yet -- deferred to Phase 4.
    val pricePerUnit: Double? = null,
    val currency: String? = null,
)
