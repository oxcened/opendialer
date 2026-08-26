package dev.alenajam.opendialer.data.contacts

import android.app.Application
import android.content.ContentValues
import android.database.ContentObserver
import android.provider.ContactsContract
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class ContactsRepositoryImpl
@Inject constructor(private val app: Application) : ContactsRepository {
    override fun getContacts(): Flow<List<DialerContactEntity>> =
        callbackFlow {
            val observer = object : ContentObserver(null) {
                override fun onChange(selfChange: Boolean) {
                    ContactsData.getCursor(app.contentResolver)?.use {
                        trySend(ContactsData.getData(it))
                    }
                }
            }

            app.contentResolver.registerContentObserver(ContactsData.URI, true, observer)
            app.contentResolver.registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, observer)

            ContactsData.getCursor(app.contentResolver)?.use {
                trySend(ContactsData.getData(it))
            }

            awaitClose {
                app.contentResolver.unregisterContentObserver(observer)
            }
        }

    override suspend fun toggleFavorite(contactId: Int, isFavorite: Boolean) {
        val values = ContentValues().apply {
            put(ContactsContract.Contacts.STARRED, if (isFavorite) 1 else 0)
        }
        app.contentResolver.update(
            ContactsContract.Contacts.CONTENT_URI,
            values,
            "${ContactsContract.Contacts._ID} = ?",
            arrayOf(contactId.toString())
        )
    }
}