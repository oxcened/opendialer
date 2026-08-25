package dev.alenajam.opendialer.data.calls

import android.annotation.SuppressLint
import android.app.Application
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import dev.alenajam.opendialer.core.common.PermissionUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoicemailNumberProvider @Inject constructor(
    private val app: Application,
) {
    @SuppressLint("MissingPermission")
    fun getNumbers(): Set<String> {
        if (!PermissionUtils.hasMakeCallPermission(app)) return emptySet()

        val telephonyManager = app.getSystemService(TelephonyManager::class.java)
            ?: return emptySet()
        val numbers = linkedSetOf<String>()

        telephonyManager.voiceMailNumber
            ?.takeIf(String::isNotBlank)
            ?.let(numbers::add)

        val subscriptionManager = app.getSystemService(SubscriptionManager::class.java)
        val subscriptions = runCatching {
            subscriptionManager?.activeSubscriptionInfoList.orEmpty()
        }.getOrDefault(emptyList())

        subscriptions.forEach { subscription ->
            runCatching {
                telephonyManager.createForSubscriptionId(subscription.subscriptionId).voiceMailNumber
            }.getOrNull()
                ?.takeIf(String::isNotBlank)
                ?.let(numbers::add)
        }

        return numbers
    }
}
