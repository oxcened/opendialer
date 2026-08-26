package dev.alenajam.opendialer.data.calls

import android.database.Cursor
import android.provider.CallLog.Calls
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class CallsDataMappingTest {

    @Test
    fun `map cursor to DialerCallEntity`() {
        val cursor = mock<Cursor> {
            on { moveToFirst() } doReturn true
            on { moveToNext() } doReturn false
            on { getColumnIndexOrThrow(Calls._ID) } doReturn 0
            on { getColumnIndexOrThrow(Calls.NUMBER) } doReturn 1
            on { getColumnIndexOrThrow(Calls.CACHED_NAME) } doReturn 2
            on { getColumnIndexOrThrow(Calls.DATE) } doReturn 3
            on { getColumnIndexOrThrow(Calls.DURATION) } doReturn 4
            on { getColumnIndexOrThrow(Calls.TYPE) } doReturn 5
            on { getColumnIndexOrThrow(Calls.NEW) } doReturn 6
            on { getColumnIndexOrThrow(Calls.CACHED_PHOTO_URI) } doReturn 7
            on { getColumnIndexOrThrow(Calls.COUNTRY_ISO) } doReturn 8
            on { getColumnIndexOrThrow(Calls.CACHED_NUMBER_LABEL) } doReturn 9
            on { getColumnIndexOrThrow(Calls.CACHED_PHOTO_ID) } doReturn 10
            on { getColumnIndexOrThrow(Calls.GEOCODED_LOCATION) } doReturn 11
            on { getColumnIndexOrThrow(Calls.CACHED_FORMATTED_NUMBER) } doReturn 12
            on { getColumnIndexOrThrow(Calls.CACHED_NORMALIZED_NUMBER) } doReturn 13
            on { getColumnIndexOrThrow(Calls.CACHED_LOOKUP_URI) } doReturn 14
            on { getColumnIndexOrThrow(Calls.POST_DIAL_DIGITS) } doReturn 15
            on { getColumnIndexOrThrow(Calls.CACHED_MATCHED_NUMBER) } doReturn 16
            on { getColumnIndexOrThrow(Calls.CACHED_NUMBER_TYPE) } doReturn 17

            on { getInt(0) } doReturn 1
            on { getString(1) } doReturn "123456789"
            on { getString(2) } doReturn "John Doe"
            on { getLong(3) } doReturn 1000L
            on { getLong(4) } doReturn 60L
            on { getInt(5) } doReturn Calls.INCOMING_TYPE
            on { getInt(6) } doReturn 1
            on { getString(7) } doReturn "content://photo"
            on { getString(8) } doReturn "US"
            on { getString(9) } doReturn "Home"
            on { getLong(10) } doReturn 10L
            on { getString(11) } doReturn "New York"
            on { getString(12) } doReturn "(123) 456-789"
            on { getString(13) } doReturn "+1123456789"
            on { getString(14) } doReturn "content://lookup"
            on { getString(15) } doReturn "123"
            on { getString(16) } doReturn "123456789"
            on { getInt(17) } doReturn 1
        }

        val result = CallsData.getData(cursor)

        assertEquals(1, result.size)
        val entity = result[0]
        assertEquals(1, entity.id)
        assertEquals("123456789", entity.number)
        assertEquals("John Doe", entity.name)
        assertEquals(1000L, entity.date)
        assertEquals(Calls.INCOMING_TYPE, entity.type)
        assertEquals("content://photo", entity.photoUri)
    }
}
