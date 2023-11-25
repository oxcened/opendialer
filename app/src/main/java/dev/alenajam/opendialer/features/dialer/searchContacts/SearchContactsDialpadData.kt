package dev.alenajam.opendialer.features.dialer.searchContacts

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone

abstract class SearchContactsDialpadData {
  companion object {
    private val URI: Uri = Phone.CONTENT_URI
    private const val MAX_ENTRIES = 20

    private val projection = arrayOf(
      Phone._ID,
      Phone.LABEL,
      Phone.NUMBER,
      Phone.CONTACT_ID,
      Phone.DISPLAY_NAME,
      Phone.PHOTO_THUMBNAIL_URI,
      Phone.LOOKUP_KEY,
      Phone.IS_PRIMARY
    )

    private const val where =
      "${Phone.NUMBER} IS NOT NULL AND ${Phone.DISPLAY_NAME} IS NOT NULL"
    private const val sort = """
            ${Phone.STARRED} DESC,
            ${Phone.IS_SUPER_PRIMARY} DESC,
            ${Phone.DISPLAY_NAME_PRIMARY},
            ${Phone.IS_PRIMARY} DESC
        """

    fun getCursor(contentResolver: ContentResolver): Cursor? = contentResolver.query(
      URI
        .buildUpon()
        .appendQueryParameter(ContactsContract.REMOVE_DUPLICATE_ENTRIES, "true")
        .build(),
      projection,
      where,
      null,
      sort
    )

    fun getData(
      context: Context,
      cursor: Cursor,
      query: String
    ): List<DialerSearchContactEntity> {
      val nameMatcher = dev.alenajam.opendialer.util.smartDialUtils.SmartDialNameMatcher(query)
      val duplicates = HashSet<dev.alenajam.opendialer.util.smartDialUtils.ContactMatch>()
      val list = mutableListOf<DialerSearchContactEntity>()
      if (cursor.moveToFirst()) {
        do {
          val name = cursor.getString(cursor.getColumnIndexOrThrow(Phone.DISPLAY_NAME))
          val number = cursor.getString(cursor.getColumnIndexOrThrow(Phone.NUMBER))
          val lookupKey = cursor.getString(cursor.getColumnIndexOrThrow(Phone.LOOKUP_KEY))
          val contactId = cursor.getLong(cursor.getColumnIndexOrThrow(Phone.CONTACT_ID))

          val contactMatch =
            dev.alenajam.opendialer.util.smartDialUtils.ContactMatch(lookupKey, contactId)
          if (duplicates.contains(contactMatch)) {
            continue
          }

          val nameMatches = nameMatcher.matches(context, name)
          val numberMatches = nameMatcher.matchesNumber(context, number, query) != null

          if (nameMatches || numberMatches) {
            duplicates.add(contactMatch)
            list.add(
              DialerSearchContactEntity(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(Phone.CONTACT_ID)),
                name = name,
                photoUri = cursor.getString(cursor.getColumnIndexOrThrow(Phone.PHOTO_THUMBNAIL_URI))
                  ?.takeIf { it.isNotBlank() },
                label = cursor.getString(cursor.getColumnIndexOrThrow(Phone.LABEL)),
                contactId = contactId.toInt(),
                number = number
              )
            )
          }
        } while (cursor.moveToNext() && list.size < MAX_ENTRIES)
      }
      return list
    }
  }
}