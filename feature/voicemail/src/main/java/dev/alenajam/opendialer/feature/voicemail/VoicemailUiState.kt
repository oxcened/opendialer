package dev.alenajam.opendialer.feature.voicemail

import dev.alenajam.opendialer.data.voicemail.Voicemail

sealed interface VoicemailUiState {
    data object Loading : VoicemailUiState
    data object Unavailable : VoicemailUiState
    data class Available(val voicemails: List<Voicemail>) : VoicemailUiState
}
