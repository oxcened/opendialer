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
import androidx.compose.ui.unit.dp

@Composable
internal fun CallButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean? = false,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (isActive == true) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceContainerLowest
        },
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 500f),
        label = "Call control container"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isActive == true) {
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

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier
                .width(76.dp)
                .height(64.dp),
            shape = RoundedCornerShape(cornerRadius),
            color = containerColor
        ) {
            Box {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = contentColor,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Text(
            text = label
        )
    }
}
