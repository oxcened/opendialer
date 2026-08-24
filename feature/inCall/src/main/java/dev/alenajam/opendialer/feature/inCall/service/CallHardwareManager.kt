package dev.alenajam.opendialer.feature.inCall.service

import android.telecom.Call
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallHardwareManager @Inject constructor(
    private val callManager: CallManager,
    private val proximitySensor: ProximitySensor
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var observationJob: Job? = null

    fun attach() {
        startObserving()
    }

    fun detach() {
        observationJob?.cancel()
        observationJob = null
        proximitySensor.tearDown()
    }

    private fun startObserving() {
        observationJob?.cancel()
        observationJob = scope.launch {
            combine(callManager.displayState, callManager.audioState) { display, audio ->
                val primary = display.primary
                if (primary != null && audio != null) {
                    proximitySensor.updateProximitySensorMode(primary.state, audio.route)
                } else {
                    proximitySensor.tearDown()
                }
            }.collect {}
        }
    }
}
