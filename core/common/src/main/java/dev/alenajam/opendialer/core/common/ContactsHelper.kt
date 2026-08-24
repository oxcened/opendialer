package dev.alenajam.opendialer.core.common

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract

object ContactsHelper {
    private val projectionPhoneLookup = arrayOf(
        ContactsContract.PhoneLookup.CONTACT_ID,
        ContactsContract.PhoneLookup.DISPLAY_NAME,
        ContactsContract.PhoneLookup.TYPE,
        ContactsContract.PhoneLookup.LABEL,
        ContactsContract.PhoneLookup.NUMBER,
        ContactsContract.PhoneLookup.NORMALIZED_NUMBER,
        ContactsContract.PhoneLookup.PHOTO_ID,
        ContactsContract.PhoneLookup.LOOKUP_KEY,
        ContactsContract.PhoneLookup.PHOTO_URI,
        ContactsContract.PhoneLookup._ID
    )

    @JvmStatic
    fun getContactByPhoneNumber(context: Context, phoneNumber: String?): Contact? {
        if (!PermissionUtils.hasContactsPermission(context) || phoneNumber.isNullOrEmpty()) {
            return null
        }

        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        val cursor = context.contentResolver.query(
            uri, projectionPhoneLookup, null, null, null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val contact = Contact(
                    id = it.getInt(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID)),
                    name = it.getString(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME)),
                    number = phoneNumber,
                    imageUri = it.getString(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.PHOTO_URI))
                )
                contact.phoneType = it.getInt(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.TYPE))
                contact.phoneLabel = it.getString(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.LABEL))
                return contact
            }
        }
        return null
    }
}
