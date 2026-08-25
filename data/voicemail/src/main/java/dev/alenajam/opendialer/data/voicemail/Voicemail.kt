package dev.alenajam.opendialer.data.voicemail

import android.net.Uri

data class Voicemail(
    val id: Long,
    val uri: Uri,
    val number: String?,
    val date: Long,
    val transcription: String?,
    val hasContent: Boolean,
)
