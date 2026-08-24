package dev.alenajam.opendialer.feature.inCall.service

data class CallDisplayState(
    val primary: OngoingCall? = null,
    val primaryState: OngoingCallState? = null,
    val secondary: OngoingCall? = null,
    val secondaryState: OngoingCallState? = null
)
