package dev.alenajam.opendialer.feature.inCall.service

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface CallManager : InCallCommands {
    val calls: StateFlow<Map<android.telecom.Call, OngoingCall>>
    val displayState: StateFlow<CallDisplayState>
    val audioState: StateFlow<CallAudioUiState?>
    val canAddCall: StateFlow<Boolean>
    val events: SharedFlow<CallEvent>

    fun answer(call: OngoingCall)
    fun hangup(call: OngoingCall, message: String? = null)
    fun hold(call: OngoingCall, hold: Boolean? = null)
    fun playDtmf(call: OngoingCall, digit: Char)
    fun merge(call: OngoingCall)
    fun split(call: OngoingCall)
    fun swap()
}

sealed class CallEvent {
    object FinishActivity : CallEvent()

    data class MissedCall(
        val callerName: String?,
        val callerNumber: String,
        val notificationId: Int
    ) : CallEvent()
}
