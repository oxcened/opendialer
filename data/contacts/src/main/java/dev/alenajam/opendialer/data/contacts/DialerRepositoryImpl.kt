package dev.alenajam.opendialer.data.contacts

import android.content.ContentResolver
import android.database.ContentObserver
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class DialerRepositoryImpl
@Inject constructor() : DialerRepository {
  override fun getContacts(contentResolver: ContentResolver): Flow<List<DialerContactEntity>> =
    callbackFlow {
      val observer = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) {
          ContactsData.getCursor(contentResolver)?.let {
            trySend(ContactsData.getData(it))
            it.close()
          }
        }
      }

      contentResolver.registerContentObserver(ContactsData.URI, true, observer)

      ContactsData.getCursor(contentResolver)?.let {
        trySend(ContactsData.getData(it))
        it.close()
      }

      awaitClose {
        contentResolver.unregisterContentObserver(observer)
      }
    }
}