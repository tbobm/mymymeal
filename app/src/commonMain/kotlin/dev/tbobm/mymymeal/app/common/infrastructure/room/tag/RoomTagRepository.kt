package dev.tbobm.mymymeal.app.common.infrastructure.room.tag

import dev.tbobm.mymymeal.app.common.domain.tag.Tag
import dev.tbobm.mymymeal.app.common.domain.tag.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal class RoomTagRepository(private val tagDao: TagDao) : TagRepository {
    override fun observeTags(): Flow<List<Tag>> = tagDao.observeTags().map { it.map(TagEntity::toModel) }

    override suspend fun createTag(name: String): Long? = tagDao.insertTag(TagEntity(name = name))

    override suspend fun renameTag(id: Long, name: String) {
        tagDao.updateTag(TagEntity(id = id, name = name))
    }

    override suspend fun deleteTag(id: Long) = tagDao.deleteTag(id)

    override fun observeTagsForProduct(productId: Long): Flow<List<Tag>> =
        tagDao.observeTagsForProduct(productId).map { it.map(TagEntity::toModel) }

    override fun observeTagsForRecipe(recipeId: Long): Flow<List<Tag>> =
        tagDao.observeTagsForRecipe(recipeId).map { it.map(TagEntity::toModel) }

    override fun observeTagsForManualDiaryEntry(manualDiaryEntryId: Long): Flow<List<Tag>> =
        tagDao.observeTagsForManualDiaryEntry(manualDiaryEntryId).map { it.map(TagEntity::toModel) }

    override suspend fun setProductTags(productId: Long, tagIds: Set<Long>) =
        tagDao.setProductTags(productId, tagIds)

    override suspend fun setRecipeTags(recipeId: Long, tagIds: Set<Long>) =
        tagDao.setRecipeTags(recipeId, tagIds)

    override suspend fun setManualDiaryEntryTags(manualDiaryEntryId: Long, tagIds: Set<Long>) =
        tagDao.setManualDiaryEntryTags(manualDiaryEntryId, tagIds)

    override fun observeProductIdsWithAnyTag(tagIds: Set<Long>): Flow<Set<Long>> =
        if (tagIds.isEmpty()) {
            flowOf(emptySet())
        } else {
            tagDao.observeProductIdsWithAnyTag(tagIds).map { it.toSet() }
        }

    override fun observeRecipeIdsWithAnyTag(tagIds: Set<Long>): Flow<Set<Long>> =
        if (tagIds.isEmpty()) {
            flowOf(emptySet())
        } else {
            tagDao.observeRecipeIdsWithAnyTag(tagIds).map { it.toSet() }
        }
}

private fun TagEntity.toModel() = Tag(id = id, name = name)
