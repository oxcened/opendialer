package dev.alenajam.opendialer.feature.inCall.ui

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.alenajam.opendialer.core.common.ui.AppIcon
import dev.alenajam.opendialer.core.common.ui.ContactAvatar
import dev.alenajam.opendialer.core.common.ui.contactAvatarColorKey
import dev.alenajam.opendialer.core.common.ui.LocalAppIcons
import dev.alenajam.opendialer.feature.inCall.service.OngoingCall
import dev.alenajam.opendialer.feature.inCall.R
import androidx.compose.ui.res.stringResource

@Composable
fun ManageConferenceSheet(
    participants: List<ConferenceParticipantUiState>,
    showSplit: Boolean = true,
    onSplit: (OngoingCall) -> Unit,
    onHangup: (OngoingCall) -> Unit,
    modifier: Modifier = Modifier
) {
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
                text = stringResource(R.string.conference_manage),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(participants) { participant ->
                    ConferenceParticipantRow(
                        participant = participant,
                        showSplit = showSplit,
                        onSplit = { onSplit(participant.call) },
                        onHangup = { onHangup(participant.call) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun ConferenceParticipantRow(
    participant: ConferenceParticipantUiState,
    showSplit: Boolean,
    onSplit: () -> Unit,
    onHangup: () -> Unit
) {
    val icons = LocalAppIcons.current

    Surface(
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            ContactAvatar(
                name = participant.callerName,
                photoUri = participant.callerImageUri,
                colorKey = contactAvatarColorKey(participant.callerName, participant.call.callerNumber),
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = participant.callerName,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val subtitle = getCallStatusLabel(participant.status)
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (showSplit && participant.isConferenced) {
                Surface(
                    onClick = onSplit,
                    color = Color.Transparent,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        AppIcon(
                            icon = icons.merge,
                            contentDescription = stringResource(R.string.action_split),
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Surface(
                onClick = onHangup,
                color = Color.Transparent,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    AppIcon(
                        icon = icons.hangup,
                        contentDescription = stringResource(R.string.action_end_call),
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
