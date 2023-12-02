package dev.alenajam.opendialer.data.contacts

import android.content.ContentResolver
import kotlinx.coroutines.flow.Flow

interface DialerRepository {
  fun getContacts(contentResolver: ContentResolver): Flow<List<DialerContactEntity>>
}