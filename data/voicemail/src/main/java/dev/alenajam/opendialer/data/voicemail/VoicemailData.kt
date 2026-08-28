package dev.alenajam.opendialer.data.voicemail

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.provider.BaseColumns
import android.provider.VoicemailContract

object VoicemailData {
    val uri = VoicemailContract.Voicemails.CONTENT_URI

    private val projection = arrayOf(
        BaseColumns._ID,
        VoicemailContract.Voicemails.NUMBER,
        VoicemailContract.Voicemails.DATE,
        VoicemailContract.Voicemails.TRANSCRIPTION,
        VoicemailContract.Voicemails.HAS_CONTENT,
    )

    fun getCursor(contentResolver: ContentResolver): Cursor? = contentResolver.query(
        uri,
        projection,
        null,
        null,
        "${VoicemailContract.Voicemails.DATE} DESC",
    )

    fun getData(cursor: Cursor): List<Voicemail> = buildList {
        val idIndex = cursor.getColumnIndexOrThrow(BaseColumns._ID)
        val numberIndex = cursor.getColumnIndexOrThrow(VoicemailContract.Voicemails.NUMBER)
        val dateIndex = cursor.getColumnIndexOrThrow(VoicemailContract.Voicemails.DATE)
        val transcriptionIndex = cursor.getColumnIndexOrThrow(VoicemailContract.Voicemails.TRANSCRIPTION)
        val hasContentIndex = cursor.getColumnIndexOrThrow(VoicemailContract.Voicemails.HAS_CONTENT)
        while (cursor.moveToNext()) {
            val id = cursor.getLong(idIndex)
            add(
                Voicemail(
                    id = id,
                    uri = ContentUris.withAppendedId(uri, id),
                    number = cursor.getString(numberIndex),
                    date = cursor.getLong(dateIndex),
                    transcription = cursor.getString(transcriptionIndex),
                    hasContent = cursor.getInt(hasContentIndex) == 1,
                )
            )
        }
    }

    fun markRead(contentResolver: ContentResolver, voicemailUri: Uri): Int {
        val values = ContentValues().apply {
            put(VoicemailContract.Voicemails.IS_READ, 1)
        }
        return contentResolver.update(
            voicemailUri,
            values,
            null,
            null
        )
    }
}
