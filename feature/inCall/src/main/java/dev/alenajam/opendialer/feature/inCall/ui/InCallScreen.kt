package dev.alenajam.opendialer.feature.inCall.ui

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.AddIcCall
import androidx.compose.material.icons.outlined.CallEnd
import androidx.compose.material.icons.outlined.Dialpad
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import dev.alenajam.opendialer.core.common.getActivity

@Composable
internal fun InCallScreen(
    viewModel: InCallViewModel = viewModel(),
    navController: NavController,
) {
    val stateLabel = viewModel.stateLabel.observeAsState("")
    val isHolding = viewModel.isHolding.observeAsState()
    val isSpeaker = viewModel.isSpeaker.observeAsState()
    val isMuted = viewModel.isMuted.observeAsState()
    val callerName = viewModel.callerName.observeAsState("")
    val callerImageUri = viewModel.callerImageUri.observeAsState("")
    val context = LocalContext.current

    Box {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
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

        CallButtons(
            isMuted = isMuted.value,
            isSpeaker = isSpeaker.value,
            isHolding = isHolding.value,
            onHangup = viewModel::hangup,
            onMute = viewModel::turnMute,
            onSpeaker = viewModel::turnSpeaker,
            onHold = viewModel::hold,
            onAddCall = { viewModel.addCall(activity = context.getActivity() as Activity) }
        )
    }
}

@Composable
private fun BoxScope.CallButtons(
    isMuted: Boolean? = false,
    isSpeaker: Boolean? = false,
    isHolding: Boolean? = false,
    onHangup: () -> Unit,
    onMute: () -> Unit,
    onSpeaker: () -> Unit,
    onHold: () -> Unit,
    onAddCall: () -> Unit,
) {
    var isOpen = remember { mutableStateOf(false) }

    Surface(
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp)
        ) {
            AnimatedVisibility(visible = isOpen.value) {
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 48.dp)
                ) {
                    CallButton(
                        icon = Icons.Outlined.Pause,
                        label = "Hold",
                        isActive = isHolding,
                        onClick = onHold
                    )

                    CallButton(
                        icon = Icons.Outlined.AddIcCall,
                        label = "Add call",
                        isActive = false,
                        onClick = onAddCall
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth().padding(bottom = 48.dp)
            ) {
                CallButton(
                    icon = Icons.Outlined.Dialpad,
                    label = "Dialpad",
                    isActive = false,
                    onClick = {}
                )

                CallButton(
                    icon = Icons.Outlined.MicOff,
                    label = "Mute",
                    isActive = isMuted,
                    onClick = onMute
                )

                CallButton(
                    icon = Icons.AutoMirrored.Outlined.VolumeUp,
                    label = "Speaker",
                    isActive = isSpeaker,
                    onClick = onSpeaker
                )

                CallButton(
                    icon = Icons.Outlined.MoreVert,
                    label = "More",
                    isActive = isOpen.value,
                    onClick = { isOpen.value = !isOpen.value }
                )
            }

            IconButton(
                onClick = onHangup,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Red
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(60.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CallEnd,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun CallButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean? = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface (
                modifier = Modifier.size(50.dp),
                shape = CircleShape,
                color = if (isActive == true) Color.DarkGray else Color.White
            ) {
                Box {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isActive == true) Color.White else Color.DarkGray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Text(
                text = label
            )
        }
    }
}