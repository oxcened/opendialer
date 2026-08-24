package dev.alenajam.opendialer.feature.inCall.ui

import android.app.Activity
import android.content.Intent
import android.telecom.Call
import android.telecom.CallAudioState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.alenajam.opendialer.core.common.CommonUtils
import dev.alenajam.opendialer.core.common.MAIN_ACTIVITY_INTENT_DIAL_EXTRA_ADD_CALL
import dev.alenajam.opendialer.feature.inCall.service.CallEvent
import dev.alenajam.opendialer.feature.inCall.service.CallManager
import dev.alenajam.opendialer.feature.inCall.service.OngoingCall
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class InCallViewModel @Inject constructor(
    private val callManager: CallManager
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
            status = CallStatus.fromTelecomState(primary?.state),
            isHolding = primary?.state == Call.STATE_HOLDING,
            isSpeaker = audio?.route == CallAudioState.ROUTE_SPEAKER,
            isMuted = audio?.isMuted == true,
            audioRoutes = audio?.availableRoutes.orEmpty(),
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
                        status = CallStatus.fromTelecomState(it.state),
                        isConferenced = it.isConferenced
                    )
                }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InCallUiState())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val activeCallDuration: Flow<Long> = callManager.displayState
        .flatMapLatest { display ->
            val primary = display.primary
            if (primary != null) {
                flow {
                    while (true) {
                        if (primary.state == Call.STATE_ACTIVE) {
                            val differenceTime = CommonUtils.getCurrentTime() - primary.startTime + primary.totalTime
                            emit(differenceTime)
                        } else {
                            emit(0L)
                        }
                        delay(1000)
                    }
                }
            } else {
                flowOf(0L)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

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
    fun selectAudioRoute(route: dev.alenajam.opendialer.feature.inCall.service.CallAudioRouteUiState) =
        callManager.selectAudioRoute(route)
    fun turnMute() = callManager.toggleMute()

    fun swap() = callManager.swap()

    fun addCall(activity: Activity) = activity.startActivity(
        Intent(Intent.ACTION_DIAL).putExtra(
            MAIN_ACTIVITY_INTENT_DIAL_EXTRA_ADD_CALL,
            true
        )
    )
}
