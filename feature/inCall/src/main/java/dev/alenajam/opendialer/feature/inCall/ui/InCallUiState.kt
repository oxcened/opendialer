package dev.alenajam.opendialer.feature.inCall.ui

import android.telecom.Call
import dev.alenajam.opendialer.feature.inCall.service.OngoingCall

enum class CallStatus {
    IDLE,
    RINGING,
    CONNECTING,
    HOLDING,
    DIALING,
    DISCONNECTING,
    DISCONNECTED,
    ACTIVE;

    companion object {
        fun fromTelecomState(state: Int?): CallStatus = when (state) {
            Call.STATE_RINGING -> RINGING
            Call.STATE_CONNECTING -> CONNECTING
            Call.STATE_HOLDING -> HOLDING
            Call.STATE_DIALING -> DIALING
            Call.STATE_DISCONNECTING -> DISCONNECTING
            Call.STATE_DISCONNECTED -> DISCONNECTED
            Call.STATE_ACTIVE -> ACTIVE
            else -> IDLE
        }
    }
}

data class InCallUiState(
    val status: CallStatus = CallStatus.IDLE,
    val isHolding: Boolean = false,
    val isSpeaker: Boolean = false,
    val isMuted: Boolean = false,
    val callerName: String = "",
    val callerNumber: String = "",
    val callerNumberLabel: String = "",
    val callerImageUri: String? = null,
    val isIncoming: Boolean = false,
    val canHold: Boolean = false,
    val canMerge: Boolean = false,
    val canManageConference: Boolean = false,
    val canAddCall: Boolean = false,
    val hasSecondaryCall: Boolean = false,
    val secondaryCallerName: String? = null,
    val conferenceParticipants: List<ConferenceParticipantUiState> = emptyList()
)

data class ConferenceParticipantUiState(
    val call: OngoingCall,
    val callerName: String,
    val callerImageUri: String?,
    val status: CallStatus,
    val isConferenced: Boolean
)
