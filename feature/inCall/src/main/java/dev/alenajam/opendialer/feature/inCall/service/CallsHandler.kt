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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
        call.answer()
    }

    override fun hangup(call: OngoingCall, message: String?) {
        call.hangup(message)
    }

    override fun hold(call: OngoingCall, hold: Boolean?) {
        call.hold(hold)
    }

    override fun startDtmf(call: OngoingCall, digit: Char) {
        call.startDtmf(digit)
    }

    override fun stopDtmf(call: OngoingCall) {
        call.stopDtmf()
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
    override fun selectAudioRoute(route: CallAudioRouteUiState) = telecomAdapter.selectAudioRoute(route)
    override fun toggleMute() = telecomAdapter.toggleMute()

    // Lifecycle and Event Methods
    @MainThread
    fun addCall(call: Call, context: Context) {
        if (call.safeState == Call.STATE_DISCONNECTED) {
            OngoingCallHelper.handleDisconnectCause(context, call)
            return
        }

        var newOngoingCall: OngoingCall? = null
        _calls.update { currentMap ->
            if (currentMap.containsKey(call)) {
                currentMap[call]?.refreshIdentity()
                currentMap
            } else {
                val ongoingCall = OngoingCall(
                    context,
                    call,
                    onRemoved = { removeCall(it) },
                    sequence = nextCallSequence++
                )
                newOngoingCall = ongoingCall
                currentMap + (call to ongoingCall)
            }
        }

        newOngoingCall?.let { resolveContact(it) }
        reconcileCallsFromTelecom()
    }

    @MainThread
    fun removeCall(call: Call) {
        var removedCall: OngoingCall? = null
        _calls.update { currentMap ->
            removedCall = currentMap[call]
            currentMap - call
        }

        removedCall?.let { ongoingCall ->
            if (ongoingCall.shouldNotifyMissedCall) {
                _events.tryEmit(
                    CallEvent.MissedCall(
                        callerName = ongoingCall.callerName,
                        callerNumber = ongoingCall.callerNumber,
                        notificationId = ((System.currentTimeMillis() + ongoingCall.sequence) and Int.MAX_VALUE.toLong()).toInt()
                    )
                )
            }
            ongoingCall.tearDown()
        }
        reconcileCallsFromTelecom()
    }

    private fun reconcileCallsFromTelecom(): Boolean {
        val service = callService ?: return false
        var changed = false

        val callsToResolve = mutableListOf<OngoingCall>()

        _calls.update { currentMap ->
            val reconciledMap = currentMap.toMutableMap()
            var mapChanged = false

            fun addIfNeeded(call: Call) {
                if (call.safeState != Call.STATE_DISCONNECTED && !reconciledMap.containsKey(call)) {
                    val ongoingCall = OngoingCall(
                        context!!,
                        call,
                        onRemoved = { removeCall(it) },
                        sequence = nextCallSequence++
                    )
                    reconciledMap[call] = ongoingCall
                    callsToResolve.add(ongoingCall)
                    mapChanged = true
                }
            }

            for (call in service.calls) {
                addIfNeeded(call)
                for (child in call.children) {
                    addIfNeeded(child)
                }
            }

            if (mapChanged) {
                changed = true
                reconciledMap
            } else {
                currentMap
            }
        }

        callsToResolve.forEach { resolveContact(it) }
        return changed
    }

    private fun resolveContact(ongoingCall: OngoingCall) {
        val number = ongoingCall.callerNumber
        if (number.isEmpty() || ongoingCall.isConference) return
        scope.launch {
            val contact = contactResolver.resolve(number)
            if (_calls.value[ongoingCall.call] !== ongoingCall) return@launch
            if (number != ongoingCall.callerNumber) return@launch
            ongoingCall.applyContact(contact)
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
        val routes = listOf(
            CallAudioState.ROUTE_EARPIECE,
            CallAudioState.ROUTE_SPEAKER,
            CallAudioState.ROUTE_BLUETOOTH,
            CallAudioState.ROUTE_WIRED_HEADSET
        ).filter { newAudioState.supportedRouteMask and it != 0 }
            .map { route ->
                CallAudioRouteUiState(
                    type = route,
                    label = TelecomAdapter.defaultRouteLabel(route),
                    isSelected = route == newAudioState.route
                )
            }
        _audioState.value = CallAudioUiState(newAudioState.route, newAudioState.isMuted, routes)
    }

    @MainThread
    fun updateAvailableAudioRoutes(route: Int, routes: List<CallAudioRouteUiState>) {
        _audioState.update { current ->
            CallAudioUiState(
                route = route,
                isMuted = current?.isMuted == true,
                availableRoutes = routes
            )
        }
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
