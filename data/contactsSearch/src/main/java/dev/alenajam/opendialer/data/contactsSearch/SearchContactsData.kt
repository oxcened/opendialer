package dev.alenajam.opendialer.data.contactsSearch

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract

abstract class SearchContactsData {
  companion object {
    private val URI: Uri = ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI

    private val projection = arrayOf(
      ContactsContract.CommonDataKinds.Phone._ID,
      ContactsContract.CommonDataKinds.Phone.LABEL,
      ContactsContract.CommonDataKinds.Phone.NUMBER,
      ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
      ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
      ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
    )

    private const val where =
      "${ContactsContract.CommonDataKinds.Phone.NUMBER} IS NOT NULL AND ${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} IS NOT NULL"

    fun getCursor(contentResolver: ContentResolver, query: String): Cursor? =
      contentResolver.query(
        URI.buildUpon().appendPath(query).build(),
        projection,
        where,
        null,
        ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY
      )

    fun getData(cursor: Cursor): List<DialerSearchContactEntity> {
      val list = mutableListOf<DialerSearchContactEntity>()
      if (cursor.moveToFirst()) {
        do {
          list.add(
            DialerSearchContactEntity(
              id = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)),
              name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)),
              photoUri = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI))
                ?.takeIf { it.isNotBlank() },
              label = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.LABEL)),
              contactId = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)),
              number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
            )
          )
        } while (cursor.moveToNext())
      }
      return list
    }
  }
}