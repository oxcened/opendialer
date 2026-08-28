package dev.alenajam.opendialer.data.contacts

import android.app.Application
import android.database.ContentObserver
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ContactsRepositoryImpl
@Inject constructor(private val app: Application) : ContactsRepository {
    override fun getContacts(): Flow<List<DialerContactSummaryEntity>> =
        observeContacts(
            uri = ContactsData.URI,
            getCursor = ContactsData::getCursor,
            getData = ContactsData::getData,
        )

    override fun getFavoriteContacts(): Flow<List<DialerContactEntity>> =
        observeContacts(
            uri = FavoriteContactsData.URI,
            getCursor = FavoriteContactsData::getCursor,
            getData = FavoriteContactsData::getData,
        )

    override fun getProfileContact(): Flow<DialerContactSummaryEntity?> =
        observeContacts(
            uri = ProfileContactData.URI,
            getCursor = ProfileContactData::getCursor,
            getData = ProfileContactData::getData,
        ).map { it.firstOrNull() }

    private fun <T> observeContacts(
        uri: android.net.Uri,
        getCursor: (android.content.ContentResolver) -> android.database.Cursor?,
        getData: (android.database.Cursor) -> List<T>,
    ): Flow<List<T>> =
        callbackFlow {
            val observer = object : ContentObserver(null) {
                override fun onChange(selfChange: Boolean) {
                    getCursor(app.contentResolver)?.use {
                        trySend(getData(it))
                    }
                }
            }

            app.contentResolver.registerContentObserver(uri, true, observer)
            app.contentResolver.registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, observer)

            getCursor(app.contentResolver)?.use {
                trySend(getData(it))
            }

            awaitClose {
                app.contentResolver.unregisterContentObserver(observer)
            }
        }

    override suspend fun toggleFavorite(contactId: Int, isFavorite: Boolean) {
        ContactsData.updateFavorite(app.contentResolver, contactId, isFavorite)
    }

    override suspend fun getContactNumbers(contactId: Int): List<String> = withContext(Dispatchers.IO) {
        ContactsData.getNumbersCursor(app.contentResolver, contactId)?.use {
            ContactsData.getNumbersData(it)
        } ?: emptyList()
    }

    override suspend fun contactExists(
        contactId: Int?,
        contactKeys: List<String>
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            contactId?.let { id ->
                ContactsData.existsByIdCursor(app.contentResolver, id)?.use { it.moveToFirst() } ?: false
            } ?: contactKeys.any { number ->
                ContactsData.existsByNumberCursor(app.contentResolver, number)?.use { it.moveToFirst() } == true
            }
        } catch (_: SecurityException) {
            true
        }
    }
}
