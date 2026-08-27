package dev.alenajam.opendialer.data.contactsSearch

class DialerSearchContactEntity(
    val dataId: Long,
    val id: Int,
    val name: String,
    val phoneType: Int,
    val label: String?,
    val contactId: Int,
    val number: String,
    val photoUri: String?
)
