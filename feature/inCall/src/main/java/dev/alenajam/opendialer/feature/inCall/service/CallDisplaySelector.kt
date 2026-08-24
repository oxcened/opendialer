package dev.alenajam.opendialer.feature.inCall.service

import android.telecom.Call

internal object CallDisplaySelector {
    private val PRIMARY_PRIORITY = intArrayOf(
        Call.STATE_RINGING,
        Call.STATE_DIALING,
        Call.STATE_CONNECTING,
        Call.STATE_ACTIVE,
        Call.STATE_HOLDING,
        Call.STATE_DISCONNECTING
    )
    private val SECONDARY_PRIORITY = intArrayOf(
        Call.STATE_ACTIVE,
        Call.STATE_HOLDING,
        Call.STATE_DISCONNECTING
    )

    fun <T> select(candidates: Collection<Candidate<T>>): Selection<T> {
        val ordered = candidates.sortedBy { it.sequence }

        val primary = firstMatching(ordered, PRIMARY_PRIORITY, null)
        val secondary = firstMatching(ordered, SECONDARY_PRIORITY, primary)
        return Selection(primary?.value, secondary?.value)
    }

    private fun <T> firstMatching(
        candidates: List<Candidate<T>>,
        priorities: IntArray,
        ignored: Candidate<T>?
    ): Candidate<T>? {
        for (state in priorities) {
            for (candidate in candidates) {
                if (candidate !== ignored && !candidate.isConferenced && candidate.state == state) {
                    return candidate
                }
            }
        }
        return null
    }

    class Candidate<T>(
        val value: T,
        val state: Int,
        val isConferenced: Boolean,
        val sequence: Long
    )

    class Selection<T>(
        val primary: T?,
        val secondary: T?
    )
}
