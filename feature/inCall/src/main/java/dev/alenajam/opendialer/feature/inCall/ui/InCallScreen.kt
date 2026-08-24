package dev.alenajam.opendialer.feature.inCall.ui

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.alenajam.opendialer.core.common.getActivity
import dev.alenajam.opendialer.core.common.ui.AppProviders

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InCallScreen(
    viewModel: InCallViewModel = viewModel()
) {
    val stateLabel = viewModel.stateLabel.observeAsState("")
    val isHolding = viewModel.isHolding.observeAsState()
    val isSpeaker = viewModel.isSpeaker.observeAsState()
    val isMuted = viewModel.isMuted.observeAsState()
    val callerName = viewModel.callerName.observeAsState("")
    val callerNumber = viewModel.callerNumber.observeAsState("")
    val callerNumberLabel = viewModel.callerNumberLabel.observeAsState("")
    val callerImageUri = viewModel.callerImageUri.observeAsState("")
    val isIncoming = viewModel.isIncoming.observeAsState(false)
    val calls = viewModel.calls.observeAsState(emptyMap())
    val context = LocalContext.current

    val canMerge = viewModel.canMerge.observeAsState(false).value
    val hasSecondaryCall = viewModel.hasSecondaryCall.observeAsState(false).value
    val secondaryCallerName = viewModel.secondaryCallerName.observeAsState(null).value
    val canSwap = hasSecondaryCall
    // Add call uses the telecom-provided capability. It is hidden once a
    // secondary call is present (two independent calls or a conference plus a
    // held call), since the map size check is unreliable for conferences where
    // the parent call inflates the count.
    val canAddCall = viewModel.canAddCall.observeAsState(false).value && !hasSecondaryCall
    // When two calls are present, Swap replaces Hold in the More panel (they
    // would otherwise both toggle the same primary/secondary state).
    val canHold = !canSwap
    // In a conference with a secondary call, splitting a participant would
    // produce three independent calls, so the split affordance is hidden.
    val canManageConference =
            calls.value.any { it.value.isConference() || it.value.isConferenced() }
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
                calls = calls.value,
                showSplit = showSplitInManage,
                onSplit = { call -> viewModel.split(call) },
                onHangup = { call -> viewModel.hangup(call) }
            )
        }
    }

    AppProviders {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (isIncoming.value) {
                    IncomingCallControls(
                        onHangup = viewModel::hangup,
                        onAnswer = viewModel::answer,
                    )
                } else {
                    InCallControls(
                        isMuted = isMuted.value,
                        isSpeaker = isSpeaker.value,
                        isHolding = isHolding.value,
                        canManageConference = canManageConference,
                        canMerge = canMerge,
                        canSwap = canSwap,
                        canHold = canHold,
                        canAddCall = canAddCall,
                        onHangup = viewModel::hangup,
                        onMute = viewModel::turnMute,
                        onSpeaker = viewModel::turnSpeaker,
                        onHold = viewModel::hold,
                        onAddCall = { viewModel.addCall(activity = context.getActivity() as Activity) },
                        onMerge = { viewModel.merge() },
                        onSwap = { viewModel.swap() },
                        onManageConference = { showManageSheet = true },
                        onDigit = viewModel::playDtmf
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (hasSecondaryCall && secondaryCallerName != null && !isIncoming.value) {
                    SecondaryCallBanner(
                        callerName = secondaryCallerName,
                        modifier = Modifier.statusBarsPadding()
                    )
                }
                InCallDetails(
                    callerName = callerName.value,
                    callerNumber = callerNumber.value,
                    callerNumberLabel = callerNumberLabel.value,
                    stateLabel = stateLabel.value,
                    callerImageUri = callerImageUri.value,
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                )
            }
        }
    }
}

enum class OpenSection {
    ADDITIONAL_ACTIONS,
    DIALPAD
}
