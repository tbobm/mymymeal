package dev.tbobm.mymymeal.app.common.infrastructure.room.tag

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import dev.tbobm.mymymeal.app.food.infrastructure.room.RecipeEntity

/** PRD 3.5. Many-to-many cross-reference between catalog `Recipe` rows and `Tag` rows. */
@Entity(
    tableName = "RecipeTagCrossRef",
    primaryKeys = ["recipeId", "tagId"],
    foreignKeys =
        [
            ForeignKey(
                entity = RecipeEntity::class,
                parentColumns = ["id"],
                childColumns = ["recipeId"],
                onDelete = ForeignKey.CASCADE,
            ),
            ForeignKey(
                entity = TagEntity::class,
                parentColumns = ["id"],
                childColumns = ["tagId"],
                onDelete = ForeignKey.CASCADE,
            ),
        ],
    indices = [Index(value = ["recipeId"]), Index(value = ["tagId"])],
)
data class RecipeTagCrossRefEntity(val recipeId: Long, val tagId: Long)
