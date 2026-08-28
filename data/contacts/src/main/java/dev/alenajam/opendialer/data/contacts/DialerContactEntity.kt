package dev.alenajam.opendialer.data.contacts

data class DialerContactEntity(
    val dataId: Long,
    val id: Int,
    val name: String,
    val starred: Int,
    val photoUri: String?,
    val number: String,
    val phoneType: Int,
    val phoneLabel: String?,
)
