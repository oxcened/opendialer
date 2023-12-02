package dev.alenajam.opendialer.data.calls

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.CallLog.Calls
import android.telephony.PhoneNumberUtils
import android.util.Log

abstract class CallsData {
  companion object {
    private val TAG = CallsData::class.simpleName
    val URI: Uri = Calls.CONTENT_URI
    private const val LIMIT = 1000

    private val projection = arrayOf(
      Calls._ID,
      Calls.NUMBER,
      Calls.CACHED_NORMALIZED_NUMBER,
      Calls.CACHED_NAME,
      Calls.DATE,
      Calls.DURATION,
      Calls.TYPE,
      Calls.NEW,
      Calls.CACHED_PHOTO_URI,
      Calls.COUNTRY_ISO,
      Calls.CACHED_NUMBER_LABEL,
      Calls.CACHED_PHOTO_ID,
      Calls.GEOCODED_LOCATION,
      Calls.CACHED_FORMATTED_NUMBER,
      Calls.CACHED_NORMALIZED_NUMBER,
      Calls.CACHED_LOOKUP_URI,
      Calls.POST_DIAL_DIGITS,
      Calls.CACHED_MATCHED_NUMBER,
      Calls.CACHED_NUMBER_TYPE
    )

    /** Filter out:
     *  - Blocked calls
     *  - Non-video Duo calls
     */
    private const val where = """
            ${Calls.TYPE} != ${Calls.BLOCKED_TYPE}
            AND (
                ${Calls.PHONE_ACCOUNT_COMPONENT_NAME} IS NULL
                OR ${Calls.PHONE_ACCOUNT_COMPONENT_NAME} NOT LIKE 'com.google.android.apps.tachyon%'
                OR ${Calls.FEATURES} & ${Calls.FEATURES_VIDEO} == ${Calls.FEATURES_VIDEO}
            )
        """

    fun getCursor(contentResolver: ContentResolver): Cursor? = contentResolver.query(
      URI
        .buildUpon()
        .appendQueryParameter(Calls.LIMIT_PARAM_KEY, LIMIT.toString())
        .build(),
      projection,
      where,
      null,
      Calls.DEFAULT_SORT_ORDER
    )

    fun getData(cursor: Cursor): List<DialerCallEntity> {
      val start = System.currentTimeMillis()

      val list = mutableListOf<DialerCallEntity>()
      if (cursor.moveToFirst()) {
        do {
          list.add(
            DialerCallEntity(
              id = cursor.getInt(cursor.getColumnIndexOrThrow(Calls._ID)),
              number = cursor.getString(cursor.getColumnIndexOrThrow(Calls.NUMBER)),
              name = cursor.getString(cursor.getColumnIndexOrThrow(Calls.CACHED_NAME)),
              date = cursor.getLong(cursor.getColumnIndexOrThrow(Calls.DATE)),
              duration = cursor.getLong(cursor.getColumnIndexOrThrow(Calls.DURATION)),
              type = cursor.getInt(cursor.getColumnIndexOrThrow(Calls.TYPE)),
              isNew = cursor.getInt(cursor.getColumnIndexOrThrow(Calls.NEW)),
              photoUri = cursor.getString(cursor.getColumnIndexOrThrow(Calls.CACHED_PHOTO_URI))
                ?.takeIf { it.isNotBlank() },
              countryIso = cursor.getString(cursor.getColumnIndexOrThrow(Calls.COUNTRY_ISO)),
              label = cursor.getString(cursor.getColumnIndexOrThrow(Calls.CACHED_NUMBER_LABEL)),
              photoId = cursor.getLong(cursor.getColumnIndexOrThrow(Calls.CACHED_PHOTO_ID)),
              geoDescription = cursor.getString(cursor.getColumnIndexOrThrow(Calls.GEOCODED_LOCATION)),
              formattedNumber = cursor.getString(cursor.getColumnIndexOrThrow(Calls.CACHED_FORMATTED_NUMBER)),
              normalizedNumber = cursor.getString(cursor.getColumnIndexOrThrow(Calls.CACHED_NORMALIZED_NUMBER)),
              lookupUri = cursor.getString(cursor.getColumnIndexOrThrow(Calls.CACHED_LOOKUP_URI)),
              postDialDigits = cursor.getString(cursor.getColumnIndexOrThrow(Calls.POST_DIAL_DIGITS)),
              matchedNumber = cursor.getString(cursor.getColumnIndexOrThrow(Calls.CACHED_MATCHED_NUMBER)),
              numberType = cursor.getInt(cursor.getColumnIndexOrThrow(Calls.CACHED_NUMBER_TYPE))
            )
          )
        } while (cursor.moveToNext())
      }

      val time = (System.currentTimeMillis() - start) / 1000f
      Log.d(TAG, "Call log query time: $time seconds")
      return list
    }
  }
}