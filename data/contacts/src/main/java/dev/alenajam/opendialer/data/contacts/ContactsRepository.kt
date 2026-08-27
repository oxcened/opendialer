package dev.alenajam.opendialer.data.contacts

import kotlinx.coroutines.flow.Flow

interface ContactsRepository {
    fun getContacts(): Flow<List<DialerContactSummaryEntity>>
    fun getProfileContact(): Flow<DialerContactSummaryEntity?>
    fun getFavoriteContacts(): Flow<List<DialerContactEntity>>
    suspend fun toggleFavorite(contactId: Int, isFavorite: Boolean)
}
