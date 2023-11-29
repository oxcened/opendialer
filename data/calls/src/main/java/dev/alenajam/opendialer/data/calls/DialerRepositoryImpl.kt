package dev.alenajam.opendialer.data.calls

import android.content.ContentResolver
import android.database.ContentObserver
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class DialerRepositoryImpl
@Inject constructor() : DialerRepository {
  override fun getCalls(contentResolver: ContentResolver): Flow<List<DialerCallEntity>> =
    callbackFlow {
      val observer = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) {
          CallsData.getCursor(contentResolver)?.let {
            trySend(CallsData.getData(it))
            it.close()
          }
        }
      }

      contentResolver.registerContentObserver(CallsData.URI, true, observer)

      CallsData.getCursor(contentResolver)?.let {
        trySend(CallsData.getData(it))
        it.close()
      }

      awaitClose {
        contentResolver.unregisterContentObserver(observer)
      }
    }
}