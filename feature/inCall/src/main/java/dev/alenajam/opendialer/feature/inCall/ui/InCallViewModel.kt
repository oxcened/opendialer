package dev.alenajam.opendialer.feature.inCall.ui

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.telecom.Call
import android.telecom.CallAudioState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.alenajam.opendialer.core.common.CommonUtils
import dev.alenajam.opendialer.core.common.MAIN_ACTIVITY_INTENT_DIAL_EXTRA_ADD_CALL
import dev.alenajam.opendialer.feature.inCall.R
import dev.alenajam.opendialer.feature.inCall.service.CallEvent
import dev.alenajam.opendialer.feature.inCall.service.CallManager
import dev.alenajam.opendialer.feature.inCall.service.OngoingCall
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class InCallViewModel @Inject constructor(
    private val callManager: CallManager,
    private val app: Application
) : ViewModel() {
    val events: SharedFlow<CallEvent> = callManager.events
    val uiState: StateFlow<InCallUiState> = combine(
        callManager.displayState,
        callManager.calls,
        callManager.audioState,
        callManager.canAddCall
    ) { display, allCalls, audio, canAdd ->
        val primary = display.primary
        val secondary = display.secondary
        val conferenceChildren = primary?.call?.children.orEmpty().toSet()

        InCallUiState(
            stateLabel = getStateLabel(primary),
            isHolding = primary?.state == Call.STATE_HOLDING,
            isSpeaker = audio?.route == CallAudioState.ROUTE_SPEAKER,
            isMuted = audio?.isMuted == true,
            callerName = primary?.let { it.callerName ?: it.callerNumber }.orEmpty(),
            callerNumber = primary?.callerNumber.orEmpty(),
            callerNumberLabel = primary?.callerNumberLabel.orEmpty(),
            callerImageUri = primary?.callerImageUri,
            isIncoming = primary?.state == Call.STATE_RINGING,
            canHold = primary?.canBeHeld() == true,
            canMerge = primary?.canBeMerged() == true,
            canManageConference = primary?.isConference == true,
            canAddCall = canAdd && secondary == null,
            hasSecondaryCall = secondary != null,
            secondaryCallerName = secondary?.let { it.callerName ?: it.callerNumber },
            conferenceParticipants = allCalls.values
                .filter { it.isConferenced || conferenceChildren.contains(it.call) }
                .map {
                    ConferenceParticipantUiState(
                        call = it,
                        callerName = (it.callerName ?: it.callerNumber).ifBlank { "Unknown" },
                        callerImageUri = it.callerImageUri,
                        state = it.state,
                        isConferenced = it.isConferenced
                    )
                }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InCallUiState())

    val durationLabel: Flow<String> = flow {
        while (true) {
            val primary = callManager.displayState.value.primary
            if (primary?.state == Call.STATE_ACTIVE) {
                emit(getDurationLabel(primary))
            } else {
                emit("")
            }
            delay(1000)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private fun getStateLabel(call: OngoingCall?): String =
        when (call?.state) {
            Call.STATE_RINGING -> app.getString(R.string.call_ringing_title)
            Call.STATE_CONNECTING -> app.getString(R.string.call_connecting_title)
            Call.STATE_HOLDING -> app.getString(R.string.call_holding_title)
            Call.STATE_DIALING -> app.getString(R.string.call_dialing_title)
            Call.STATE_DISCONNECTING -> app.getString(R.string.call_disconnecting_title)
            Call.STATE_DISCONNECTED -> app.getString(R.string.call_disconnected_title)
            Call.STATE_ACTIVE -> getDurationLabel(call)
            else -> ""
        }

    private fun getDurationLabel(call: OngoingCall): String {
        val differenceTime = CommonUtils.getCurrentTime() - call.startTime + call.totalTime
        return CommonUtils.getDurationTimeString(differenceTime)
    }

    // Call Actions (Delegated to CallManager)
    fun hangup(message: String? = null) {
        callManager.displayState.value.primary?.let {
            callManager.hangup(it, message)
        }
    }

    fun answer() {
        callManager.displayState.value.primary?.let {
            callManager.answer(it)
        }
    }

    fun hold() {
        callManager.displayState.value.primary?.let {
            callManager.hold(it)
        }
    }

    fun playDtmf(digit: Char) {
        callManager.displayState.value.primary?.let {
            callManager.playDtmf(it, digit)
        }
    }

    fun merge() {
        callManager.displayState.value.primary?.let {
            callManager.merge(it)
        }
    }

    fun split(call: OngoingCall) = callManager.split(call)
    fun hangup(call: OngoingCall) = callManager.hangup(call)

    fun turnSpeaker() = callManager.toggleSpeaker()
    fun turnBluetooth() = callManager.toggleBluetooth()
    fun turnMute() = callManager.toggleMute()

    fun swap() = callManager.swap()

    fun addCall(activity: Activity) = activity.startActivity(
        Intent(Intent.ACTION_DIAL).putExtra(
            MAIN_ACTIVITY_INTENT_DIAL_EXTRA_ADD_CALL,
            true
        )
    )
}
