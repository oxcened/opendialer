package dev.alenajam.opendialer.core.common.ui

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.automirrored.outlined.CallMade
import androidx.compose.material.icons.automirrored.outlined.CallMissed
import androidx.compose.material.icons.automirrored.outlined.CallReceived
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Voicemail
import androidx.compose.material.icons.outlined.AddIcCall
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CallEnd
import androidx.compose.material.icons.outlined.Dialpad
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Merge
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PhonePaused
import androidx.compose.material.icons.outlined.SwapCalls
import androidx.compose.material.icons.outlined.Voicemail
import androidx.compose.foundation.Image
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource

@Immutable
sealed interface IconSource {
    val tintable: Boolean

    data class Vector(
        val imageVector: ImageVector,
        override val tintable: Boolean = true
    ) : IconSource

    data class Resource(
        @DrawableRes val resId: Int,
        override val tintable: Boolean = true
    ) : IconSource
}

@Composable
fun IconSource.rememberPainter(): Painter = when (this) {
    is IconSource.Vector -> rememberVectorPainter(imageVector)
    is IconSource.Resource -> painterResource(resId)
}

@Composable
fun AppIcon(
    icon: IconSource,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    if (icon.tintable) {
        androidx.compose.material3.Icon(
            painter = icon.rememberPainter(),
            contentDescription = contentDescription,
            modifier = modifier,
            tint = tint
        )
    } else {
        Image(
            painter = icon.rememberPainter(),
            contentDescription = contentDescription,
            modifier = modifier
        )
    }
}

@Immutable
data class AppIcons(
    val hangup: IconSource,
    val dialpad: IconSource,
    val mute: IconSource,
    val speaker: IconSource,
    val more: IconSource,
    val pause: IconSource,
    val addCall: IconSource,
    val accountCircle: IconSource,
    val callReceived: IconSource,
    val callMade: IconSource,
    val callMissed: IconSource,
    val voicemail: IconSource,
    val voicemailSelected: IconSource = voicemail,
    val block: IconSource,
    val phone: IconSource,
    val message: IconSource,
    val personAdd: IconSource,
    val history: IconSource,
    val merge: IconSource,
    val swapCalls: IconSource,
    val phonePaused: IconSource,
    val recents: IconSource,
    val recentsSelected: IconSource = recents,
    val contacts: IconSource,
    val contactsSelected: IconSource = contacts,
    val voicemailLarge: IconSource = voicemail
)

val DefaultAppIcons = AppIcons(
    hangup = IconSource.Vector(Icons.Outlined.CallEnd),
    dialpad = IconSource.Vector(Icons.Outlined.Dialpad),
    mute = IconSource.Vector(Icons.Outlined.MicOff),
    speaker = IconSource.Vector(Icons.AutoMirrored.Outlined.VolumeUp),
    more = IconSource.Vector(Icons.Outlined.MoreVert),
    pause = IconSource.Vector(Icons.Outlined.Pause),
    addCall = IconSource.Vector(Icons.Outlined.AddIcCall),
    accountCircle = IconSource.Vector(Icons.Filled.AccountCircle),
    callReceived = IconSource.Vector(Icons.AutoMirrored.Outlined.CallReceived),
    callMade = IconSource.Vector(Icons.AutoMirrored.Outlined.CallMade),
    callMissed = IconSource.Vector(Icons.AutoMirrored.Outlined.CallMissed),
    voicemail = IconSource.Vector(Icons.Outlined.Voicemail),
    voicemailSelected = IconSource.Vector(Icons.Filled.Voicemail),
    block = IconSource.Vector(Icons.Outlined.Block),
    phone = IconSource.Vector(Icons.Outlined.Phone),
    message = IconSource.Vector(Icons.AutoMirrored.Outlined.Message),
    personAdd = IconSource.Vector(Icons.Outlined.PersonAddAlt),
    history = IconSource.Vector(Icons.Outlined.History),
    merge = IconSource.Vector(Icons.Outlined.Merge),
    swapCalls = IconSource.Vector(Icons.Outlined.SwapCalls),
    phonePaused = IconSource.Vector(Icons.Outlined.PhonePaused),
    recents = IconSource.Vector(Icons.Outlined.AccessTime),
    recentsSelected = IconSource.Vector(Icons.Filled.AccessTimeFilled),
    contacts = IconSource.Vector(Icons.Outlined.People),
    contactsSelected = IconSource.Vector(Icons.Filled.People),
    voicemailLarge = IconSource.Vector(Icons.Outlined.Voicemail)
)

val LocalAppIcons = staticCompositionLocalOf { DefaultAppIcons }
