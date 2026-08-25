package dev.alenajam.opendialer.data.voicemail

import kotlinx.coroutines.flow.Flow

interface VoicemailRepository {
    fun getVoicemails(): Flow<VoicemailRepositoryState>
    suspend fun requestContent(voicemail: Voicemail)
    suspend fun markRead(voicemail: Voicemail)
}

sealed interface VoicemailRepositoryState {
    data object Unavailable : VoicemailRepositoryState
    data class Available(val voicemails: List<Voicemail>) : VoicemailRepositoryState
}
