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
    suspend fun addSupplement(name: String): Long =
        insertSupplement(SupplementEntity(name = name, sortOrder = maxSortOrder() + 1))

    @Query("DELETE FROM Supplement WHERE id = :id")
    abstract suspend fun deleteSupplement(id: Long)

    @Query("SELECT supplementId FROM SupplementIntake WHERE date = :date")
    abstract fun observeIntakeSupplementIdsForDate(date: Long): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertIntake(intake: SupplementIntakeEntity)

    @Query("DELETE FROM SupplementIntake WHERE supplementId = :supplementId AND date = :date")
    protected abstract suspend fun deleteIntake(supplementId: Long, date: Long)

    /** Toggles [supplementId]'s intake row for [date] -- inserts if absent, deletes if present. */
    suspend fun setTaken(supplementId: Long, date: Long, taken: Boolean) {
        if (taken) {
            insertIntake(SupplementIntakeEntity(supplementId, date))
        } else {
            deleteIntake(supplementId, date)
        }
    }
}
