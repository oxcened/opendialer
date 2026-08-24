package dev.alenajam.opendialer.feature.inCall.service

data class CallDisplayState(
    val primary: OngoingCall? = null,
    val secondary: OngoingCall? = null
)
