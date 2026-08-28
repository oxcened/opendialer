package dev.alenajam.opendialer.data.callsCache

import android.content.Context
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import dev.alenajam.opendialer.core.aosp.UriUtils

object CacheData {
    fun getContactInfoByNumber(
        context: Context,
        number: String,
        countryIso: String?
    ): ContactInfo {
        val uri = ContactsContract.PhoneLookup.CONTENT_FILTER_URI

        val projection = arrayOf(
            ContactsContract.PhoneLookup.CONTACT_ID,
            ContactsContract.PhoneLookup.DISPLAY_NAME,
            ContactsContract.PhoneLookup.TYPE,
            ContactsContract.PhoneLookup.LABEL,
            ContactsContract.PhoneLookup.NUMBER,
            ContactsContract.PhoneLookup.NORMALIZED_NUMBER,
            ContactsContract.PhoneLookup.PHOTO_ID,
            ContactsContract.PhoneLookup.LOOKUP_KEY,
            ContactsContract.PhoneLookup.PHOTO_URI
        )

        context.contentResolver.query(
            uri
                .buildUpon()
                .appendPath(number)
                .build(),
            projection,
            null,
            null,
            null
        )?.use {
            if (it.moveToFirst()) {
                val lookupKey =
                    it.getString(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.LOOKUP_KEY))
                val contactId =
                    it.getLong(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.CONTACT_ID))
                return ContactInfo(
                    name = it.getString(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME)),
                    number = it.getString(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.NUMBER)),
                    photoUri = it.getString(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.PHOTO_URI))
                        ?.takeIf { uri -> uri.isNotBlank() },
                    type = it.getInt(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.TYPE)),
                    label = it.getString(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.LABEL)),
                    lookupUri = UriUtils.uriToString(
                        ContactsContract.Contacts.getLookupUri(
                            contactId,
                            lookupKey
                        )
                    ),
                    normalizedNumber = it.getString(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.NORMALIZED_NUMBER)),
                    photoId = it.getLong(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.PHOTO_ID))
                )
            }
        }

        return createEmptyContactInfoForNumber(context, number, countryIso)
    }

    private fun createEmptyContactInfoForNumber(
        context: Context,
        number: String,
        countryIso: String?
    ): ContactInfo {
        val formattedNumber: String =
            ContactInfoHelper(context).formatPhoneNumber(number, null, countryIso)
        val normalizedNumber = PhoneNumberUtils.formatNumberToE164(number, countryIso)
        return ContactInfo(
            number = number,
            lookupUri = UriUtils.uriToString(
                ContactInfoHelper.createTemporaryContactUri(
                    formattedNumber
                )
            ),
            normalizedNumber = normalizedNumber,
            formattedNumber = formattedNumber
        )
    }
}
