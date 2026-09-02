package dev.alenajam.opendialer.core.common.ui

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.automirrored.outlined.CallMade
import androidx.compose.material.icons.automirrored.outlined.CallMissed
import androidx.compose.material.icons.automirrored.outlined.CallReceived
import androidx.compose.material.icons.automirrored.outlined.CallSplit
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Voicemail
import androidx.compose.material.icons.outlined.AddIcCall
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CallEnd
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Dialpad
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Merge
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PhonePaused
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SwapCalls
import androidx.compose.material.icons.outlined.Voicemail
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

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
    modifier: Modifier = Modifier.size(24.dp),
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
    val dialpadActive: IconSource = dialpad,
    val mute: IconSource,
    val speaker: IconSource,
    val more: IconSource,
    val backspace: IconSource = IconSource.Vector(Icons.AutoMirrored.Outlined.Backspace),
    val pause: IconSource,
    val addCall: IconSource,
    val person: IconSource,
    val conference: IconSource = IconSource.Vector(Icons.Outlined.People),
    val callReceived: IconSource,
    val callMade: IconSource,
    val callMissed: IconSource,
    val voicemail: IconSource,
    val voicemailSelected: IconSource = voicemail,
    val block: IconSource,
    val blockCall: IconSource = block,
    val phone: IconSource,
    val message: IconSource,
    val personAdd: IconSource,
    val personAddInContactsList: IconSource = personAdd,
    val history: IconSource,
    val merge: IconSource,
    val split: IconSource = IconSource.Vector(Icons.AutoMirrored.Outlined.CallSplit),
    val swapCalls: IconSource,
    val phonePaused: IconSource,
    val recents: IconSource,
    val recentsSelected: IconSource = recents,
    val contacts: IconSource,
    val contactsSelected: IconSource = contacts,
    val voicemailLarge: IconSource = voicemail,
    val search: IconSource,
    val close: IconSource,
    val share: IconSource,
    val edit: IconSource,
    val copy: IconSource,
    val delete: IconSource,
    val arrowLeft: IconSource,
    val arrowRight: IconSource,
    val arrowUp: IconSource,
    val arrowDown: IconSource,
    val favorite: IconSource
)

val DefaultAppIcons = AppIcons(
    hangup = IconSource.Vector(Icons.Outlined.CallEnd),
    dialpad = IconSource.Vector(Icons.Outlined.Dialpad),
    mute = IconSource.Vector(Icons.Outlined.MicOff),
    speaker = IconSource.Vector(Icons.AutoMirrored.Outlined.VolumeUp),
    more = IconSource.Vector(Icons.Outlined.MoreVert),
    pause = IconSource.Vector(Icons.Outlined.Pause),
    addCall = IconSource.Vector(Icons.Outlined.AddIcCall),
    person = IconSource.Vector(Icons.Outlined.Person),
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
    voicemailLarge = IconSource.Vector(Icons.Outlined.Voicemail),
    search = IconSource.Vector(Icons.Outlined.Search),
    close = IconSource.Vector(Icons.Outlined.Close),
    share = IconSource.Vector(Icons.Outlined.Share),
    edit = IconSource.Vector(Icons.Outlined.Edit),
    copy = IconSource.Vector(Icons.Outlined.ContentCopy),
    delete = IconSource.Vector(Icons.Outlined.Delete),
    arrowLeft = IconSource.Vector(Icons.AutoMirrored.Filled.ArrowBack),
    arrowRight = IconSource.Vector(Icons.AutoMirrored.Filled.ArrowForward),
    arrowUp = IconSource.Vector(Icons.Filled.KeyboardArrowUp),
    arrowDown = IconSource.Vector(Icons.Filled.KeyboardArrowDown),
    favorite = IconSource.Vector(Icons.Filled.Star)
)

val LocalAppIcons = staticCompositionLocalOf { DefaultAppIcons }
