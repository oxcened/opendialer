package dev.alenajam.opendialer.data.calls

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telephony.SubscriptionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.alenajam.opendialer.core.common.PermissionUtils
import dev.alenajam.opendialer.core.common.telecom.CallAccount
import dev.alenajam.opendialer.core.common.telecom.CallPlacementRepository
import dev.alenajam.opendialer.core.common.telecom.CallPlacementResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallPlacementRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : CallPlacementRepository {
    @SuppressLint("MissingPermission")
    override fun placeCall(number: String, account: CallAccount?): CallPlacementResult =
        place(Uri.fromParts(PhoneAccount.SCHEME_TEL, number, null), account)

    @SuppressLint("MissingPermission")
    override fun placeVoicemailCall(account: CallAccount?): CallPlacementResult =
        place(Uri.fromParts("voicemail", "", null), account)

    @SuppressLint("MissingPermission")
    private fun place(address: Uri, selectedAccount: CallAccount?): CallPlacementResult {
        if (!PermissionUtils.hasMakeCallPermission(context)) {
            return CallPlacementResult.PermissionRequired
        }

        val telecomManager = context.getSystemService(TelecomManager::class.java)
            ?: return CallPlacementResult.Unavailable
        val account = selectedAccount?.handle
            ?: telecomManager.getDefaultOutgoingPhoneAccount(address.scheme)

        if (account != null) {
            telecomManager.placeCall(address, account.extras())
            return CallPlacementResult.Placed
        }

        val accounts = telecomManager.callCapablePhoneAccounts.mapIndexed { index, handle ->
            handle.toCallAccount(telecomManager, index)
        }
        if (accounts.isEmpty()) return CallPlacementResult.Unavailable
        if (accounts.size == 1) {
            telecomManager.placeCall(address, accounts.single().handle.extras())
            return CallPlacementResult.Placed
        }

        return CallPlacementResult.AccountSelectionRequired(accounts)
    }

    private fun PhoneAccountHandle.extras(): Bundle = Bundle().apply {
        putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, this@extras)
    }

    @SuppressLint("MissingPermission")
    private fun PhoneAccountHandle.toCallAccount(
        telecomManager: TelecomManager,
        index: Int,
    ): CallAccount {
        val phoneAccount = runCatching { telecomManager.getPhoneAccount(this) }.getOrNull()
        val subscription = context.getSystemService(SubscriptionManager::class.java)
            ?.activeSubscriptionInfoList
            ?.firstOrNull { it.subscriptionId.toString() == id }
        return CallAccount(
            handle = this,
            label = phoneAccount?.label?.toString()
                ?: subscription?.carrierName?.toString()
                ?: "SIM ${index + 1}",
            number = phoneAccount?.address?.schemeSpecificPart ?: subscription?.number,
            iconTint = subscription?.iconTint,
        )
    }
}
