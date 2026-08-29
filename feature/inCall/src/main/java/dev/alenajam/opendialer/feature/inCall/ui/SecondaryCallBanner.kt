package dev.alenajam.opendialer.feature.inCall.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.alenajam.opendialer.core.common.ui.AppIcon
import dev.alenajam.opendialer.core.common.ui.LocalAppIcons
import dev.alenajam.opendialer.feature.inCall.R

@Composable
fun SecondaryCallBanner(
    callerName: String,
    modifier: Modifier = Modifier
) {
    val icons = LocalAppIcons.current
    Surface(
        color = Color(0xFF424242).copy(alpha = 0.75f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            AppIcon(
                icon = icons.phonePaused,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "$callerName – ${stringResource(R.string.call_holding_title)}",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
