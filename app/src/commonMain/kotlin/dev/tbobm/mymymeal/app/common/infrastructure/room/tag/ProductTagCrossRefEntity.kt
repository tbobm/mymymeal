package dev.tbobm.mymymeal.app.common.infrastructure.room.tag

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import dev.tbobm.mymymeal.app.food.infrastructure.room.ProductEntity

/** PRD 3.5. Many-to-many cross-reference between catalog `Product` rows and `Tag` rows. */
@Entity(
    tableName = "ProductTagCrossRef",
    primaryKeys = ["productId", "tagId"],
    foreignKeys =
        [
            ForeignKey(
                entity = ProductEntity::class,
                parentColumns = ["id"],
                childColumns = ["productId"],
                onDelete = ForeignKey.CASCADE,
            ),
            ForeignKey(
                entity = TagEntity::class,
                parentColumns = ["id"],
                childColumns = ["tagId"],
                onDelete = ForeignKey.CASCADE,
            ),
        ],
    indices = [Index(value = ["productId"]), Index(value = ["tagId"])],
)
data class ProductTagCrossRefEntity(val productId: Long, val tagId: Long)
