package dev.alenajam.opendialer.feature.inCall.ui

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import dev.alenajam.opendialer.core.common.getActivity
import dev.alenajam.opendialer.core.common.ui.Dialpad

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
    val context = LocalContext.current

    Scaffold(
        bottomBar = {
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

enum class OpenSection {
    ADDITIONAL_ACTIONS,
    DIALPAD
}

@Composable
private fun Footer(
    isMuted: Boolean? = false,
    isSpeaker: Boolean? = false,
    isHolding: Boolean? = false,
    onHangup: () -> Unit,
    onMute: () -> Unit,
    onSpeaker: () -> Unit,
    onHold: () -> Unit,
    onAddCall: () -> Unit,
    onDigit: (digit: Char) -> Unit
) {
    var openSection = remember { mutableStateOf<OpenSection?>(null) }
    var dialpadInput = remember { mutableStateOf("") }

    fun handleDialpadDigit(digit: Char) {
        dialpadInput.value = dialpadInput.value.plus(digit)
        onDigit(digit)
    }

    fun toggleSectionButton(section: OpenSection) {
        openSection.value = if (openSection.value == section) null else section
    }

    Surface(
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
        ) {
            AnimatedVisibility(visible = openSection.value == OpenSection.ADDITIONAL_ACTIONS) {
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp, top = 32.dp)
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

            AnimatedVisibility(
                visible = openSection.value == OpenSection.DIALPAD
            ) {
                Column(
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    TextField(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 8.dp)
                            .fillMaxWidth(),
                        value = TextFieldValue(text = dialpadInput.value),
                        onValueChange = { dialpadInput.value = it.text },
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent
                        )
                    )

                    Dialpad(
                        onDigitClick = ::handleDialpadDigit
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp, top = 32.dp)
            ) {
                CallButton(
                    icon = Icons.Outlined.Dialpad,
                    label = "Dialpad",
                    isActive = openSection.value == OpenSection.DIALPAD,
                    onClick = { toggleSectionButton(OpenSection.DIALPAD) }
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
                    isActive = openSection.value == OpenSection.ADDITIONAL_ACTIONS,
                    onClick = { toggleSectionButton(OpenSection.ADDITIONAL_ACTIONS) }
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
            Surface(
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