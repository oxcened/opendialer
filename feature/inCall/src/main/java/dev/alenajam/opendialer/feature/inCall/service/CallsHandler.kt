package dev.alenajam.opendialer.feature.inCall.service

import android.content.Context
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.VideoProfile
import androidx.annotation.MainThread
import dev.alenajam.opendialer.feature.inCall.ui.InCallActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallsHandler @Inject constructor(
    private val contactResolver: CallContactResolver,
    private val telecomAdapter: TelecomAdapter
) : CallManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _calls = MutableStateFlow<Map<Call, OngoingCall>>(emptyMap())
    override val calls: StateFlow<Map<Call, OngoingCall>> = _calls.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    override val displayState: StateFlow<CallDisplayState> = _calls
        .flatMapLatest { map ->
            if (map.isEmpty()) {
                flowOf(CallDisplayState(null, null))
            } else {
                combine(map.values.map { it.stateFlow }) { _ ->
                    deriveDisplayState(map)
                }
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, CallDisplayState(null, null))

    private val _audioState = MutableStateFlow<CallAudioUiState?>(null)
    override val audioState: StateFlow<CallAudioUiState?> = _audioState.asStateFlow()

    private val _canAddCall = MutableStateFlow(false)
    override val canAddCall: StateFlow<Boolean> = _canAddCall.asStateFlow()

    private val _events = MutableSharedFlow<CallEvent>(extraBufferCapacity = 1)
    override val events: SharedFlow<CallEvent> = _events.asSharedFlow()

    private var callService: InCallServiceImpl? = null
    private var context: Context? = null
    private var nextCallSequence: Long = 0

    init {
        // Observe displayState to handle side-effects like Activity start/finish
        displayState.onEach { state ->
            if (state.primary == null && _calls.value.isEmpty()) {
                _events.tryEmit(CallEvent.FinishActivity)
            } else if (state.primary?.state == Call.STATE_DIALING) {
                attemptStartActivity()
            }
        }.launchIn(scope)
    }

    private fun deriveDisplayState(map: Map<Call, OngoingCall>): CallDisplayState {
        val selection = selectDisplayCalls(map)
        val primary = selection.primary ?: getPreviousVisibleCall(map) ?: getFirstTrackedCall(map)
        return CallDisplayState(
            primary = primary,
            primaryState = primary?.stateFlow?.value,
            secondary = selection.secondary,
            secondaryState = selection.secondary?.stateFlow?.value
        )
    }

    // CallManager Implementation (Delegated Actions)
    override fun answer(call: OngoingCall) {
        if (call.state == Call.STATE_RINGING) {
            call.call.answer(VideoProfile.STATE_AUDIO_ONLY)
        }
    }

    override fun hangup(call: OngoingCall, message: String?) {
        if (call.state == Call.STATE_RINGING) {
            call.call.reject(message != null, message)
        } else {
            call.call.disconnect()
        }
    }

    override fun hold(call: OngoingCall, hold: Boolean?) {
        val shouldHold = hold ?: (call.state != Call.STATE_HOLDING)
        if (shouldHold && call.canBeHeld()) {
            call.call.hold()
        } else if (!shouldHold && call.state == Call.STATE_HOLDING) {
            call.call.unhold()
        }
    }

    override fun playDtmf(call: OngoingCall, digit: Char) {
        call.playDtmf(digit)
    }

    override fun merge(call: OngoingCall) {
        call.merge()
    }

    override fun split(call: OngoingCall) {
        call.split()
    }

    override fun swap() {
        val secondary = displayState.value.secondary ?: return
        hold(secondary, false)
    }

    // Audio Commands (Delegated to TelecomAdapter)
    override fun toggleSpeaker() = telecomAdapter.toggleSpeaker()
    override fun toggleBluetooth() = telecomAdapter.toggleBluetooth()
    override fun toggleMute() = telecomAdapter.toggleMute()

    // Lifecycle and Event Methods
    @MainThread
    fun addCall(call: Call, context: Context) {
        if (call.state == Call.STATE_DISCONNECTED) {
            OngoingCallHelper.handleDisconnectCause(context, call)
            return
        }

        val map = HashMap(_calls.value)
        var ongoingCall = map[call]

        if (ongoingCall == null) {
            ongoingCall = OngoingCall(
                context,
                call,
                onRemoved = { removeCall(it) },
                sequence = nextCallSequence++
            )
            map[call] = ongoingCall
            resolveContact(ongoingCall)
        } else {
            ongoingCall.refreshIdentity()
        }

        _calls.value = map
        reconcileCallsFromTelecom()
    }

    @MainThread
    fun removeCall(call: Call) {
        val map = HashMap(_calls.value)
        val ongoingCall = map.remove(call) ?: return
        ongoingCall.tearDown()
        _calls.value = map
        reconcileCallsFromTelecom()
    }

    private fun reconcileCallsFromTelecom(): Boolean {
        val service = callService ?: return false
        val reconciledCalls = HashMap(_calls.value)
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
        if (call.state == Call.STATE_DISCONNECTED || reconciledCalls.containsKey(call)) return false
        val ongoingCall = OngoingCall(
            context!!,
            call,
            onRemoved = { removeCall(it) },
            sequence = nextCallSequence++
        )
        reconciledCalls[call] = ongoingCall
        resolveContact(ongoingCall)
        return true
    }

    private fun resolveContact(ongoingCall: OngoingCall) {
        val number = ongoingCall.callerNumber
        if (number.isEmpty() || ongoingCall.isConference) return
        contactResolver.resolve(number) { contact ->
            if (_calls.value[ongoingCall.call] !== ongoingCall) return@resolve
            if (number != ongoingCall.callerNumber) return@resolve
            ongoingCall.applyContact(contact)
            // No need to manually trigger update, stateFlow emission in OngoingCall handles it
        }
    }

    private fun getPreviousVisibleCall(map: Map<Call, OngoingCall>): OngoingCall? {
        val previous = displayState.value.primary ?: return null
        return if (map.containsValue(previous) && previous.state != Call.STATE_DISCONNECTED) previous else null
    }

    private fun getFirstTrackedCall(map: Map<Call, OngoingCall>): OngoingCall? {
        return map.values.filter { it.state != Call.STATE_DISCONNECTED }.minByOrNull { it.sequence }
    }

    private fun selectDisplayCalls(map: Map<Call, OngoingCall>): CallDisplaySelector.Selection<OngoingCall> {
        val candidates = map.values.map {
            CallDisplaySelector.Candidate(it, it.state, it.isConferenced, it.sequence)
        }
        return CallDisplaySelector.select(candidates)
    }

    fun setup(callService: InCallServiceImpl, context: Context) {
        this.callService = callService
        this.context = context
        reconcileCallsFromTelecom()
    }

    fun tearDown() {
        _calls.value.values.forEach { it.tearDown() }
        _calls.value = emptyMap()
        _audioState.value = null
        _canAddCall.value = false
        callService = null
        context = null
    }

    @MainThread
    fun updateCallAudioState(newAudioState: CallAudioState) {
        _audioState.value = CallAudioUiState(newAudioState.route, newAudioState.isMuted)
    }

    @MainThread
    fun updateCallEndpoint(route: Int) {
        val isMuted = _audioState.value?.isMuted == true
        _audioState.value = CallAudioUiState(route, isMuted)
    }

    @MainThread
    fun updateMuteState(isMuted: Boolean) {
        val route = _audioState.value?.route ?: CallAudioState.ROUTE_EARPIECE
        _audioState.value = CallAudioUiState(route, isMuted)
    }

    @MainThread
    fun updateCanAddCall(newCanAddCall: Boolean) {
        _canAddCall.value = newCanAddCall
    }

    fun attemptStartActivity() {
        context?.let { InCallActivity.start(it) }
    }
}
