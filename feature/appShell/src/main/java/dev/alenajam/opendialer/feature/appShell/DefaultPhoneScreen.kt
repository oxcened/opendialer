package dev.alenajam.opendialer.feature.appShell

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SetupScreen(
    isDefaultPhoneApp: Boolean,
    hasFullScreenIntentAccess: Boolean,
    onSetAsDefault: () -> Unit,
    onEnableFullScreenIntent: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.setup_title)) })
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.setup_description),
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Normal,
                ),
            )
            Spacer(Modifier.height(16.dp))
            SetupStep(
                stepNumber = 1,
                state = if (isDefaultPhoneApp) SetupStepState.COMPLETE else SetupStepState.CURRENT,
                title = stringResource(R.string.setup_default_phone_title),
                description = stringResource(R.string.setup_default_phone_description),
                actionLabel = stringResource(R.string.setup_default_phone_action),
                onAction = onSetAsDefault,
            )
            Spacer(Modifier.height(12.dp))
            SetupStep(
                stepNumber = 2,
                state = when {
                    hasFullScreenIntentAccess -> SetupStepState.COMPLETE
                    isDefaultPhoneApp -> SetupStepState.CURRENT
                    else -> SetupStepState.NEXT
                },
                title = stringResource(R.string.setup_full_screen_title),
                description = stringResource(R.string.setup_full_screen_description),
                actionLabel = stringResource(R.string.setup_full_screen_action),
                onAction = onEnableFullScreenIntent,
            )
        }
    }
}

@Composable
private fun SetupStep(
    stepNumber: Int,
    state: SetupStepState,
    title: String,
    description: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (state) {
                SetupStepState.COMPLETE -> MaterialTheme.colorScheme.surfaceContainerLow
                SetupStepState.CURRENT -> MaterialTheme.colorScheme.surfaceContainerHigh
                SetupStepState.NEXT -> MaterialTheme.colorScheme.surfaceContainerLow
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StepMarker(stepNumber = stepNumber, state = state)
                Spacer(Modifier.size(16.dp))
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (state == SetupStepState.CURRENT) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Text(text = actionLabel)
                }
            }
        }
    }
}

@Composable
private fun StepMarker(stepNumber: Int, state: SetupStepState) {
    val isComplete = state == SetupStepState.COMPLETE
    Surface(
        modifier = Modifier.size(40.dp),
        shape = RoundedCornerShape(20.dp),
        color = when (state) {
            SetupStepState.COMPLETE -> MaterialTheme.colorScheme.tertiary
            SetupStepState.CURRENT -> MaterialTheme.colorScheme.primary
            SetupStepState.NEXT -> MaterialTheme.colorScheme.surfaceVariant
        },
    ) {
        if (isComplete) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = stringResource(R.string.setup_step_complete),
                modifier = Modifier.padding(9.dp),
                tint = MaterialTheme.colorScheme.onTertiary,
            )
        } else {
            Text(
                text = stepNumber.toString(),
                modifier = Modifier.padding(top = 8.dp),
                color = if (state == SetupStepState.CURRENT) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private enum class SetupStepState { COMPLETE, CURRENT, NEXT }
