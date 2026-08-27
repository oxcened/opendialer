package dev.alenajam.opendialer.data.contacts

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract

abstract class FavoriteContactsData {
    companion object {
        val URI: Uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI

        private val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.STARRED,
            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.LABEL,
        )

        private const val where =
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} IS NOT NULL AND " +
                "${ContactsContract.CommonDataKinds.Phone.NUMBER} IS NOT NULL AND " +
                "${ContactsContract.CommonDataKinds.Phone.STARRED} = 1"
        private const val sort = ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY

        fun getCursor(contentResolver: ContentResolver): Cursor? = contentResolver.query(
            URI,
            projection,
            where,
            null,
            sort,
        )

        fun getData(cursor: Cursor): List<DialerContactEntity> {
            val list = mutableListOf<DialerContactEntity>()
            if (cursor.moveToFirst()) {
                do {
                    list.add(
                        DialerContactEntity(
                            dataId = cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone._ID)),
                            id = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)),
                            name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)),
                            photoUri = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI))
                                ?.takeIf { it.isNotBlank() },
                            starred = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.STARRED)),
                            number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)),
                            phoneType = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE)),
                            phoneLabel = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.LABEL)),
                        )
                    )
                } while (cursor.moveToNext())
            }
            return list
        }
    }
}
