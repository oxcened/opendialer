package dev.alenajam.opendialer.feature.inCall.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.alenajam.opendialer.core.common.CommonUtils
import dev.alenajam.opendialer.core.common.ui.ContactAvatar
import dev.alenajam.opendialer.feature.inCall.R

@Composable
fun InCallDetails(
    callerName: String,
    callerNumber: String,
    callerNumberLabel: String,
    status: CallStatus,
    durationMillis: Long,
    callerImageUri: String? = null,
    modifier: Modifier = Modifier,
    showCallerImage: Boolean = true
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        val displayName = callerName.ifBlank { callerNumber.ifBlank { "Unknown" } }

        Text(
            text = if (status == CallStatus.ACTIVE) {
                CommonUtils.getDurationTimeString(durationMillis)
            } else {
                getCallStatusLabel(status)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = displayName,
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (callerName.isNotBlank() && callerNumber.isNotBlank() && callerName != callerNumber) {
            Text(
                text = if (callerNumberLabel.isBlank()) {
                    callerNumber
                } else {
                    stringResource(R.string.caller_number_subtitle, callerNumberLabel, callerNumber)
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (showCallerImage) {
            Spacer(modifier = Modifier.height(80.dp))

            ContactAvatar(
                name = callerName.takeIf { it.isNotBlank() && it != callerNumber },
                photoUri = callerImageUri,
                colorKey = callerName.ifBlank { callerNumber },
                fallbackIconModifier = Modifier.size(72.dp),
                initialTextStyle = MaterialTheme.typography.displayLarge,
                modifier = Modifier.size(176.dp)
            )
        }
    }
}

@Composable
fun getCallStatusLabel(status: CallStatus): String = when (status) {
    CallStatus.RINGING -> stringResource(R.string.call_ringing_title)
    CallStatus.CONNECTING -> stringResource(R.string.call_connecting_title)
    CallStatus.HOLDING -> stringResource(R.string.call_holding_title)
    CallStatus.DIALING -> stringResource(R.string.call_dialing_title)
    CallStatus.DISCONNECTING -> stringResource(R.string.call_disconnecting_title)
    CallStatus.DISCONNECTED -> stringResource(R.string.call_disconnected_title)
    else -> ""
}
