package dev.alenajam.opendialer.feature.inCall.ui

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
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
    val callerImageUri = viewModel.callerImageUri.observeAsState("")
    val isIncoming = viewModel.isIncoming.observeAsState(false)
    val context = LocalContext.current

    AppProviders {
        Scaffold(
            bottomBar = {
                if (isIncoming.value) {
                    IncomingFooter(
                        onHangup = viewModel::hangup,
                        onAnswer = viewModel::answer,
                    )
                } else {
                    Footer(
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AsyncImage(
                    model = callerImageUri.value,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    placeholder = null,
                    error = null,
                    fallback = null
                )

                Text(
                    text = callerName.value,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = stateLabel.value,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

enum class OpenSection {
    ADDITIONAL_ACTIONS,
    DIALPAD
}