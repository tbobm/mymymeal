package dev.tbobm.mymymeal.app.habits.infrastructure.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One discrete coffee tap. Not a food/diary entry -- [caffeineMg] is snapshotted at log time from
 * [dev.tbobm.mymymeal.app.habits.domain.entity.CoffeeType]'s hardcoded default, so a later
 * change to that constant never rewrites history.
 */
@Entity(tableName = "CoffeeIntake", indices = [Index("dateEpochDay")])
data class CoffeeIntakeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val createdEpochSeconds: Long,
    /** [dev.tbobm.mymymeal.app.habits.domain.entity.CoffeeType] name. */
    val type: String,
    val caffeineMg: Int,
)
