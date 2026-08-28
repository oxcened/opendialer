package dev.alenajam.opendialer.feature.inCall.ui

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.alenajam.opendialer.core.common.getActivity
import dev.alenajam.opendialer.core.common.ui.AppProviders

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InCallScreen(
    viewModel: InCallViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val durationMillis by viewModel.activeCallDuration.collectAsStateWithLifecycle(0L)
    val context = LocalContext.current

    val canMerge = uiState.canMerge
    val telecomCanHold = uiState.canHold
    val hasSecondaryCall = uiState.hasSecondaryCall
    val secondaryCallerName = uiState.secondaryCallerName
    val canSwap = hasSecondaryCall
    // Add call uses the telecom-provided capability. It is hidden once a
    // secondary call is present (two independent calls or a conference plus a
    // held call), since the map size check is unreliable for conferences where
    // the parent call inflates the count.
    val canAddCall = uiState.canAddCall && !hasSecondaryCall
    // When two calls are present, Swap replaces Hold in the More panel (they
    // would otherwise both toggle the same primary/secondary state).
    val canHold = telecomCanHold && !canSwap
    // In a conference with a secondary call, splitting a participant would
    // produce three independent calls, so the split affordance is hidden.
    val canManageConference = uiState.canManageConference
    val showSplitInManage = canManageConference && !hasSecondaryCall

    var showManageSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Dismiss the sheet when there is no longer a conference to manage, so a
    // stale `showManageSheet = true` can't re-open it when a new conference forms.
    LaunchedEffect(canManageConference) {
        if (!canManageConference) showManageSheet = false
    }

    if (showManageSheet && canManageConference) {
        ModalBottomSheet(
            onDismissRequest = { showManageSheet = false },
            sheetState = sheetState
        ) {
            ManageConferenceSheet(
                participants = uiState.conferenceParticipants,
                showSplit = showSplitInManage,
                onSplit = { call -> viewModel.split(call) },
                onHangup = { call -> viewModel.hangup(call) }
            )
        }
    }

    AppProviders {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (hasSecondaryCall && secondaryCallerName != null && !uiState.isIncoming) {
                    SecondaryCallBanner(
                        callerName = secondaryCallerName,
                        modifier = Modifier.statusBarsPadding()
                    )
                }
                InCallDetails(
                    callerName = uiState.callerName,
                    callerNumber = uiState.callerNumber,
                    callerNumberLabel = uiState.callerNumberLabel,
                    status = uiState.status,
                    durationMillis = durationMillis,
                    callerImageUri = uiState.callerImageUri,
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 32.dp)
                )
            }

            if (uiState.isIncoming) {
                IncomingCallControls(
                    onHangup = viewModel::hangup,
                    onAnswer = viewModel::answer,
                    onMessage = { viewModel.hangup(it) },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            } else {
                InCallControls(
                    isMuted = uiState.isMuted,
                    isSpeaker = uiState.isSpeaker,
                    audioRoutes = uiState.audioRoutes,
                    isHolding = uiState.isHolding,
                    canManageConference = canManageConference,
                    canMerge = canMerge,
                    canSwap = canSwap,
                    canHold = canHold,
                    showAddCall = !hasSecondaryCall,
                    canAddCall = canAddCall,
                    controlsEnabled = uiState.status != CallStatus.CONNECTING &&
                        uiState.status != CallStatus.DISCONNECTING &&
                        uiState.status != CallStatus.DISCONNECTED,
                    canHangup = uiState.status != CallStatus.DISCONNECTING &&
                        uiState.status != CallStatus.DISCONNECTED,
                    onHangup = viewModel::hangup,
                    onMute = viewModel::turnMute,
                    onSpeaker = viewModel::turnSpeaker,
                    onAudioRouteSelected = viewModel::selectAudioRoute,
                    onHold = viewModel::hold,
                    onAddCall = { viewModel.addCall(activity = context.getActivity() as Activity) },
                    onMerge = { viewModel.merge() },
                    onSwap = { viewModel.swap() },
                    onManageConference = { showManageSheet = true },
                    onDigitPress = viewModel::startDtmf,
                    onDigitRelease = viewModel::stopDtmf,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}
}

enum class OpenSection {
    ADDITIONAL_ACTIONS,
    DIALPAD
}
