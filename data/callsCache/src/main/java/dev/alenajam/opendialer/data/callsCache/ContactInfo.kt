package dev.alenajam.opendialer.data.callsCache

import java.io.Serializable

data class ContactInfo(
    val name: String? = null,
    val number: String? = null,
    val photoUri: String? = null,
    var type: Int? = 0,
    val label: String? = null,
    val lookupUri: String? = null,
    val normalizedNumber: String? = null,
    val formattedNumber: String? = null,
    val geoDescription: String? = null,
    var photoId: Long? = 0
) : Serializable {
    companion object {
        val EMPTY = ContactInfo()
    }
}
