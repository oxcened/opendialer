package dev.alenajam.opendialer.features.dialer.contacts

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import dev.alenajam.opendialer.features.dialer.calls.cache.ContactInfo

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
              id = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)),
              name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)),
              photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI))
                ?.takeIf { it.isNotBlank() },
              starred = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.STARRED))
            )
          )
        } while (cursor.moveToNext())
      }
      return list
    }

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
      )?.let {
        if (it.moveToFirst()) {
          val lookupKey =
            it.getString(it.getColumnIndex(ContactsContract.PhoneLookup.LOOKUP_KEY))
          val contactId =
            it.getLong(it.getColumnIndex(ContactsContract.PhoneLookup.CONTACT_ID))
          return ContactInfo(
            name = it.getString(it.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)),
            number = it.getString(it.getColumnIndex(ContactsContract.PhoneLookup.NUMBER)),
            photoUri = it.getString(it.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_URI))
              ?.takeIf { uri -> uri.isNotBlank() },
            type = it.getInt(it.getColumnIndex(ContactsContract.PhoneLookup.TYPE)),
            label = it.getString(it.getColumnIndex(ContactsContract.PhoneLookup.LABEL)),
            lookupUri = dev.alenajam.opendialer.util.UriUtils.uriToString(
              ContactsContract.Contacts.getLookupUri(
                contactId,
                lookupKey
              )
            ),
            normalizedNumber = it.getString(it.getColumnIndex(ContactsContract.PhoneLookup.NORMALIZED_NUMBER)),
            photoId = it.getLong(it.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_ID))
          )
        }
        it.close()
      }

      return createEmptyContactInfoForNumber(context, number, countryIso)
    }

    private fun createEmptyContactInfoForNumber(
      context: Context,
      number: String,
      countryIso: String?
    ): ContactInfo {
      val formattedNumber: String =
        dev.alenajam.opendialer.features.dialer.calls.cache.ContactInfoHelper(context)
          .formatPhoneNumber(number, null, countryIso)
      val normalizedNumber = PhoneNumberUtils.formatNumberToE164(number, countryIso)
      return ContactInfo(
        number = number,
        lookupUri = dev.alenajam.opendialer.util.UriUtils.uriToString(
          dev.alenajam.opendialer.features.dialer.calls.cache.ContactInfoHelper.createTemporaryContactUri(
            formattedNumber
          )
        ),
        normalizedNumber = normalizedNumber,
        formattedNumber = formattedNumber
      )
    }
  }
}