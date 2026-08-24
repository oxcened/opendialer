package dev.alenajam.opendialer.core.common.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.CallMade
import androidx.compose.material.icons.automirrored.outlined.CallMissed
import androidx.compose.material.icons.automirrored.outlined.CallReceived
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.outlined.AddIcCall
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CallEnd
import androidx.compose.material.icons.outlined.Dialpad
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Merge
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.SwapCalls
import androidx.compose.material.icons.outlined.Voicemail
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.vector.ImageVector

@Immutable
data class AppIcons(
    val hangup: ImageVector,
    val dialpad: ImageVector,
    val mute: ImageVector,
    val speaker: ImageVector,
    val more: ImageVector,
    val pause: ImageVector,
    val addCall: ImageVector,
    val accountCircle: ImageVector,
    val callReceived: ImageVector,
    val callMade: ImageVector,
    val callMissed: ImageVector,
    val voicemail: ImageVector,
    val block: ImageVector,
    val phone: ImageVector,
    val message: ImageVector,
    val personAdd: ImageVector,
    val history: ImageVector,
    val merge: ImageVector,
    val swapCalls: ImageVector
)

val DefaultAppIcons = AppIcons(
    hangup = Icons.Outlined.CallEnd,
    dialpad = Icons.Outlined.Dialpad,
    mute = Icons.Outlined.MicOff,
    speaker = Icons.AutoMirrored.Outlined.VolumeUp,
    more = Icons.Outlined.MoreVert,
    pause = Icons.Outlined.Pause,
    addCall = Icons.Outlined.AddIcCall,
    accountCircle = Icons.Filled.AccountCircle,
    callReceived = Icons.AutoMirrored.Outlined.CallReceived,
    callMade = Icons.AutoMirrored.Outlined.CallMade,
    callMissed = Icons.AutoMirrored.Outlined.CallMissed,
    voicemail = Icons.Outlined.Voicemail,
    block = Icons.Outlined.Block,
    phone = Icons.Outlined.Phone,
    message = Icons.AutoMirrored.Outlined.Message,
    personAdd = Icons.Outlined.PersonAddAlt,
    history = Icons.Outlined.History,
    merge = Icons.Outlined.Merge,
    swapCalls = Icons.Outlined.SwapCalls
)

val LocalAppIcons = staticCompositionLocalOf { DefaultAppIcons }
