package dev.alenajam.opendialer.data.callsCache

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContactInfoRequestDeduplicatorTest {
    private val deduplicator = ContactInfoRequestDeduplicator()

    @Test
    fun `marks a number only once for the same country`() {
        assertTrue(deduplicator.markIfNew("6505551212", "US"))
        assertFalse(deduplicator.markIfNew("6505551212", "US"))
    }

    @Test
    fun `treats the same number in different countries as separate requests`() {
        assertTrue(deduplicator.markIfNew("02079460000", "GB"))
        assertTrue(deduplicator.markIfNew("02079460000", "IT"))
    }

    @Test
    fun `allows a number to be refreshed again after invalidation`() {
        assertTrue(deduplicator.markIfNew("6505551212", "US"))
        deduplicator.clear()

        assertTrue(deduplicator.markIfNew("6505551212", "US"))
    }

    @Test
    fun `allows a request to be retried after it is removed`() {
        assertTrue(deduplicator.markIfNew("6505551212", "US"))
        deduplicator.remove("6505551212", "US")

        assertTrue(deduplicator.markIfNew("6505551212", "US"))
    }
}
