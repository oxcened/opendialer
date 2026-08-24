package dev.alenajam.opendialer.feature.inCall.service

import kotlinx.coroutines.flow.StateFlow

interface CallManager : InCallCommands {
    val calls: StateFlow<Map<android.telecom.Call, OngoingCall>>
    val displayState: StateFlow<CallDisplayState>
    val audioState: StateFlow<CallAudioUiState?>
    val canAddCall: StateFlow<Boolean>

    fun answer(call: OngoingCall)
    fun hangup(call: OngoingCall, message: String? = null)
    fun hold(call: OngoingCall, hold: Boolean? = null)
    fun playDtmf(call: OngoingCall, digit: Char)
    fun merge(call: OngoingCall)
    fun split(call: OngoingCall)
    fun swap()
}
