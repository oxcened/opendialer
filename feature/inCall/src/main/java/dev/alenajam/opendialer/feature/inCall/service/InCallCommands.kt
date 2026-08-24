package dev.alenajam.opendialer.feature.inCall.service

interface InCallCommands {
    fun toggleSpeaker()
    fun toggleBluetooth()
    fun selectAudioRoute(route: CallAudioRouteUiState)
    fun toggleMute()
}
