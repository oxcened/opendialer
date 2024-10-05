package dev.alenajam.opendialer.data.contacts

import android.content.ContentResolver
import kotlinx.coroutines.flow.Flow

interface ContactsRepository {
  fun getContacts(): Flow<List<DialerContactEntity>>
}