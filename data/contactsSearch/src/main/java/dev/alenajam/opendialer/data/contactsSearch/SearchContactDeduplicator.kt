package dev.alenajam.opendialer.data.contactsSearch

import android.telephony.PhoneNumberUtils

internal object SearchContactDeduplicator {
    fun deduplicate(contacts: List<DialerSearchContactEntity>): List<DialerSearchContactEntity> =
        contacts.distinctBy { contact ->
            contact.contactId to PhoneNumberUtils.normalizeNumber(contact.number)
        }
}
