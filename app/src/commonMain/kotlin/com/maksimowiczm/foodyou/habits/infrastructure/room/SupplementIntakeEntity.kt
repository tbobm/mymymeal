package com.maksimowiczm.foodyou.habits.infrastructure.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** Presence of a row means the supplement was taken on that [date]. No dose, no timestamp. */
@Entity(
    tableName = "SupplementIntake",
    primaryKeys = ["supplementId", "date"],
    foreignKeys =
        [
            ForeignKey(
                entity = SupplementEntity::class,
                parentColumns = ["id"],
                childColumns = ["supplementId"],
                onDelete = ForeignKey.CASCADE,
            )
        ],
    indices = [Index("supplementId")],
)
data class SupplementIntakeEntity(
    val supplementId: Long,
    /** Epoch day (local date), matching the existing `epochDay` convention on `Measurement`. */
    val date: Long,
)
