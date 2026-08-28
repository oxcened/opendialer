package dev.alenajam.opendialer.data.contacts

data class DialerContactSummaryEntity(
    val id: Int,
    val name: String,
    val starred: Int,
    val photoUri: String?,
)
