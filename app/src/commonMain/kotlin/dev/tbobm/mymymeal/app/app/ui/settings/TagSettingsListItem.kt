package dev.tbobm.mymymeal.app.app.ui.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import dev.tbobm.mymymeal.app.app.ui.common.component.SettingsListItem
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/** PRD 3.5 (categorisation). */
@Composable
internal fun TagSettingsListItem(
    onClick: () -> Unit,
    shape: Shape,
    color: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    SettingsListItem(
        icon = { Icon(Icons.Outlined.Sell, null) },
        label = { Text(stringResource(Res.string.headline_tags)) },
        supportingContent = { Text(stringResource(Res.string.neutral_manage_tags)) },
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
    )
}
