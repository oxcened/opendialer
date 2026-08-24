package dev.alenajam.opendialer.feature.inCall.ui

import android.telecom.Call
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.alenajam.opendialer.core.common.ui.LocalAppIcons
import dev.alenajam.opendialer.feature.inCall.service.OngoingCall

@Composable
fun ManageConferenceSheet(
    calls: Map<Call, OngoingCall>,
    showSplit: Boolean = true,
    onSplit: (OngoingCall) -> Unit,
    onHangup: (OngoingCall) -> Unit,
    modifier: Modifier = Modifier
) {
    val participants = calls.values.filter { it.isConferenced() }

    Surface(
        tonalElevation = 8.dp,
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Manage",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(participants) { call ->
                    ConferenceParticipantRow(
                        call = call,
                        showSplit = showSplit,
                        onSplit = { onSplit(call) },
                        onHangup = { onHangup(call) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun ConferenceParticipantRow(
    call: OngoingCall,
    showSplit: Boolean,
    onSplit: () -> Unit,
    onHangup: () -> Unit
) {
    val icons = LocalAppIcons.current
    val isConferenced = call.isConferenced()
    val name = call.callerName ?: call.callerNumber ?: ""
    val state = call.state

    Surface(
        onClick = { if (showSplit && isConferenced) onSplit() },
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            AsyncImage(
                model = call.callerImageUri,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name.ifBlank { "Unknown" },
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val subtitle = when (state) {
                    Call.STATE_ACTIVE -> "Active"
                    Call.STATE_HOLDING -> "On hold"
                    Call.STATE_RINGING -> "Ringing"
                    Call.STATE_DIALING -> "Dialing"
                    Call.STATE_CONNECTING -> "Connecting"
                    else -> null
                }
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (showSplit && isConferenced) {
                Surface(
                    onClick = onSplit,
                    color = Color.Transparent,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icons.merge,
                            contentDescription = "Split",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Surface(
                onClick = onHangup,
                color = Color.Transparent,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icons.hangup,
                        contentDescription = "End call",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
