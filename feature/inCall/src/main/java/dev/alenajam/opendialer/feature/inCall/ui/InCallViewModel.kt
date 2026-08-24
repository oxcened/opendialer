
package dev.alenajam.opendialer.feature.inCall.ui

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.telecom.Call
import android.telecom.CallAudioState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.alenajam.opendialer.core.common.CommonUtils
import dev.alenajam.opendialer.core.common.MAIN_ACTIVITY_INTENT_DIAL_EXTRA_ADD_CALL
import dev.alenajam.opendialer.feature.inCall.R
import dev.alenajam.opendialer.feature.inCall.service.CallDisplayState
import dev.alenajam.opendialer.feature.inCall.service.CallsHandler
import dev.alenajam.opendialer.feature.inCall.service.InCallCommands
import dev.alenajam.opendialer.feature.inCall.service.OngoingCall
import dev.alenajam.opendialer.feature.inCall.service.OngoingCallHelper
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@HiltViewModel
class InCallViewModel
@Inject constructor(
    callHandler: CallsHandler,
    private val inCallCommands: InCallCommands,
    private val app: Application
) : ViewModel() {
    private val displayState: LiveData<CallDisplayState> = callHandler.displayState
    private val calls: LiveData<Map<Call, OngoingCall>> = callHandler.calls
    private val audioState: LiveData<CallAudioState> = callHandler.audioState
    private val canAddCall: LiveData<Boolean> = callHandler.canAddCall
    private val _uiState = MediatorLiveData(InCallUiState())
    val uiState: LiveData<InCallUiState> = _uiState
    private var durationJob: Job? = null
    private var durationCall: OngoingCall? = null

    init {
        _uiState.addSource(displayState) { refreshUiState() }
        _uiState.addSource(calls) { refreshUiState() }
        _uiState.addSource(audioState) { refreshUiState() }
        _uiState.addSource(canAddCall) { refreshUiState() }
    }

    override fun onCleared() {
        super.onCleared()
        durationJob?.cancel()
        durationJob = null
        durationCall = null
    }

    private fun refreshUiState() {
        val callState = displayState.value ?: CallDisplayState()
        val primary = callState.primary
        val secondary = callState.secondary
        val audio = audioState.value
        updateDurationJob(primary)

        _uiState.value = InCallUiState(
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
            canAddCall = canAddCall.value == true,
            hasSecondaryCall = secondary != null,
            secondaryCallerName = secondary?.let { it.callerName ?: it.callerNumber },
            conferenceParticipants = calls.value.orEmpty().values
                .filter { it.isConferenced() }
                .map {
                    ConferenceParticipantUiState(
                        call = it,
                        callerName = (it.callerName ?: it.callerNumber).ifBlank { "Unknown" },
                        callerImageUri = it.callerImageUri,
                        state = it.state,
                        isConferenced = it.isConferenced()
                    )
                }
        )
    }

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

    private fun updateDurationJob(call: OngoingCall?) {
        if (call?.state != Call.STATE_ACTIVE) {
            durationJob?.cancel()
            durationJob = null
            durationCall = null
            return
        }
        if (durationCall === call && durationJob?.isActive == true) return

        durationJob?.cancel()
        durationCall = call
        durationJob = viewModelScope.launch {
            while (true) {
                delay(1_000)
                val currentState = _uiState.value ?: continue
                if (displayState.value?.primary !== call || call.state != Call.STATE_ACTIVE) break
                _uiState.value = currentState.copy(stateLabel = getDurationLabel(call))
            }
        }
    }

    private fun getDurationLabel(call: OngoingCall): String {
        val differenceTime = CommonUtils.getCurrentTime() - call.startTime + call.totalTime
        return CommonUtils.getDurationTimeString(differenceTime)
    }

    fun hangup(message: String? = null) = displayState.value?.primary?.hangup(message)
    fun answer() = displayState.value?.primary?.answer()
    fun turnSpeaker() = inCallCommands.toggleSpeaker()
    fun turnBluetooth() = inCallCommands.toggleBluetooth()
    fun turnMute() = inCallCommands.toggleMute()
    fun playDtmf(digit: Char) = displayState.value?.primary?.playDtmf(digit)
    fun hold() = displayState.value?.primary?.hold()

    fun addCall(activity: Activity) = activity.startActivity(
        Intent(Intent.ACTION_DIAL).putExtra(
            MAIN_ACTIVITY_INTENT_DIAL_EXTRA_ADD_CALL,
            true
        )
    )

    fun merge() = displayState.value?.primary?.let { OngoingCallHelper.merge(it) }

    fun swap() {
        val secondary = displayState.value?.secondary
        if (secondary == null) return
        secondary.hold(false)
    }

    fun split(call: OngoingCall) = call.split()

    fun hangup(call: OngoingCall) = call.hangup()
}
