package dev.alenajam.opendialer.core.common.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class ContactAvatarColorKeyTest {
    @Test
    fun usesNameWhenAvailable() {
        assertEquals("Ada Lovelace", contactAvatarColorKey(" Ada Lovelace ", "+39 555 0100"))
    }

    @Test
    fun usesNumberWhenNameIsMissingOrRepeatsTheNumber() {
        assertEquals("+39 555 0100", contactAvatarColorKey(null, " +39 555 0100 "))
        assertEquals("+39 555 0100", contactAvatarColorKey("+39 555 0100", "+39 555 0100"))
    }
}
