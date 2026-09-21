package com.maksimowiczm.foodyou.habits.infrastructure.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Supplement")
data class SupplementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sortOrder: Int,
    /** Whether intake for this supplement carries a dose (grams) or is presence-only. */
    val tracksDose: Boolean = false,
)
