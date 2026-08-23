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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun InCallDetails(
    callerName: String,
    callerNumber: String,
    stateLabel: String,
    callerImageUri: String? = null,
    modifier: Modifier = Modifier,
    showCallerImage: Boolean = true
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        if (showCallerImage) {
            AsyncImage(
                model = callerImageUri,
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                placeholder = null,
                error = null,
                fallback = null
            )
        }

        Text(
            text = stateLabel,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = callerName.ifBlank { callerNumber.ifBlank { "Unknown" } },
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (callerName.isNotBlank() && callerNumber.isNotBlank() && callerName != callerNumber) {
            Text(
                text = callerNumber,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
