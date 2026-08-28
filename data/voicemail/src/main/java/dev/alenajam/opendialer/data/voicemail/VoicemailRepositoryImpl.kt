package dev.alenajam.opendialer.data.voicemail

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.provider.VoicemailContract
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoicemailRepositoryImpl @Inject constructor(
    private val app: Application,
) : VoicemailRepository {
    override fun getVoicemails(): Flow<VoicemailRepositoryState> = callbackFlow {
        if (app.checkSelfPermission(Manifest.permission.READ_VOICEMAIL) != PackageManager.PERMISSION_GRANTED) {
            trySend(VoicemailRepositoryState.Unavailable)
            close()
            return@callbackFlow
        }

        fun loadVoicemails() {
            try {
                VoicemailData.getCursor(app.contentResolver)?.use { cursor ->
                    trySend(VoicemailRepositoryState.Available(VoicemailData.getData(cursor)))
                } ?: trySend(VoicemailRepositoryState.Unavailable)
            } catch (_: SecurityException) {
                trySend(VoicemailRepositoryState.Unavailable)
            }
        }

        val observer = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) = loadVoicemails()
        }

        try {
            app.contentResolver.registerContentObserver(VoicemailData.uri, true, observer)
        } catch (_: SecurityException) {
            trySend(VoicemailRepositoryState.Unavailable)
            close()
            return@callbackFlow
        }
        loadVoicemails()
        awaitClose { app.contentResolver.unregisterContentObserver(observer) }
    }.flowOn(Dispatchers.IO)

    override suspend fun requestContent(voicemail: Voicemail) {
        try {
            app.sendBroadcast(Intent(VoicemailContract.ACTION_FETCH_VOICEMAIL, voicemail.uri))
        } catch (_: SecurityException) {
            // The default-dialer role can be revoked while the screen is open.
        }
    }

    override suspend fun markRead(voicemail: Voicemail) {
        try {
            VoicemailData.markRead(app.contentResolver, voicemail.uri)
        } catch (_: SecurityException) {
            // The default-dialer role can be revoked while the screen is open.
        }
    }
}
