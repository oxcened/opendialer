package dev.alenajam.opendialer.feature.inCall.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.alenajam.opendialer.core.common.Contact
import dev.alenajam.opendialer.core.common.ContactsHelper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
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

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun resolve(phoneNumber: String, callback: Callback) {
        executor.execute {
            var contact: Contact? = null
            try {
                contact = ContactsHelper.getContactByPhoneNumber(context, phoneNumber)
            } catch (exception: RuntimeException) {
                // Keep the phone number fallback when contact access is denied or
                // the provider becomes unavailable while a call is arriving.
                Log.w(TAG, "Unable to resolve call contact", exception)
            }
            val resolvedContact = contact
            mainHandler.post { callback.onResolved(resolvedContact) }
        }
    }
}
