package dev.tbobm.mymymeal.app.common.infrastructure.room.tag

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * PRD 3.5 (categorisation). A user-defined tag, freely assignable to foods (Product/Recipe) and
 * manual diary entries via the cross-reference tables in this package. Not attached directly to
 * `Measurement` -- a food-backed diary entry already references a `Product`/`Recipe` via
 * `originProductId`/`originRecipeId`, so tagging the food covers it transitively.
 */
@Entity(tableName = "Tag", indices = [Index(value = ["name"], unique = true)])
data class TagEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String)
