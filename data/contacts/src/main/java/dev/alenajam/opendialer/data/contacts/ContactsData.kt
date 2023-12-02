package dev.alenajam.opendialer.data.contacts

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract

abstract class ContactsData {
  companion object {
    val URI: Uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI

    private val projection = arrayOf(
      ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
      ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
      ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
      ContactsContract.CommonDataKinds.Phone.STARRED,
      ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
    )

    private const val where =
      "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} IS NOT NULL"
    private const val sort =
      "${ContactsContract.CommonDataKinds.Phone.STARRED} DESC, ${ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY}"

    fun getCursor(contentResolver: ContentResolver): Cursor? = contentResolver.query(
      URI,
      projection,
      where,
      null,
      sort
    )

    fun getData(cursor: Cursor): List<DialerContactEntity> {
      val list = mutableListOf<DialerContactEntity>()
      if (cursor.moveToFirst()) {
        do {
          list.add(
            DialerContactEntity(
              id = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)),
              name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)),
              photoUri = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI))
                ?.takeIf { it.isNotBlank() },
              starred = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.STARRED))
            )
          )
        } while (cursor.moveToNext())
      }
      return list
    }
  }
}