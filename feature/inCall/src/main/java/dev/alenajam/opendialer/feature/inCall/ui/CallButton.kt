package dev.alenajam.opendialer.feature.inCall.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.alenajam.opendialer.feature.inCall.R

@Composable
internal fun CallButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean? = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (isActive == true && enabled) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceContainerLowest
        },
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 500f),
        label = "Call control container"
    )
    val contentColor by animateColorAsState(
        targetValue = if (!enabled) {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        } else if (isActive == true) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 500f),
        label = "Call control content"
    )
    val cornerRadius by animateDpAsState(
        targetValue = if (isActive == true) 18.dp else 24.dp,
        animationSpec = spring(dampingRatio = 0.68f, stiffness = 420f),
        label = "Call control shape"
    )
    val stateDescription = stringResource(
        if (isActive == true) R.string.control_state_on else R.string.control_state_off
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Surface(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .width(72.dp)
                .height(64.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = label
                    this.stateDescription = stateDescription
                    role = Role.Button
                },
            shape = RoundedCornerShape(cornerRadius),
            color = containerColor
        ) {
            Box {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Text(
            text = label,
            modifier = Modifier.width(72.dp),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            },
        )
    }
}
