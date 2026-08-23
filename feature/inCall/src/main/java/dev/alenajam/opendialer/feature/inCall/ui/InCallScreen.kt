package dev.alenajam.opendialer.feature.inCall.ui

import android.app.Activity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.alenajam.opendialer.core.common.getActivity
import dev.alenajam.opendialer.core.common.ui.AppProviders

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
    val callerImageUri = viewModel.callerImageUri.observeAsState("")
    val isIncoming = viewModel.isIncoming.observeAsState(false)
    val context = LocalContext.current

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
                        onHangup = viewModel::hangup,
                        onMute = viewModel::turnMute,
                        onSpeaker = viewModel::turnSpeaker,
                        onHold = viewModel::hold,
                        onAddCall = { viewModel.addCall(activity = context.getActivity() as Activity) },
                        onDigit = viewModel::playDtmf
                    )
                }
            }
        ) { innerPadding ->
            InCallDetails(
                callerName = callerName.value,
                callerNumber = callerNumber.value,
                stateLabel = stateLabel.value,
                callerImageUri = callerImageUri.value,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .statusBarsPadding()
            )
        }
    }
}

enum class OpenSection {
    ADDITIONAL_ACTIONS,
    DIALPAD
}
