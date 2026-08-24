package dev.alenajam.opendialer.feature.inCall.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import dev.alenajam.opendialer.core.common.Contact;
import dev.alenajam.opendialer.core.common.ContactsHelper;

@Singleton
public class CallContactResolver {
    private static final String TAG = "CallContactResolver";

    public interface Callback {
        void onResolved(@Nullable Contact contact);
    }

    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Inject
    public CallContactResolver(@ApplicationContext Context context) {
        this.context = context;
    }

    public void resolve(String phoneNumber, Callback callback) {
        executor.execute(() -> {
            Contact contact = null;
            try {
                contact = ContactsHelper.getContactByPhoneNumber(context, phoneNumber);
            } catch (RuntimeException exception) {
                // Keep the phone number fallback when contact access is denied or
                // the provider becomes unavailable while a call is arriving.
                Log.w(TAG, "Unable to resolve call contact", exception);
            }
            Contact resolvedContact = contact;
            mainHandler.post(() -> callback.onResolved(resolvedContact));
        });
    }
}
