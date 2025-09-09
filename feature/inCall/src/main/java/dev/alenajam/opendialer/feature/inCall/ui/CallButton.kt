package dev.alenajam.opendialer.feature.inCall.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
internal fun CallButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean? = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(50.dp),
                shape = CircleShape,
                color = if (isActive == true) Color.DarkGray else Color.White
            ) {
                Box {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isActive == true) Color.White else Color.DarkGray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Text(
                text = label
            )
        }
    }
}