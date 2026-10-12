package dev.tbobm.mymymeal.app.app.ui.tag

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.tbobm.mymymeal.app.common.domain.tag.Tag
import dev.tbobm.mymymeal.app.common.domain.tag.TagRepository
import foodyou.app.generated.resources.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/**
 * PRD 3.5 (categorisation). Chip-picker for assigning existing tags to an already-persisted
 * product/recipe/manual diary entry. Writes through [TagRepository] immediately on toggle rather
 * than batching into the surrounding form's save action -- there is no unsaved-entity state here
 * since this is only ever placed on Update screens (never Create), so the entity id already
 * exists.
 *
 * If no tags exist yet in the app, this renders nothing rather than an empty "Tags" section --
 * users create tags from Settings first.
 */
@Composable
private fun TagAssignmentChips(
    allTags: List<Tag>,
    assignedTagIds: Set<Long>,
    onToggle: (tagId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (allTags.isEmpty()) {
        return
    }

    Column(modifier = modifier) {
        Text(
            text = stringResource(Res.string.headline_tags),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            allTags.forEach { tag ->
                val selected = tag.id in assignedTagIds

                key(tag.id) {
                    FilterChip(
                        selected = selected,
                        onClick = { onToggle(tag.id) },
                        label = { Text(tag.name) },
                        leadingIcon = {
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}

/**
 * Shared plumbing for the three per-entity tag pickers below: observe all tags + the entity's
 * current tags, and write the toggled set back immediately.
 */
@Composable
private fun EntityTagAssignmentChips(
    observeAssignedTags: (TagRepository) -> Flow<List<Tag>>,
    setAssignedTags: suspend TagRepository.(Set<Long>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tagRepository = koinInject<TagRepository>()
    val coroutineScope = rememberCoroutineScope()
    val allTags =
        remember(tagRepository) { tagRepository.observeTags() }
            .collectAsStateWithLifecycle(emptyList())
            .value
    val assignedTagIds =
        remember(tagRepository, observeAssignedTags) { observeAssignedTags(tagRepository) }
            .collectAsStateWithLifecycle(emptyList())
            .value
            .map { it.id }
            .toSet()

    TagAssignmentChips(
        allTags = allTags,
        assignedTagIds = assignedTagIds,
        onToggle = { tagId ->
            val newIds = if (tagId in assignedTagIds) assignedTagIds - tagId else assignedTagIds + tagId
            coroutineScope.launch { tagRepository.setAssignedTags(newIds) }
        },
        modifier = modifier,
    )
}

/** Product-tag picker for the product edit screen. */
@Composable
internal fun ProductTagAssignmentChips(productId: Long, modifier: Modifier = Modifier) {
    EntityTagAssignmentChips(
        observeAssignedTags = { it.observeTagsForProduct(productId) },
        setAssignedTags = { setProductTags(productId, it) },
        modifier = modifier,
    )
}

/** Recipe-tag picker for the recipe edit screen. */
@Composable
internal fun RecipeTagAssignmentChips(recipeId: Long, modifier: Modifier = Modifier) {
    EntityTagAssignmentChips(
        observeAssignedTags = { it.observeTagsForRecipe(recipeId) },
        setAssignedTags = { setRecipeTags(recipeId, it) },
        modifier = modifier,
    )
}

/** Tag picker for the manual diary entry edit screen. */
@Composable
internal fun ManualDiaryEntryTagAssignmentChips(manualDiaryEntryId: Long, modifier: Modifier = Modifier) {
    EntityTagAssignmentChips(
        observeAssignedTags = { it.observeTagsForManualDiaryEntry(manualDiaryEntryId) },
        setAssignedTags = { setManualDiaryEntryTags(manualDiaryEntryId, it) },
        modifier = modifier,
    )
}
