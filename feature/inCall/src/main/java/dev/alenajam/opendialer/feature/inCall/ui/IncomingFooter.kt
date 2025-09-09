package dev.alenajam.opendialer.feature.inCall.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.CallEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.alenajam.opendialer.core.common.LocalCustomColorsScheme

@Composable
internal fun IncomingFooter(
    onHangup: () -> Unit,
    onAnswer: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 60.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            IconButton(
                onClick = onHangup,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier
                    .size(60.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CallEnd,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            Text(text = "Decline")
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            IconButton(
                onClick = onAnswer,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = LocalCustomColorsScheme.current.success
                ),
                modifier = Modifier
                    .size(60.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Call,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            Text(text = "Answer")
        }
    }
}