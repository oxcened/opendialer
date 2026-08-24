package dev.alenajam.opendialer.feature.inCall.service

import android.content.Context
import android.telecom.Call
import android.telecom.CallAudioState
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dev.alenajam.opendialer.feature.inCall.R
import dev.alenajam.opendialer.feature.inCall.ui.InCallActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallsHandler @Inject constructor(
    private val contactResolver: CallContactResolver
) {
    private val _calls = MutableLiveData<Map<Call, OngoingCall>>(HashMap())
    val calls: LiveData<Map<Call, OngoingCall>> = _calls

    private val _displayState = MutableLiveData(CallDisplayState(null, null))
    val displayState: LiveData<CallDisplayState> = _displayState

    private val _audioState = MutableLiveData<CallAudioUiState?>()
    val audioState: LiveData<CallAudioUiState?> = _audioState

    private val _canAddCall = MutableLiveData<Boolean>()
    val canAddCall: LiveData<Boolean> = _canAddCall

    private var callService: InCallServiceImpl? = null
    private var inCallActivity: InCallActivity? = null

    private var context: Context? = null
    private var proximitySensor: ProximitySensor? = null
    private var nextCallSequence: Long = 0

    fun setInCallActivity(inCallActivity: InCallActivity) {
        this.inCallActivity = inCallActivity
    }

    fun clearInCallActivity(inCallActivity: InCallActivity) {
        if (inCallActivity === this.inCallActivity) {
            this.inCallActivity = null
        }
    }

    fun isActivityStarted(): Boolean {
        return inCallActivity != null && !inCallActivity!!.isDestroyed && !inCallActivity!!.isFinishing
    }

    fun isActivityShowing(): Boolean {
        if (!isActivityStarted()) return false
        return inCallActivity!!.visibility
    }

    @MainThread
    @Suppress("DEPRECATION")
    fun addCall(call: Call, context: Context) {
        if (call.state == Call.STATE_DISCONNECTED) {
            OngoingCallHelper.handleDisconnectCause(context, call)
            return
        }

        val map = HashMap(_calls.value ?: emptyMap())
        var ongoingCall = map[call]

        if (ongoingCall == null) {
            ongoingCall = OngoingCall(context, call, this, nextCallSequence++)
            map[call] = ongoingCall
            resolveContact(ongoingCall)
        } else {
            ongoingCall.refreshIdentity()
        }

        _calls.value = map
        updateCalls()
    }

    @MainThread
    fun removeCall(call: Call) {
        val currentMap = _calls.value ?: return
        val map = HashMap(currentMap)

        val ongoingCall = map.remove(call) ?: return
        ongoingCall.tearDown()

        _calls.value = map
        updateCalls()
    }

    @MainThread
    fun updateCalls() {
        var map = _calls.value ?: emptyMap()
        if (reconcileCallsFromTelecom()) {
            map = _calls.value ?: emptyMap()
        }

        var selection = selectDisplayCalls(map)
        if ((map.isEmpty() || selection.primary == null) && reconcileCallsFromTelecom()) {
            map = _calls.value ?: emptyMap()
            selection = selectDisplayCalls(map)
        }

        if (map.isEmpty()) {
            _displayState.value = CallDisplayState(null, null)
            attemptFinishActivity()
            NotificationHelper.tearDown(callService)
            return
        }

        var primary = selection.primary
        if (primary == null) {
            primary = getPreviousVisibleCall(map)
        }
        if (primary == null) {
            primary = getFirstTrackedCall(map)
        }

        if (primary != null) {
            val secondary = selection.secondary
            _displayState.value = CallDisplayState(primary, secondary)
            handleCallNotification(primary, primary.state ?: Call.STATE_NEW)
            if (primary.state == Call.STATE_DIALING) attemptStartActivity()
            updateProximitySensor(primary)
        } else {
            _displayState.value = CallDisplayState(null, null)
        }
    }

    private fun reconcileCallsFromTelecom(): Boolean {
        val service = callService ?: return false

        val reconciledCalls = HashMap(_calls.value ?: emptyMap())
        var changed = false
        for (call in service.calls) {
            changed = changed or addReconciledCall(reconciledCalls, call)
            for (child in call.children) {
                changed = changed or addReconciledCall(reconciledCalls, child)
            }
        }

        if (!changed) return false

        _calls.value = reconciledCalls
        return true
    }

    private fun addReconciledCall(reconciledCalls: MutableMap<Call, OngoingCall>, call: Call): Boolean {
        if (call.state == Call.STATE_DISCONNECTED || reconciledCalls.containsKey(call)) {
            return false
        }

        val ongoingCall = OngoingCall(context!!, call, this, nextCallSequence++)
        reconciledCalls[call] = ongoingCall
        resolveContact(ongoingCall)
        return true
    }

    @MainThread
    fun onCallDetailsChanged(ongoingCall: OngoingCall) {
        ongoingCall.refreshIdentity()
        resolveContact(ongoingCall)
        updateCalls()
    }

    private fun resolveContact(ongoingCall: OngoingCall) {
        val number = ongoingCall.callerNumber
        if (number.isEmpty() || ongoingCall.isConference) return

        contactResolver.resolve(number) { contact ->
            val currentCalls = _calls.value
            if (currentCalls == null || currentCalls[ongoingCall.call] !== ongoingCall) return@resolve
            if (number != ongoingCall.callerNumber) return@resolve

            ongoingCall.applyContact(contact)
            updateCalls()
        }
    }

    private fun getPreviousVisibleCall(map: Map<Call, OngoingCall>): OngoingCall? {
        val previous = _displayState.value ?: return null
        val call = previous.primary ?: return null

        return if (map.containsValue(call) && call.state != Call.STATE_DISCONNECTED) call else null
    }

    private fun getFirstTrackedCall(map: Map<Call, OngoingCall>): OngoingCall? {
        var first: OngoingCall? = null
        for (current in map.values) {
            if (current.state == Call.STATE_DISCONNECTED) continue
            if (first == null || current.sequence < first.sequence) first = current
        }
        return first
    }

    @Suppress("DEPRECATION")
    private fun handleCallNotification(call: OngoingCall, state: Int) {
        val service = callService ?: return
        var caller = call.callerName
        if (caller.isNullOrEmpty()) caller = call.callerNumber
        if (caller.isNullOrEmpty()) caller = context?.getString(R.string.anonymous) ?: "Anonymous"

        when (state) {
            Call.STATE_RINGING -> {
                if (!isActivityShowing())
                    NotificationHelper.notifyIncomingCall(context!!, service, caller)
            }
            Call.STATE_DIALING -> {
                NotificationHelper.notifyOutgoingCall(context!!, service, caller)
            }
            Call.STATE_ACTIVE -> {
                NotificationHelper.notifyOngoingCall(context!!, service, caller)
            }
            Call.STATE_HOLDING -> {
                NotificationHelper.notifyOnHoldCall(context!!, service, caller)
            }
            Call.STATE_DISCONNECTING -> {
                NotificationHelper.notifyDisconnectingCall(context!!, service, caller)
            }
        }
    }

    private fun updateProximitySensor(pCall: OngoingCall?) {
        var call = pCall
        if (call == null) {
            val currentDisplayState = _displayState.value
            call = currentDisplayState?.primary
        }

        val sensor = proximitySensor ?: return
        val currentCall = call ?: return
        val currentAudioState = _audioState.value ?: return

        val state = currentCall.state ?: Call.STATE_NEW
        val audioRoute = currentAudioState.route
        sensor.updateProximitySensorMode(state, audioRoute)
    }

    private fun selectDisplayCalls(map: Map<Call, OngoingCall>): CallDisplaySelector.Selection<OngoingCall> {
        val candidates = ArrayList<CallDisplaySelector.Candidate<OngoingCall>>()
        for (call in map.values) {
            candidates.add(
                CallDisplaySelector.Candidate(
                    call,
                    call.state ?: Call.STATE_NEW,
                    call.isConferenced,
                    call.sequence
                )
            )
        }
        return CallDisplaySelector.select(candidates)
    }

    fun attemptFinishActivity() {
        if (isActivityStarted()) {
            inCallActivity?.finish()
        }
    }

    fun attemptStartActivity() {
        if (!isActivityShowing() && context != null) {
            InCallActivity.start(context!!)
        }
    }

    @MainThread
    fun updateCallAudioState(newAudioState: CallAudioState) {
        _audioState.value = CallAudioUiState(newAudioState.route, newAudioState.isMuted)
        updateProximitySensor(null)
    }

    @MainThread
    fun updateCallEndpoint(route: Int) {
        val current = _audioState.value
        _audioState.value = CallAudioUiState(route, current?.isMuted == true)
        updateProximitySensor(null)
    }

    @MainThread
    fun updateMuteState(isMuted: Boolean) {
        val current = _audioState.value
        val route = current?.route ?: CallAudioState.ROUTE_EARPIECE
        _audioState.value = CallAudioUiState(route, isMuted)
    }

    @MainThread
    fun updateCanAddCall(newCanAddCall: Boolean) {
        _canAddCall.value = newCanAddCall
    }

    fun setup(callService: InCallServiceImpl, context: Context, proximitySensor: ProximitySensor) {
        this.callService = callService
        this.context = context
        this.proximitySensor = proximitySensor
        updateCalls()
    }

    fun tearDown() {
        val map = _calls.value
        if (map != null) {
            for (call in map.values) {
                call.tearDown()
            }
        }
        _calls.value = HashMap()
        _displayState.value = CallDisplayState(null, null)
        _audioState.value = null
        _canAddCall.value = false
        NotificationHelper.tearDown(callService)
        callService = null
        context = null
        proximitySensor?.tearDown()
        proximitySensor = null
    }
}
