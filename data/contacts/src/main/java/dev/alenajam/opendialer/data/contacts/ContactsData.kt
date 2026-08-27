package dev.alenajam.opendialer.data.contacts

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract

abstract class ContactsData {
    companion object {
        val URI: Uri = ContactsContract.Contacts.CONTENT_URI

        private val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.STARRED,
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
        )

        private const val where =
            "${ContactsContract.Contacts.DISPLAY_NAME} IS NOT NULL"
        private const val sort =
            "${ContactsContract.Contacts.STARRED} DESC, ${ContactsContract.Contacts.SORT_KEY_PRIMARY}"

        fun getCursor(contentResolver: ContentResolver): Cursor? = contentResolver.query(
            URI,
            projection,
            where,
            null,
            sort
        )

        fun getData(cursor: Cursor): List<DialerContactSummaryEntity> {
            val list = mutableListOf<DialerContactSummaryEntity>()
            if (cursor.moveToFirst()) {
                do {
                    list.add(
                        DialerContactSummaryEntity(
                            id = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID)),
                            name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)),
                            photoUri = cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                    ContactsContract.Contacts.PHOTO_THUMBNAIL_URI
                                )
                            )
                                ?.takeIf { it.isNotBlank() },
                            starred = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.STARRED)),
                        )
                    )
                } while (cursor.moveToNext())
            }
            return list
        }
    }
}
