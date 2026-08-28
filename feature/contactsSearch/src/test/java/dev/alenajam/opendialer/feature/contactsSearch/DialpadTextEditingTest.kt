package dev.alenajam.opendialer.feature.contactsSearch

import androidx.compose.ui.text.TextRange
import org.junit.Assert.assertEquals
import org.junit.Test

class DialpadTextEditingTest {
    @Test
    fun `replacement clamps stale selection to current text`() {
        val result = replaceDialpadSelection("1", TextRange(0, 2), "2")

        assertEquals("2", result.text)
        assertEquals(TextRange(1), result.selection)
    }

    @Test
    fun `replacement clamps a stale cursor to the end of current text`() {
        val result = replaceDialpadSelection("1", TextRange(2), "2")

        assertEquals("12", result.text)
        assertEquals(TextRange(2), result.selection)
    }

    @Test
    fun `backspace clamps stale selection to current text`() {
        val result = deleteDialpadSelection("1", TextRange(0, 2))

        assertEquals("", result.text)
        assertEquals(TextRange.Zero, result.selection)
    }
}
