package dev.alenajam.opendialer.data.contacts

class DialerContact(
  val id: Int,
  val name: String,
  val starred: Boolean,
  val image: String?
) {
  companion object {
    fun mapList(list: List<DialerContactEntity>): List<DialerContact> {
      return list.map { map(it) }
    }

    fun map(contact: DialerContactEntity): DialerContact {
      return DialerContact(
        id = contact.id,
        name = contact.name,
        image = contact.photoUri,
        starred = contact.starred == 1
      )
    }
  }
}