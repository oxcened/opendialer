package dev.alenajam.opendialer.core.common.telecom

import android.telecom.PhoneAccountHandle

data class CallAccount(
    val handle: PhoneAccountHandle,
    val label: String,
    val number: String?,
    val iconTint: Int?,
)

sealed interface CallPlacementResult {
    data object Placed : CallPlacementResult
    data object PermissionRequired : CallPlacementResult
    data object Unavailable : CallPlacementResult
    data class AccountSelectionRequired(val accounts: List<CallAccount>) : CallPlacementResult
}

interface CallPlacementRepository {
    fun placeCall(number: String, account: CallAccount? = null): CallPlacementResult

    fun placeVoicemailCall(account: CallAccount? = null): CallPlacementResult
}
