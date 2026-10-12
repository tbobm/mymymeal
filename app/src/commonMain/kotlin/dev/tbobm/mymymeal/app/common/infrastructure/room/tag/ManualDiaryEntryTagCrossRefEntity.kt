package dev.tbobm.mymymeal.app.common.infrastructure.room.tag

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import dev.tbobm.mymymeal.app.fooddiary.infrastructure.room.ManualDiaryEntryEntity

/**
 * PRD 3.5. Many-to-many cross-reference between `ManualDiaryEntry` rows and `Tag` rows.
 * `ManualDiaryEntry` has no product/recipe reference to inherit tags through, so it needs its own
 * direct cross-ref (unlike `Measurement`, which is covered transitively via the food it snapshots).
 */
@Entity(
    tableName = "ManualDiaryEntryTagCrossRef",
    primaryKeys = ["manualDiaryEntryId", "tagId"],
    foreignKeys =
        [
            ForeignKey(
                entity = ManualDiaryEntryEntity::class,
                parentColumns = ["id"],
                childColumns = ["manualDiaryEntryId"],
                onDelete = ForeignKey.CASCADE,
            ),
            ForeignKey(
                entity = TagEntity::class,
                parentColumns = ["id"],
                childColumns = ["tagId"],
                onDelete = ForeignKey.CASCADE,
            ),
        ],
    indices = [Index(value = ["manualDiaryEntryId"]), Index(value = ["tagId"])],
)
data class ManualDiaryEntryTagCrossRefEntity(val manualDiaryEntryId: Long, val tagId: Long)
