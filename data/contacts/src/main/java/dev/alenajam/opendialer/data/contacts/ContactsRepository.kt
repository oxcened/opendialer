package dev.alenajam.opendialer.data.contacts

import kotlinx.coroutines.flow.Flow

interface ContactsRepository {
    fun getContacts(): Flow<List<DialerContactEntity>>
}