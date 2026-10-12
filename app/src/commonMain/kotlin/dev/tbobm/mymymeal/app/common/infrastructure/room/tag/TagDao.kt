package dev.tbobm.mymymeal.app.common.infrastructure.room.tag

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TagDao {
    @Query("SELECT * FROM Tag ORDER BY name COLLATE NOCASE ASC")
    abstract fun observeTags(): Flow<List<TagEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM Tag WHERE name = :name)")
    protected abstract suspend fun existsTagByName(name: String): Boolean

    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract suspend fun insertTagUnchecked(tag: TagEntity): Long

    /** Returns the new tag's id, or null if a tag with this name already exists. */
    @Transaction
    open suspend fun insertTag(tag: TagEntity): Long? =
        if (existsTagByName(tag.name)) null else insertTagUnchecked(tag)

    @Update abstract suspend fun updateTag(tag: TagEntity)

    @Query("DELETE FROM Tag WHERE id = :id") abstract suspend fun deleteTag(id: Long)

    @Query(
        "SELECT * FROM Tag WHERE id IN (SELECT tagId FROM ProductTagCrossRef WHERE productId = :productId)"
    )
    abstract fun observeTagsForProduct(productId: Long): Flow<List<TagEntity>>

    @Query(
        "SELECT * FROM Tag WHERE id IN (SELECT tagId FROM RecipeTagCrossRef WHERE recipeId = :recipeId)"
    )
    abstract fun observeTagsForRecipe(recipeId: Long): Flow<List<TagEntity>>

    @Query(
        "SELECT * FROM Tag WHERE id IN " +
            "(SELECT tagId FROM ManualDiaryEntryTagCrossRef WHERE manualDiaryEntryId = :manualDiaryEntryId)"
    )
    abstract fun observeTagsForManualDiaryEntry(
        manualDiaryEntryId: Long
    ): Flow<List<TagEntity>>

    @Query("DELETE FROM ProductTagCrossRef WHERE productId = :productId")
    protected abstract suspend fun clearProductTags(productId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertProductTags(crossRefs: List<ProductTagCrossRefEntity>)

    /** Replaces the full tag set for [productId] with [tagIds] in one transaction. */
    @Transaction
    open suspend fun setProductTags(productId: Long, tagIds: Set<Long>) {
        clearProductTags(productId)
        insertProductTags(tagIds.map { ProductTagCrossRefEntity(productId, it) })
    }

    @Query("DELETE FROM RecipeTagCrossRef WHERE recipeId = :recipeId")
    protected abstract suspend fun clearRecipeTags(recipeId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertRecipeTags(crossRefs: List<RecipeTagCrossRefEntity>)

    @Transaction
    open suspend fun setRecipeTags(recipeId: Long, tagIds: Set<Long>) {
        clearRecipeTags(recipeId)
        insertRecipeTags(tagIds.map { RecipeTagCrossRefEntity(recipeId, it) })
    }

    @Query("DELETE FROM ManualDiaryEntryTagCrossRef WHERE manualDiaryEntryId = :manualDiaryEntryId")
    protected abstract suspend fun clearManualDiaryEntryTags(manualDiaryEntryId: Long)

    /**
     * Product ids carrying any of [tagIds]. Used by food search filtering (PRD 3.5) to intersect
     * against already-paged results client-side, rather than threading a tag filter through the
     * search DAO's UNION/CTE queries.
     */
    @Query("SELECT DISTINCT productId FROM ProductTagCrossRef WHERE tagId IN (:tagIds)")
    abstract fun observeProductIdsWithAnyTag(tagIds: Set<Long>): Flow<List<Long>>

    /** Recipe ids carrying any of [tagIds]. See [observeProductIdsWithAnyTag]. */
    @Query("SELECT DISTINCT recipeId FROM RecipeTagCrossRef WHERE tagId IN (:tagIds)")
    abstract fun observeRecipeIdsWithAnyTag(tagIds: Set<Long>): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertManualDiaryEntryTags(
        crossRefs: List<ManualDiaryEntryTagCrossRefEntity>
    )

    @Transaction
    open suspend fun setManualDiaryEntryTags(manualDiaryEntryId: Long, tagIds: Set<Long>) {
        clearManualDiaryEntryTags(manualDiaryEntryId)
        insertManualDiaryEntryTags(
            tagIds.map { ManualDiaryEntryTagCrossRefEntity(manualDiaryEntryId, it) }
        )
    }
}
