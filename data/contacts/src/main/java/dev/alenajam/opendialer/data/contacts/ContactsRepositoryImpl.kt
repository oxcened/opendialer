package dev.alenajam.opendialer.data.contacts

import android.app.Application
import android.content.ContentResolver
import android.database.ContentObserver
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
          ContactsData.getCursor(app.contentResolver)?.let {
            trySend(ContactsData.getData(it))
            it.close()
          }
        }
      }

      app.contentResolver.registerContentObserver(ContactsData.URI, true, observer)

      ContactsData.getCursor(app.contentResolver)?.let {
        trySend(ContactsData.getData(it))
        it.close()
      }

      awaitClose {
        app.contentResolver.unregisterContentObserver(observer)
      }
    }
}