package dev.alenajam.opendialer.data.contacts

class DialerContact(
    val dataId: Long,
    val id: Int,
    val name: String,
    val starred: Boolean,
    val image: String?,
    val number: String,
    val phoneType: Int,
    val phoneLabel: String?,
) {
    companion object {
        fun mapList(list: List<DialerContactEntity>): List<DialerContact> {
            return list.map { map(it) }
        }

        fun map(contact: DialerContactEntity): DialerContact {
            return DialerContact(
                dataId = contact.dataId,
                id = contact.id,
                name = contact.name,
                image = contact.photoUri,
                starred = contact.starred == 1,
                number = contact.number,
                phoneType = contact.phoneType,
                phoneLabel = contact.phoneLabel,
            )
        }
    }
}
