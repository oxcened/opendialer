package dev.alenajam.opendialer.feature.inCall.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.alenajam.opendialer.core.common.ui.AppIcon
import dev.alenajam.opendialer.core.common.ui.Dialpad
import dev.alenajam.opendialer.core.common.ui.IconSource
import dev.alenajam.opendialer.core.common.ui.LocalAppIcons
import dev.alenajam.opendialer.feature.inCall.R
import dev.alenajam.opendialer.feature.inCall.service.CallAudioRouteUiState

@Composable
fun InCallControls(
    isMuted: Boolean? = false,
    isSpeaker: Boolean? = false,
    audioRoutes: List<CallAudioRouteUiState> = emptyList(),
    isHolding: Boolean? = false,
    canManageConference: Boolean = false,
    canMerge: Boolean = false,
    canSwap: Boolean = false,
    canHold: Boolean = true,
    showAddCall: Boolean = true,
    canAddCall: Boolean = true,
    controlsEnabled: Boolean = true,
    canHangup: Boolean = true,
    onHangup: () -> Unit,
    onMute: () -> Unit,
    onSpeaker: () -> Unit,
    onAudioRouteSelected: (CallAudioRouteUiState) -> Unit = {},
    onHold: () -> Unit,
    onAddCall: () -> Unit,
    onMerge: () -> Unit = {},
    onSwap: () -> Unit = {},
    onManageConference: () -> Unit = {},
    onDigitPress: (digit: Char) -> Unit,
    onDigitRelease: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icons = LocalAppIcons.current
    var openSection = remember { mutableStateOf<OpenSection?>(null) }
    var dialpadInput = remember { mutableStateOf("") }
    var audioRoutesExpanded = remember { mutableStateOf(false) }

    DisposableEffect(onDigitRelease) {
        onDispose(onDigitRelease)
    }
    val hasExternalAudioRoute = audioRoutes.any {
        it.type == android.telecom.CallAudioState.ROUTE_BLUETOOTH ||
            it.type == android.telecom.CallAudioState.ROUTE_WIRED_HEADSET
    }
    val currentAudioRoute = audioRoutes.firstOrNull { it.isSelected }
    val endCallDescription = stringResource(R.string.action_end_call)

    fun handleDialpadDigit(digit: Char) {
        dialpadInput.value = dialpadInput.value.plus(digit)
    }

    fun toggleSectionButton(section: OpenSection) {
        if (openSection.value == OpenSection.DIALPAD) onDigitRelease()
        openSection.value = if (openSection.value == section) null else section
    }

    Surface(
        tonalElevation = 8.dp,
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            AnimatedVisibility(
                visible = openSection.value == OpenSection.ADDITIONAL_ACTIONS,
                enter = expandVertically(
                    animationSpec = spring(dampingRatio = 0.78f, stiffness = 420f)
                ) + fadeIn(),
                exit = shrinkVertically(
                    animationSpec = spring(dampingRatio = 0.9f, stiffness = 500f)
                ) + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text(text = stringResource(R.string.control_more), style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = { toggleSectionButton(OpenSection.ADDITIONAL_ACTIONS) }
                        ) {
                            Icon(imageVector = Icons.Outlined.Close, contentDescription = stringResource(R.string.action_close))
                        }
                    }

                    val moreActions = buildList {
                        if (showAddCall) {
                            add(MoreAction(icons.addCall, stringResource(R.string.action_add_call), onAddCall, enabled = controlsEnabled && canAddCall))
                        }
                        if (canHold) add(MoreAction(icons.pause, stringResource(R.string.action_hold), onHold, isHolding == true, enabled = controlsEnabled))
                        if (canMerge) add(MoreAction(icons.merge, stringResource(R.string.conference_merge), onMerge, enabled = controlsEnabled))
                        if (canSwap) add(MoreAction(icons.swapCalls, stringResource(R.string.conference_swap), onSwap, enabled = controlsEnabled))
                        if (canManageConference) {
                            add(MoreAction(icons.more, stringResource(R.string.conference_manage), onManageConference, enabled = controlsEnabled))
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        moreActions.forEachIndexed { index, action ->
                            MoreActionRow(
                                action = action,
                                roundTop = index == 0,
                                roundBottom = index == moreActions.lastIndex
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = openSection.value == OpenSection.DIALPAD,
                enter = expandVertically(
                    animationSpec = spring(dampingRatio = 0.78f, stiffness = 420f)
                ) + fadeIn(),
                exit = shrinkVertically(
                    animationSpec = spring(dampingRatio = 0.9f, stiffness = 500f)
                ) + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(text = stringResource(R.string.control_dialpad), style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = { toggleSectionButton(OpenSection.DIALPAD) }
                        ) {
                            Icon(imageVector = Icons.Outlined.Close, contentDescription = stringResource(R.string.action_close))
                        }
                    }

                    TextField(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .fillMaxWidth(),
                        value = TextFieldValue(text = dialpadInput.value),
                        onValueChange = { dialpadInput.value = it.text },
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            fontSize = MaterialTheme.typography.headlineMedium.fontSize
                        ),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            errorIndicatorColor = Color.Transparent
                        )
                    )

                    Dialpad(
                        onDigitClick = ::handleDialpadDigit,
                        onDigitPress = onDigitPress,
                        onDigitRelease = onDigitRelease,
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, top = 16.dp)
            ) {
                CallButton(
                    icon = if (openSection.value == OpenSection.DIALPAD) {
                        icons.dialpadActive
                    } else {
                        icons.dialpad
                    },
                    label = stringResource(R.string.control_dialpad),
                    isActive = openSection.value == OpenSection.DIALPAD,
                    enabled = controlsEnabled,
                    onClick = { toggleSectionButton(OpenSection.DIALPAD) }
                )

                CallButton(
                    icon = icons.mute,
                    label = stringResource(R.string.control_mute),
                    isActive = isMuted,
                    enabled = controlsEnabled,
                    onClick = onMute
                )

                Box {
                    CallButton(
                        icon = icons.speaker,
                        label = currentAudioRoute?.label ?: stringResource(R.string.control_speaker),
                        isActive = isSpeaker == true || hasExternalAudioRoute,
                        enabled = controlsEnabled,
                        onClick = {
                            if (hasExternalAudioRoute) audioRoutesExpanded.value = true else onSpeaker()
                        }
                    )
                    DropdownMenu(
                        expanded = audioRoutesExpanded.value,
                        onDismissRequest = { audioRoutesExpanded.value = false }
                    ) {
                        audioRoutes.forEach { route ->
                            DropdownMenuItem(
                                text = { Text(if (route.isSelected) "${route.label} ✓" else route.label) },
                                onClick = {
                                    audioRoutesExpanded.value = false
                                    onAudioRouteSelected(route)
                                }
                            )
                        }
                    }
                }

                CallButton(
                    icon = icons.more,
                    label = stringResource(R.string.control_more),
                    isActive = openSection.value == OpenSection.ADDITIONAL_ACTIONS,
                    enabled = controlsEnabled,
                    onClick = { toggleSectionButton(OpenSection.ADDITIONAL_ACTIONS) }
                )
            }

            Surface(
                onClick = onHangup,
                enabled = canHangup,
                color = MaterialTheme.colorScheme.error,
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(224.dp)
                    .height(64.dp)
                    .semantics {
                        contentDescription = endCallDescription
                        role = Role.Button
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    AppIcon(
                        icon = icons.hangup,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onError
                    )
                }
            }
        }
    }
}

@Composable
private fun MoreActionRow(
    action: MoreAction,
    roundTop: Boolean,
    roundBottom: Boolean
) {
    Surface(
        onClick = action.onClick,
        enabled = action.enabled,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(
            topStart = if (roundTop) 20.dp else 2.dp,
            topEnd = if (roundTop) 20.dp else 2.dp,
            bottomStart = if (roundBottom) 20.dp else 2.dp,
            bottomEnd = if (roundBottom) 20.dp else 2.dp
        ),
        shadowElevation = 0.5.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Surface(
                color = if (action.isActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    AppIcon(
                        icon = action.icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (action.isActive) {
                            MaterialTheme.colorScheme.onPrimary
                        } else if (action.enabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = action.label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (action.enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }
    }
}

private data class MoreAction(
    val icon: IconSource,
    val label: String,
    val onClick: () -> Unit,
    val isActive: Boolean = false,
    val enabled: Boolean = true
)
