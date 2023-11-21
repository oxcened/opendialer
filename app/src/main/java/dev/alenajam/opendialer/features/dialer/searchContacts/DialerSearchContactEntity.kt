package dev.alenajam.opendialer.features.dialer.searchContacts

class DialerSearchContactEntity(
  val id: Int,
  val name: String,
  val label: String?,
  val contactId: Int,
  val number: String,
  val photoUri: String?
)