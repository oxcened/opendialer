package dev.alenajam.opendialer.feature.inCall.ui

import dev.alenajam.opendialer.feature.inCall.service.OngoingCall

data class InCallUiState(
    val stateLabel: String = "",
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
    val state: Int?,
    val isConferenced: Boolean
)
