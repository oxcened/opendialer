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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.alenajam.opendialer.core.common.ui.Dialpad
import dev.alenajam.opendialer.core.common.ui.LocalAppIcons

@Composable
fun InCallControls(
    isMuted: Boolean? = false,
    isSpeaker: Boolean? = false,
    isHolding: Boolean? = false,
    canManageConference: Boolean = false,
    canMerge: Boolean = false,
    canSwap: Boolean = false,
    canHold: Boolean = true,
    canAddCall: Boolean = true,
    onHangup: () -> Unit,
    onMute: () -> Unit,
    onSpeaker: () -> Unit,
    onHold: () -> Unit,
    onAddCall: () -> Unit,
    onMerge: () -> Unit = {},
    onSwap: () -> Unit = {},
    onManageConference: () -> Unit = {},
    onDigit: (digit: Char) -> Unit
) {
    val icons = LocalAppIcons.current
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
                        Text(text = "More", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = { toggleSectionButton(OpenSection.ADDITIONAL_ACTIONS) }
                        ) {
                            Icon(imageVector = Icons.Outlined.Close, contentDescription = "Close")
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            if (canAddCall) {
                                MoreActionRow(
                                    icon = icons.addCall,
                                    label = "Add call",
                                    onClick = onAddCall
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                            if (canHold) {
                                MoreActionRow(
                                    icon = icons.pause,
                                    label = "Hold",
                                    isActive = isHolding,
                                    onClick = onHold
                                )
                                if (canMerge || canSwap || canManageConference) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                }
                            }
                            if (canMerge) {
                                MoreActionRow(
                                    icon = icons.merge,
                                    label = "Merge",
                                    onClick = onMerge
                                )
                                if (canSwap || canManageConference) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                }
                            }
                            if (canSwap) {
                                MoreActionRow(
                                    icon = icons.swapCalls,
                                    label = "Swap",
                                    onClick = onSwap
                                )
                                if (canManageConference) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                }
                            }
                            if (canManageConference) {
                                MoreActionRow(
                                    icon = icons.more,
                                    label = "Manage",
                                    onClick = onManageConference
                                )
                            }
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
                            focusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            errorIndicatorColor = Color.Transparent
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
                    .padding(bottom = 24.dp, top = 24.dp)
            ) {
                CallButton(
                    icon = icons.dialpad,
                    label = "Dialpad",
                    isActive = openSection.value == OpenSection.DIALPAD,
                    onClick = { toggleSectionButton(OpenSection.DIALPAD) }
                )

                CallButton(
                    icon = icons.mute,
                    label = "Mute",
                    isActive = isMuted,
                    onClick = onMute
                )

                CallButton(
                    icon = icons.speaker,
                    label = "Speaker",
                    isActive = isSpeaker,
                    onClick = onSpeaker
                )

                CallButton(
                    icon = icons.more,
                    label = "More",
                    isActive = openSection.value == OpenSection.ADDITIONAL_ACTIONS,
                    onClick = { toggleSectionButton(OpenSection.ADDITIONAL_ACTIONS) }
                )
            }

            Surface(
                onClick = onHangup,
                color = MaterialTheme.colorScheme.error,
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(224.dp)
                    .height(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icons.hangup,
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
    icon: ImageVector,
    label: String,
    isActive: Boolean? = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Surface(
                color = if (isActive == true) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isActive == true) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
