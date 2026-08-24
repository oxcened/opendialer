package dev.alenajam.opendialer.feature.inCall.service

import android.annotation.SuppressLint
import android.content.Context
import android.os.PowerManager
import android.telecom.Call
import android.telecom.CallAudioState
import android.util.Log

class ProximitySensor(context: Context) {
    companion object {
        private val TAG = ProximitySensor::class.java.simpleName
    }

    private val proximityWakeLock: PowerManager.WakeLock? = run {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (powerManager.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)) {
            powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, TAG)
        } else {
            Log.i(TAG, "Device does not support proximity wake lock.")
            null
        }
    }

    fun updateProximitySensorMode(state: Int, audioRoute: Int) {
        val on = when (state) {
            Call.STATE_CONNECTING,
            Call.STATE_DIALING,
            Call.STATE_ACTIVE -> audioRoute == CallAudioState.ROUTE_EARPIECE
            else -> false
        }

        if (on) turnOnProximitySensor()
        else turnOffProximitySensor(true)
    }

    @SuppressLint("WakelockTimeout")
    private fun turnOnProximitySensor() {
        proximityWakeLock?.let {
            if (!it.isHeld) {
                it.acquire()
            }
        }
    }

    private fun turnOffProximitySensor(screenOnImmediately: Boolean) {
        proximityWakeLock?.let {
            if (it.isHeld) {
                val flags = if (screenOnImmediately) 0 else PowerManager.RELEASE_FLAG_WAIT_FOR_NO_PROXIMITY
                it.release(flags)
            }
        }
    }

    fun tearDown() {
        turnOffProximitySensor(true)
    }
}
