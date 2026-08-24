package dev.alenajam.opendialer.feature.inCall.service

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.alenajam.opendialer.core.common.Contact
import dev.alenajam.opendialer.core.common.ContactsHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallContactResolver @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "CallContactResolver"
    }

    fun interface Callback {
        fun onResolved(contact: Contact?)
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun resolve(phoneNumber: String, callback: Callback) {
        scope.launch {
            val contact = withContext(Dispatchers.IO) {
                try {
                    ContactsHelper.getContactByPhoneNumber(context, phoneNumber)
                } catch (exception: RuntimeException) {
                    Log.w(TAG, "Unable to resolve call contact", exception)
                    null
                }
            }
            callback.onResolved(contact)
        }
    }
}
