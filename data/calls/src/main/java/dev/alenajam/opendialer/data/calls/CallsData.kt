package dev.alenajam.opendialer.data.calls

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.CallLog.Calls
import android.telephony.PhoneNumberUtils
import android.util.Log

object CallsData {
    private val TAG = CallsData::class.simpleName

    /**
     * Includes carrier-provided visual voicemail when the app holds the default dialer
     * voicemail role. Other installations continue to read the regular call log.
     */
    fun getUri(context: Context): Uri = if (
        context.checkSelfPermission(Manifest.permission.READ_VOICEMAIL) == PackageManager.PERMISSION_GRANTED
    ) {
        Calls.CONTENT_URI_WITH_VOICEMAIL
    } else {
        Calls.CONTENT_URI
    }

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

    /** Filter out blocked calls and non-video Duo calls. */
    private const val where = """
            ${Calls.TYPE} != ${Calls.BLOCKED_TYPE}
            AND (
                ${Calls.PHONE_ACCOUNT_COMPONENT_NAME} IS NULL
                OR ${Calls.PHONE_ACCOUNT_COMPONENT_NAME} NOT LIKE 'com.google.android.apps.tachyon%'
                OR ${Calls.FEATURES} & ${Calls.FEATURES_VIDEO} == ${Calls.FEATURES_VIDEO}
            )
        """

    fun getCursor(contentResolver: ContentResolver, uri: Uri): Cursor? = contentResolver.query(
        uri
            .buildUpon()
            .appendQueryParameter(Calls.LIMIT_PARAM_KEY, LIMIT.toString())
            .build(),
        projection,
        where,
        null,
        Calls.DEFAULT_SORT_ORDER
    )

    fun getData(
        cursor: Cursor,
        voicemailNumbers: Set<String> = emptySet(),
        contactPhoneTypes: ContactPhoneTypes = ContactPhoneTypes.Empty,
    ): List<DialerCallEntity> {
        val start = System.currentTimeMillis()

        val list = mutableListOf<DialerCallEntity>()
        if (cursor.moveToFirst()) {
            do {
                val number = cursor.getString(cursor.getColumnIndexOrThrow(Calls.NUMBER))
                val type = cursor.getInt(cursor.getColumnIndexOrThrow(Calls.TYPE))
                val phoneType = contactPhoneTypes.findForNumber(number)
                list.add(
                    DialerCallEntity(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(Calls._ID)),
                        number = number,
                        name = cursor.getString(cursor.getColumnIndexOrThrow(Calls.CACHED_NAME)),
                        date = cursor.getLong(cursor.getColumnIndexOrThrow(Calls.DATE)),
                        duration = cursor.getLong(cursor.getColumnIndexOrThrow(Calls.DURATION)),
                        type = type,
                        isNew = cursor.getInt(cursor.getColumnIndexOrThrow(Calls.NEW)),
                        photoUri = cursor.getString(cursor.getColumnIndexOrThrow(Calls.CACHED_PHOTO_URI))
                            ?.takeIf { it.isNotBlank() },
                        countryIso = cursor.getString(cursor.getColumnIndexOrThrow(Calls.COUNTRY_ISO)),
                        label = phoneType?.label
                            ?: cursor.getString(cursor.getColumnIndexOrThrow(Calls.CACHED_NUMBER_LABEL)),
                        photoId = cursor.getLong(cursor.getColumnIndexOrThrow(Calls.CACHED_PHOTO_ID)),
                        geoDescription = cursor.getString(cursor.getColumnIndexOrThrow(Calls.GEOCODED_LOCATION)),
                        formattedNumber = cursor.getString(cursor.getColumnIndexOrThrow(Calls.CACHED_FORMATTED_NUMBER)),
                        normalizedNumber = cursor.getString(cursor.getColumnIndexOrThrow(Calls.CACHED_NORMALIZED_NUMBER)),
                        lookupUri = cursor.getString(cursor.getColumnIndexOrThrow(Calls.CACHED_LOOKUP_URI)),
                        postDialDigits = cursor.getString(cursor.getColumnIndexOrThrow(Calls.POST_DIAL_DIGITS)),
                        matchedNumber = cursor.getString(cursor.getColumnIndexOrThrow(Calls.CACHED_MATCHED_NUMBER)),
                        numberType = phoneType?.type
                            ?: cursor.getInt(cursor.getColumnIndexOrThrow(Calls.CACHED_NUMBER_TYPE)),
                        isVoicemailNumber = type == Calls.OUTGOING_TYPE && voicemailNumbers.any {
                            PhoneNumberUtils.compare(number, it)
                        },
                    )
                )
            } while (cursor.moveToNext())
        }

        val time = (System.currentTimeMillis() - start) / 1000f
        Log.d(TAG, "Call log query time: $time seconds")
        return list
    }

    fun delete(contentResolver: ContentResolver, id: Int): Int {
        return contentResolver.delete(
            Calls.CONTENT_URI,
            "${Calls._ID} = ?",
            arrayOf(id.toString())
        )
    }
}
