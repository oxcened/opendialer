package dev.alenajam.opendialer.feature.inCall.service

import android.content.Context
import android.telecom.Call
import android.telecom.DisconnectCause
import android.widget.Toast

object OngoingCallHelper {
    @JvmStatic
    fun handleDisconnectCause(context: Context, call: Call): Boolean {
        if (!isDisconnectedByError(call)) return false

        val cause = getDisconnectCauseDesc(call)
        val hasCause = !cause.isNullOrEmpty()
        if (hasCause) {
            Toast.makeText(context, cause, Toast.LENGTH_LONG).show()
        }
        return hasCause
    }

    @JvmStatic
    fun getDisconnectCauseDesc(call: Call): String? =
        call.details.disconnectCause.description?.toString()

    @JvmStatic
    fun isDisconnectedByError(call: Call): Boolean {
        val code = call.details.disconnectCause.code
        return code != DisconnectCause.LOCAL && code != DisconnectCause.REMOTE
    }
}
