package dev.alenajam.opendialer.core.common.ui

/**
 * Returns the shared color identity for a contact avatar.
 *
 * A resolved contact name is available in the call log, in-call UI, and contacts list. Phone
 * numbers keep unknown callers distinct until a name is available.
 */
fun contactAvatarColorKey(name: String?, number: String? = null): String =
    name?.trim()?.takeIf { it.isNotEmpty() && it != number?.trim() }
        ?: number?.trim().orEmpty()
