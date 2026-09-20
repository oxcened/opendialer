package dev.alenajam.opendialer.data.contacts

import org.junit.Assert.assertEquals
import org.junit.Test

class DialerContactSummaryTest {
    @Test
    fun `mapList removes duplicate contact ids`() {
        val summaries = DialerContactSummary.mapList(
            listOf(
                DialerContactSummaryEntity(id = 42, name = "First name", starred = 0, photoUri = null),
                DialerContactSummaryEntity(id = 42, name = "Duplicate name", starred = 1, photoUri = "photo"),
                DialerContactSummaryEntity(id = 43, name = "Another contact", starred = 0, photoUri = null),
            ),
        )

        assertEquals(listOf(42, 43), summaries.map { it.id })
        assertEquals("First name", summaries.first().name)
    }
}
