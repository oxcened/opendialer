package dev.alenajam.opendialer.feature.voicemail

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Voicemail
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.alenajam.opendialer.core.common.PermissionUtils
import dev.alenajam.opendialer.core.common.telecom.CallAccount
import dev.alenajam.opendialer.core.common.telecom.CallPlacementResult
import dev.alenajam.opendialer.core.common.ui.CallAccountPicker
import dev.alenajam.opendialer.data.voicemail.Voicemail
import java.text.DateFormat
import java.util.Date

@Composable
fun VoicemailScreen(
    viewModel: VoicemailViewModel = hiltViewModel(),
) {
    val requestPermissions = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (PermissionUtils.makeCallPermissions.all { result[it] == true }) {
            viewModel.handleRuntimePermissionGranted()
        }
    }
    val hasPermission by viewModel.hasRuntimePermission.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playingId by viewModel.playingId.collectAsStateWithLifecycle()
    var callAccounts by remember { mutableStateOf<List<CallAccount>?>(null) }

    fun callVoicemail() {
        when (val result = viewModel.callVoicemail()) {
            is CallPlacementResult.AccountSelectionRequired -> callAccounts = result.accounts
            else -> Unit
        }
    }

    callAccounts?.let { accounts ->
        CallAccountPicker(
            accounts = accounts,
            onAccountSelected = { account ->
                callAccounts = null
                when (val result = viewModel.callVoicemail(account)) {
                    is CallPlacementResult.AccountSelectionRequired -> callAccounts = result.accounts
                    else -> Unit
                }
            },
            onDismiss = { callAccounts = null },
        )
    }

    if (!hasPermission) {
        PermissionPlaceholder(
            onTurnOn = { requestPermissions.launch(PermissionUtils.makeCallPermissions) },
        )
        return
    }

    when (val state = uiState) {
        VoicemailUiState.Loading -> Unit
        VoicemailUiState.Unavailable -> VoicemailState(
            title = stringResource(R.string.visual_voicemail_unavailable),
            description = stringResource(R.string.visual_voicemail_unavailable_description),
            onCallVoicemail = ::callVoicemail,
            onRetry = viewModel::refresh,
        )
        is VoicemailUiState.Available -> {
            if (state.voicemails.isEmpty()) {
                VoicemailState(
                    title = stringResource(R.string.visual_voicemail_no_messages),
                    description = stringResource(R.string.visual_voicemail_no_messages_description),
                    onCallVoicemail = ::callVoicemail,
                    onRetry = viewModel::refresh,
                )
            } else {
                VoicemailList(
                    voicemails = state.voicemails,
                    playingId = playingId,
                    onPlay = viewModel::play,
                )
            }
        }
    }
}

@Composable
private fun PermissionPlaceholder(onTurnOn: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(R.string.placeholder_voicemail),
            textAlign = TextAlign.Center,
        )
        Button(onClick = onTurnOn) {
            Text(stringResource(R.string.turn_on))
        }
    }
}

@Composable
private fun VoicemailList(
    voicemails: List<Voicemail>,
    playingId: Long?,
    onPlay: (Voicemail) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(voicemails, key = Voicemail::id) { voicemail ->
            VoicemailRow(
                voicemail = voicemail,
                isPlaying = playingId == voicemail.id,
                onPlay = { onPlay(voicemail) },
            )
        }
    }
}

@Composable
private fun VoicemailRow(
    voicemail: Voicemail,
    isPlaying: Boolean,
    onPlay: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
            ) {
                Text(
                    text = voicemail.number ?: stringResource(R.string.unknown_caller),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                        .format(Date(voicemail.date)),
                )
                voicemail.transcription?.takeIf(String::isNotBlank)?.let { transcription ->
                    Text(
                        text = transcription,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            IconButton(onClick = onPlay) {
                Icon(
                    imageVector = Icons.Outlined.PlayArrow,
                    contentDescription = stringResource(
                        if (isPlaying) R.string.playing else R.string.play_voicemail
                    ),
                )
            }
        }
    }
}

@Composable
private fun VoicemailState(
    title: String,
    description: String,
    onCallVoicemail: () -> Unit,
    onRetry: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Voicemail,
            contentDescription = null,
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Text(description, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Button(onClick = onCallVoicemail) {
                Text(stringResource(R.string.call_voicemail))
            }
            OutlinedButton(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}
