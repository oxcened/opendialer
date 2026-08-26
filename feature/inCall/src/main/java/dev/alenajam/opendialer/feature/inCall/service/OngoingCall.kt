package dev.alenajam.opendialer.feature.inCall.service

import android.content.Context
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.VideoProfile
import dev.alenajam.opendialer.core.common.CommonUtils
import dev.alenajam.opendialer.core.common.Contact
import dev.alenajam.opendialer.feature.inCall.R
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OngoingCallState(
    val callerNumber: String = "",
    val callerNumberLabel: String = "",
    val callerName: String? = null,
    val callerImageUri: String? = null,
    val startTime: Long = -1,
    val totalTime: Long = 0,
    val state: Int = Call.STATE_NEW,
    val isConference: Boolean = false,
    val isConferenced: Boolean = false,
    val canMerge: Boolean = false,
    val canHold: Boolean = false,
    val canSplit: Boolean = false,
    val isAnonymous: Boolean = false
)

class OngoingCall(
    private val context: Context,
    val call: Call,
    private val onRemoved: (Call) -> Unit,
    val sequence: Long
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(OngoingCallState())
    val stateFlow: StateFlow<OngoingCallState> = _state.asStateFlow()

    companion object {
        private const val DTMF_DURATION_MS = 300L
    }

    private var lastState = Call.STATE_NEW
    private val missedCallTracker = MissedCallTracker()
    private var dtmfJob: Job? = null

    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, newState: Int) {
            super.onStateChanged(call, newState)
            updateCallState(newState)
        }

        override fun onConferenceableCallsChanged(call: Call, conferenceableCalls: List<Call>) {
            super.onConferenceableCallsChanged(call, conferenceableCalls)
            syncState()
        }

        override fun onParentChanged(call: Call, parent: Call?) {
            super.onParentChanged(call, parent)
            syncState()
        }

        override fun onDetailsChanged(call: Call, details: Call.Details) {
            super.onDetailsChanged(call, details)
            syncState()
        }
    }

    init {
        call.registerCallback(callback)
        refreshIdentity()
        updateCallState(call.state)
    }

    fun refreshIdentity() {
        val isConf = call.details.hasProperty(Call.Details.PROPERTY_CONFERENCE)
        val handle = call.details.handle
        val isAnon = handle == null
        val isVoicemail = handle?.scheme.equals("voicemail", ignoreCase = true)

        _state.update {
            it.copy(
                isConference = isConf,
                isAnonymous = isAnon,
                callerName = when {
                    isConf -> context.getString(R.string.conference_call)
                    isVoicemail -> context.getString(R.string.voicemail)
                    !isAnon -> handle?.schemeSpecificPart
                    else -> null
                },
                callerNumber = if (!isConf && !isAnon && !isVoicemail) handle?.schemeSpecificPart ?: "" else "",
                callerNumberLabel = "",
                callerImageUri = null
            )
        }
        syncState()
    }

    fun applyContact(contact: Contact?) {
        if (contact == null) return
        _state.update {
            it.copy(
                callerName = contact.name,
                callerImageUri = contact.imageUri,
                callerNumberLabel = ContactsContract.CommonDataKinds.Phone.getTypeLabel(
                    context.resources,
                    contact.phoneType,
                    contact.phoneLabel
                ).toString()
            )
        }
    }

    private fun syncState() {
        _state.update {
            it.copy(
                isConferenced = call.parent != null,
                canMerge = call.details.can(Call.Details.CAPABILITY_MERGE_CONFERENCE) || call.conferenceableCalls.isNotEmpty(),
                canHold = call.state == Call.STATE_HOLDING || call.details.can(Call.Details.CAPABILITY_HOLD),
                canSplit = call.details.can(Call.Details.CAPABILITY_SEPARATE_FROM_CONFERENCE),
                state = call.state
            )
        }
    }

    fun tearDown() {
        dtmfJob?.cancel()
        call.stopDtmfTone()
        call.unregisterCallback(callback)
        scope.cancel()
    }

    private fun updateCallState(state: Int) {
        if (state == lastState) return
        missedCallTracker.onStateChanged(state)

        when (state) {
            Call.STATE_HOLDING -> {
                if (lastState == Call.STATE_ACTIVE) {
                    accumulateActiveTime()
                }
            }
            Call.STATE_DISCONNECTED -> {
                if (lastState == Call.STATE_ACTIVE) {
                    accumulateActiveTime()
                }
                lastState = state
                onRemoved(call)
                OngoingCallHelper.handleDisconnectCause(context, call)
                return
            }
            Call.STATE_ACTIVE -> {
                _state.update { it.copy(startTime = CommonUtils.getCurrentTime()) }
            }
        }
        lastState = state
        syncState()
    }

    private fun accumulateActiveTime() {
        val current = _state.value
        if (current.startTime < 0) return
        val newTotal = current.totalTime + (CommonUtils.getCurrentTime() - current.startTime)
        _state.update { it.copy(totalTime = newTotal, startTime = -1) }
    }

    // Direct access to state for legacy/convenience
    val state: Int get() = _state.value.state
    val callerNumber: String get() = _state.value.callerNumber
    val callerName: String? get() = _state.value.callerName
    val callerNumberLabel: String get() = _state.value.callerNumberLabel
    val callerImageUri: String? get() = _state.value.callerImageUri
    val isConference: Boolean get() = _state.value.isConference
    val isConferenced: Boolean get() = _state.value.isConferenced
    val startTime: Long get() = _state.value.startTime
    val totalTime: Long get() = _state.value.totalTime
    val shouldNotifyMissedCall: Boolean get() = missedCallTracker.shouldNotify()

    fun answer() {
        if (!CallActionPolicy.shouldAnswer(state)) return
        call.answer(VideoProfile.STATE_AUDIO_ONLY)
    }

    fun hangup(message: String? = null) {
        if (CallActionPolicy.shouldReject(state)) {
            missedCallTracker.markLocallyDeclined()
            call.reject(message != null, message)
        } else {
            call.disconnect()
        }
    }

    fun markLocallyDeclined() {
        missedCallTracker.markLocallyDeclined()
    }

    fun hold(hold: Boolean? = null) {
        when (CallActionPolicy.holdAction(state, canBeHeld(), hold)) {
            CallActionPolicy.HoldAction.HOLD -> call.hold()
            CallActionPolicy.HoldAction.UNHOLD -> call.unhold()
            CallActionPolicy.HoldAction.NONE -> Unit
        }
    }

    fun canBeHeld(): Boolean = _state.value.canHold
    fun canBeMerged(): Boolean = _state.value.canMerge

    fun playDtmf(digit: Char) {
        if (!CallActionPolicy.canPlayDtmf(state)) return
        dtmfJob?.cancel()
        call.stopDtmfTone()
        call.playDtmfTone(digit)
        dtmfJob = scope.launch {
            delay(DTMF_DURATION_MS.milliseconds)
            call.stopDtmfTone()
        }
    }

    fun split() {
        if (!_state.value.canSplit) return
        call.splitFromConference()
    }

    fun merge() {
        if (!_state.value.canMerge) return
        val conferenceableCalls = call.conferenceableCalls
        if (conferenceableCalls.isNotEmpty()) {
            call.conference(conferenceableCalls[0])
        } else if (call.details.can(Call.Details.CAPABILITY_MERGE_CONFERENCE)) {
            call.mergeConference()
        }
    }
}
