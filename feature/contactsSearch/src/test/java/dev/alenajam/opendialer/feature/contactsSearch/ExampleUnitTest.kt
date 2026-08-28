package dev.alenajam.opendialer.feature.contactsSearch

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchMatchHighlighterTest {
    @Test
    fun `text search highlights only a name prefix`() {
        val matches = textPrefixMatchPositions("haaahaaa", "h")

        assertEquals(1, matches.size)
        assertEquals(0, matches.single().start)
        assertEquals(1, matches.single().end)
    }

    @Test
    fun `text search highlights a later word prefix`() {
        val matches = textPrefixMatchPositions("Ada Hopper", "ho")

        assertEquals(1, matches.size)
        assertEquals(4, matches.single().start)
        assertEquals(6, matches.single().end)
    }

    @Test
    fun `text search ignores an earlier match in the middle of a word`() {
        val matches = textPrefixMatchPositions("Chad Harry", "h")

        assertEquals(1, matches.size)
        assertEquals(5, matches.single().start)
        assertEquals(6, matches.single().end)
    }
}
