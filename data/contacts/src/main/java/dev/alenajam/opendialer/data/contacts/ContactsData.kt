package dev.alenajam.opendialer.data.contacts

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract

object ContactsData {
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

    fun getNumbersCursor(contentResolver: ContentResolver, contactId: Int): Cursor? =
        contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId.toString()),
            null
        )

    fun getNumbersData(cursor: Cursor): List<String> {
        val numbers = mutableListOf<String>()
        while (cursor.moveToNext()) {
            cursor.getString(0)?.takeIf { it.isNotBlank() }?.let(numbers::add)
        }
        return numbers.distinct()
    }

    fun existsByIdCursor(contentResolver: ContentResolver, contactId: Int): Cursor? =
        contentResolver.query(
            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId.toLong()),
            arrayOf(ContactsContract.Contacts._ID),
            null,
            null,
            null
        )

    fun existsByNumberCursor(contentResolver: ContentResolver, number: String): Cursor? =
        contentResolver.query(
            Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number)),
            arrayOf(ContactsContract.PhoneLookup._ID),
            null,
            null,
            null
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

    fun updateFavorite(contentResolver: ContentResolver, contactId: Int, isFavorite: Boolean): Int {
        val values = ContentValues().apply {
            put(ContactsContract.Contacts.STARRED, if (isFavorite) 1 else 0)
        }
        return contentResolver.update(
            ContactsContract.Contacts.CONTENT_URI,
            values,
            "${ContactsContract.Contacts._ID} = ?",
            arrayOf(contactId.toString())
        )
    }
}
