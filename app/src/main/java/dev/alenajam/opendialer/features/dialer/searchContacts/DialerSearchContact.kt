package dev.alenajam.opendialer.features.dialer.searchContacts

class DialerSearchContact(
  val id: Int,
  val name: String,
  val label: String?,
  val contactId: Int,
  val number: String,
  val image: String?
) {
  companion object {
    fun mapList(list: List<DialerSearchContactEntity>): List<DialerSearchContact> {
      return list.map { map(it) }
    }

    fun map(contact: DialerSearchContactEntity): DialerSearchContact {
      return DialerSearchContact(
        id = contact.id,
        name = contact.name,
        image = contact.photoUri,
        number = contact.number,
        contactId = contact.contactId,
        label = contact.label
      )
    }
  }
}