package dev.alenajam.opendialer.feature.inCall.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.VideoProfile
import dev.alenajam.opendialer.core.common.CommonUtils
import dev.alenajam.opendialer.core.common.Contact
import dev.alenajam.opendialer.feature.inCall.R

class OngoingCall(
    private val context: Context,
    val call: Call,
    private val listener: Listener,
    val sequence: Long
) {
    interface Listener {
        fun onCallUpdated(call: OngoingCall)
        fun onCallRemoved(call: android.telecom.Call)
    }

    companion object {
        private const val DTMF_DURATION_MS = 300L
    }

    var callerNumber: String = ""
        private set
    var callerNumberLabel: String = ""
        private set
    var callerName: String? = null
        private set
    var callerImageUri: String? = null
        private set
    var startTime: Long = -1
        private set
    var totalTime: Long = 0
        private set

    private var lastState = Call.STATE_NEW
    private val mainHandler = Handler(Looper.getMainLooper())
    private val stopDtmfTone = Runnable { call.stopDtmfTone() }

    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, newState: Int) {
            super.onStateChanged(call, newState)
            updateState(newState)
            if (newState != Call.STATE_DISCONNECTED) listener.onCallUpdated(this@OngoingCall)
        }

        override fun onConferenceableCallsChanged(call: Call, conferenceableCalls: List<Call>) {
            super.onConferenceableCallsChanged(call, conferenceableCalls)
            listener.onCallUpdated(this@OngoingCall)
        }

        override fun onParentChanged(call: Call, parent: Call?) {
            super.onParentChanged(call, parent)
            listener.onCallUpdated(this@OngoingCall)
        }

        override fun onDetailsChanged(call: Call, details: Call.Details) {
            super.onDetailsChanged(call, details)
            listener.onCallUpdated(this@OngoingCall)
        }
    }

    init {
        call.registerCallback(callback)
        refreshIdentity()
        updateState(state ?: Call.STATE_NEW)
    }

    fun refreshIdentity() {
        callerName = null
        callerNumber = ""
        callerNumberLabel = ""
        callerImageUri = null
        if (isConference) {
            callerName = context.getString(R.string.conference_call)
        } else if (!isAnonymous) {
            val numberUri = call.details.handle
            callerNumber = numberUri?.schemeSpecificPart ?: ""
            callerName = callerNumber
        }
    }

    fun applyContact(contact: Contact?) {
        if (contact == null) return
        callerName = contact.name
        callerImageUri = contact.imageUri
        callerNumberLabel = ContactsContract.CommonDataKinds.Phone.getTypeLabel(
            context.resources,
            contact.phoneType,
            contact.phoneLabel
        ).toString()
    }

    fun tearDown() {
        mainHandler.removeCallbacks(stopDtmfTone)
        call.stopDtmfTone()
        call.unregisterCallback(callback)
    }

    fun updateState(state: Int) {
        handleCall(state)
    }

    private fun handleCall(state: Int) {
        if (state == lastState) return

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
                listener.onCallRemoved(call)
                OngoingCallHelper.handleDisconnectCause(context, call)
                return
            }
            Call.STATE_ACTIVE -> {
                this.startTime = CommonUtils.getCurrentTime()
            }
        }
        lastState = state
    }

    private fun accumulateActiveTime() {
        if (startTime < 0) return
        totalTime += CommonUtils.getCurrentTime() - startTime
        startTime = -1
    }

    val state: Int?
        get() = call.state

    fun answer() {
        if (state != Call.STATE_RINGING) return
        call.answer(VideoProfile.STATE_AUDIO_ONLY)
    }

    fun hangup() {
        if (state == Call.STATE_RINGING) {
            call.reject(false, null)
        } else {
            call.disconnect()
        }
    }

    fun hangup(message: String) {
        if (state == Call.STATE_RINGING) {
            call.reject(true, message)
        } else {
            call.disconnect()
        }
    }

    fun hold() {
        if (state == Call.STATE_HOLDING) {
            call.unhold()
        } else if (canBeHeld()) {
            call.hold()
        }
    }

    fun hold(hold: Boolean) {
        if (hold && canBeHeld()) call.hold()
        else if (!hold && state == Call.STATE_HOLDING) call.unhold()
    }

    fun playDtmf(digit: Char) {
        if (state != Call.STATE_ACTIVE) return
        mainHandler.removeCallbacks(stopDtmfTone)
        call.stopDtmfTone()
        call.playDtmfTone(digit)
        mainHandler.postDelayed(stopDtmfTone, DTMF_DURATION_MS)
    }

    val isAnonymous: Boolean
        get() = call.details.handle == null

    fun canBeMerged(): Boolean {
        if (call.details.can(Call.Details.CAPABILITY_MERGE_CONFERENCE)) return true
        if (call.conferenceableCalls.isNotEmpty()) return true
        return false
    }

    fun canBeHeld(): Boolean {
        return state == Call.STATE_HOLDING || call.details.can(Call.Details.CAPABILITY_HOLD)
    }

    fun canBeSplit(): Boolean {
        return call.details.can(Call.Details.CAPABILITY_SEPARATE_FROM_CONFERENCE)
    }

    fun split() {
        if (!canBeSplit()) return
        call.splitFromConference()
    }

    fun merge() {
        if (!canBeMerged()) return

        val conferenceableCalls = call.conferenceableCalls
        if (conferenceableCalls.isNotEmpty()) {
            call.conference(conferenceableCalls[0])
        } else if (call.details.can(Call.Details.CAPABILITY_MERGE_CONFERENCE)) {
            call.mergeConference()
        }
    }

    val isConference: Boolean
        get() = call.details.hasProperty(Call.Details.PROPERTY_CONFERENCE)

    val isConferenced: Boolean
        get() = call.parent != null
}
