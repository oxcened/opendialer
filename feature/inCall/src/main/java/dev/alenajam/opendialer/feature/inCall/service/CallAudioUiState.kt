package dev.alenajam.opendialer.feature.inCall.service

import android.telecom.CallAudioState

data class CallAudioUiState(
    val route: Int = CallAudioState.ROUTE_EARPIECE,
    val isMuted: Boolean = false
)
