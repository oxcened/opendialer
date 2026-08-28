package dev.alenajam.opendialer.data.contacts

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract

object ProfileContactData {
    val URI: Uri = ContactsContract.Profile.CONTENT_URI

    private val projection = arrayOf(
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.DISPLAY_NAME,
        ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
    )

    fun getCursor(contentResolver: ContentResolver): Cursor? = contentResolver.query(
        URI,
        projection,
        "${ContactsContract.Contacts.DISPLAY_NAME} IS NOT NULL",
        null,
        null,
    )

    fun getData(cursor: Cursor): List<DialerContactSummaryEntity> = buildList {
        if (cursor.moveToFirst()) {
            add(
                DialerContactSummaryEntity(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID)),
                    name = cursor.getString(
                        cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)
                    ),
                    starred = 0,
                    photoUri = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI
                        )
                    )?.takeIf { it.isNotBlank() },
                )
            )
        }
    }
}
