package com.maksimowiczm.foodyou.food.infrastructure.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementSuggestionDao {

    @Insert suspend fun insert(measurementSuggestion: MeasurementSuggestionEntity)

    @Query(
        """
        SELECT *
        FROM MeasurementSuggestion
        WHERE
            COALESCE(:productId, -1) = productId
            OR COALESCE(:recipeId, -1) = recipeId
        ORDER BY epochSeconds DESC
        LIMIT :limit
        """
    )
    fun observeByFoodId(
        productId: Long?,
        recipeId: Long?,
        limit: Int,
    ): Flow<List<MeasurementSuggestionEntity>>

    @Query(
        """
        SELECT productId, recipeId, headline, type, value, epochSeconds
        FROM (
            SELECT
                s.productId, s.recipeId,
                CASE
                    WHEN p.brand IS NOT NULL THEN p.name || ' (' || p.brand || ')'
                    ELSE p.name
                END AS headline,
                s.type, s.value, s.epochSeconds
            FROM LatestMeasurementSuggestion s
            JOIN Product p ON s.productId = p.id
            WHERE s.productId IS NOT NULL
            UNION ALL
            SELECT s.productId, s.recipeId, r.name AS headline, s.type, s.value, s.epochSeconds
            FROM LatestMeasurementSuggestion s
            JOIN Recipe r ON s.recipeId = r.id
            WHERE s.recipeId IS NOT NULL
        )
        ORDER BY epochSeconds DESC
        LIMIT :limit
        """
    )
    fun observeRecentFoods(limit: Int): Flow<List<RecentFoodSuggestion>>
}
