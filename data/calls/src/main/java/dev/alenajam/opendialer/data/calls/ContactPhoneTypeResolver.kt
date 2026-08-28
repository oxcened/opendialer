package dev.alenajam.opendialer.data.calls

import android.content.ContentResolver
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.telephony.PhoneNumberUtils

data class ContactPhoneType(
    val number: String,
    val type: Int,
    val label: String?,
)

class ContactPhoneTypes private constructor(
    private val phones: List<ContactPhoneType>,
) {
    private val phonesByNormalizedNumber = phones
        .mapNotNull { phone ->
            PhoneNumberUtils.normalizeNumber(phone.number)
                .takeIf(String::isNotEmpty)
                ?.let { normalizedNumber -> normalizedNumber to phone }
        }
        .toMap()

    fun findForNumber(number: String?): ContactPhoneType? {
        if (number.isNullOrBlank()) return null

        val normalizedNumber = PhoneNumberUtils.normalizeNumber(number)
        return phonesByNormalizedNumber[normalizedNumber]
            ?: phones.firstOrNull { phone -> PhoneNumberUtils.compare(number, phone.number) }
    }

    companion object {
        val Empty = ContactPhoneTypes(emptyList())

        fun from(phones: List<ContactPhoneType>) = ContactPhoneTypes(phones)
    }
}

internal fun getContactPhoneTypes(contentResolver: ContentResolver): ContactPhoneTypes = try {
    contentResolver.query(
        Phone.CONTENT_URI,
        arrayOf(Phone.NUMBER, Phone.TYPE, Phone.LABEL),
        "${Phone.NUMBER} IS NOT NULL",
        null,
        "${Phone.IS_SUPER_PRIMARY} DESC, ${Phone.IS_PRIMARY} DESC",
    )?.use { cursor ->
        buildList {
            while (cursor.moveToNext()) {
                add(
                    ContactPhoneType(
                        number = cursor.getString(cursor.getColumnIndexOrThrow(Phone.NUMBER)),
                        type = cursor.getInt(cursor.getColumnIndexOrThrow(Phone.TYPE)),
                        label = cursor.getString(cursor.getColumnIndexOrThrow(Phone.LABEL)),
                    )
                )
            }
        }
    }.orEmpty().let(ContactPhoneTypes::from)
} catch (_: SecurityException) {
    ContactPhoneTypes.Empty
}
