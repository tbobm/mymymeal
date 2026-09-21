package com.maksimowiczm.foodyou.habits.infrastructure.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CoffeeIntakeDao {
    @Insert abstract suspend fun insert(intake: CoffeeIntakeEntity): Long

    @Query("SELECT * FROM CoffeeIntake WHERE dateEpochDay = :date ORDER BY createdEpochSeconds DESC")
    abstract fun observeForDate(date: Long): Flow<List<CoffeeIntakeEntity>>

    @Query("SELECT COALESCE(SUM(caffeineMg), 0) FROM CoffeeIntake WHERE dateEpochDay = :date")
    abstract fun observeCaffeineMgForDate(date: Long): Flow<Int>

    @Query("DELETE FROM CoffeeIntake WHERE id = :id")
    abstract suspend fun deleteById(id: Long)
}
