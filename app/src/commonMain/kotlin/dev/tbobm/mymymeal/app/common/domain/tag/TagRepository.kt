package dev.tbobm.mymymeal.app.common.domain.tag

import kotlinx.coroutines.flow.Flow

/** PRD 3.5 (categorisation). CRUD on tags plus assignment to foods and manual diary entries. */
interface TagRepository {
    fun observeTags(): Flow<List<Tag>>

    /** Returns the new tag's id, or null if a tag with this name already exists. */
    suspend fun createTag(name: String): Long?

    suspend fun renameTag(id: Long, name: String)

    suspend fun deleteTag(id: Long)

    fun observeTagsForProduct(productId: Long): Flow<List<Tag>>

    fun observeTagsForRecipe(recipeId: Long): Flow<List<Tag>>

    fun observeTagsForManualDiaryEntry(manualDiaryEntryId: Long): Flow<List<Tag>>

    suspend fun setProductTags(productId: Long, tagIds: Set<Long>)

    suspend fun setRecipeTags(recipeId: Long, tagIds: Set<Long>)

    suspend fun setManualDiaryEntryTags(manualDiaryEntryId: Long, tagIds: Set<Long>)

    /** Ids of products carrying any tag in [tagIds]. Empty [tagIds] yields an empty result. */
    fun observeProductIdsWithAnyTag(tagIds: Set<Long>): Flow<Set<Long>>

    /** Ids of recipes carrying any tag in [tagIds]. Empty [tagIds] yields an empty result. */
    fun observeRecipeIdsWithAnyTag(tagIds: Set<Long>): Flow<Set<Long>>
}
