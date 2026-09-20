package dev.alenajam.opendialer.data.contacts

class DialerContactSummary(
    val id: Int,
    val name: String,
    val starred: Boolean,
    val image: String?,
) {
    companion object {
        fun mapList(list: List<DialerContactSummaryEntity>): List<DialerContactSummary> =
            list.distinctBy { it.id }.map { contact ->
                DialerContactSummary(
                    id = contact.id,
                    name = contact.name,
                    starred = contact.starred == 1,
                    image = contact.photoUri,
                )
            }
    }
}
