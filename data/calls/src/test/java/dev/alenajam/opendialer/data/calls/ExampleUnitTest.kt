package dev.alenajam.opendialer.data.calls

import org.junit.Assert.assertTrue
import org.junit.Test

class DialerCallTest {
    @Test
    fun `mapping preserves voicemail number identity`() {
        val entity = DialerCallEntity(
            id = 1,
            number = "+15557654321",
            name = null,
            date = 0,
            duration = 0,
            type = CallType.OUTGOING.value,
            isNew = 0,
            photoUri = null,
            label = null,
            lookupUri = null,
            isVoicemailNumber = true,
        )

        val call = requireNotNull(DialerCall.map(entity))

        assertTrue(call.isVoicemailNumber)
    }
}
