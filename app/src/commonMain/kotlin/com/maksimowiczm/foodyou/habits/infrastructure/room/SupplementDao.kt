package com.maksimowiczm.foodyou.habits.infrastructure.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SupplementDao {
    @Query("SELECT * FROM Supplement ORDER BY sortOrder ASC")
    abstract fun observeSupplements(): Flow<List<SupplementEntity>>

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM Supplement")
    protected abstract suspend fun maxSortOrder(): Int

    @Insert protected abstract suspend fun insertSupplement(supplement: SupplementEntity): Long

    /** Appends a new supplement after the current highest [SupplementEntity.sortOrder]. */
    suspend fun addSupplement(name: String, tracksDose: Boolean): Long =
        insertSupplement(
            SupplementEntity(name = name, sortOrder = maxSortOrder() + 1, tracksDose = tracksDose)
        )

    @Query("DELETE FROM Supplement WHERE id = :id")
    abstract suspend fun deleteSupplement(id: Long)

    @Query("SELECT * FROM SupplementIntake WHERE date = :date")
    abstract fun observeIntakeForDate(date: Long): Flow<List<SupplementIntakeEntity>>

    /** Replaces any existing row for the day, so re-logging a dose updates rather than duplicates. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertIntake(intake: SupplementIntakeEntity)

    @Query("DELETE FROM SupplementIntake WHERE supplementId = :supplementId AND date = :date")
    protected abstract suspend fun deleteIntake(supplementId: Long, date: Long)

    /**
     * Sets [supplementId]'s intake row for [date] -- inserts/replaces if [taken], deletes if not.
     * [doseGrams] is only meaningful when the supplement tracks a dose.
     */
    suspend fun setIntake(supplementId: Long, date: Long, taken: Boolean, doseGrams: Double?) {
        if (taken) {
            insertIntake(SupplementIntakeEntity(supplementId, date, doseGrams))
        } else {
            deleteIntake(supplementId, date)
        }
    }
}
