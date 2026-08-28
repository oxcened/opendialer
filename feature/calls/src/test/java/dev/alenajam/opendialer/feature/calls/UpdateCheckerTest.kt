package dev.alenajam.opendialer.feature.calls

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UpdateCheckerTest {
    @Test
    fun `parses release tags and debug version suffixes`() {
        assertEquals(SemanticVersion(1, 2, 3), "v1.2.3".toSemanticVersionOrNull())
        assertEquals(SemanticVersion(1, 2, 3), "1.2.3-debug".toSemanticVersionOrNull(allowSuffix = true))
    }

    @Test
    fun `rejects malformed release tags`() {
        assertNull("v1.2".toSemanticVersionOrNull())
        assertNull("v1.02.3".toSemanticVersionOrNull())
        assertNull("v1.2.3-rc.1".toSemanticVersionOrNull())
    }

    @Test
    fun `compares semantic versions by each component`() {
        assertEquals(true, SemanticVersion(1, 10, 0) > SemanticVersion(1, 9, 9))
    }
}
