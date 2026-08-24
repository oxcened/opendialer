package dev.alenajam.opendialer.core.common.ui

import java.util.Locale

data class ContactAvatarColors(
    val background: Long,
    val foreground: Long
)

fun contactAvatarColors(key: String): ContactAvatarColors {
    val palette = listOf(
        ContactAvatarColors(background = 0xFFE7E0FF, foreground = 0xFF42208A),
        ContactAvatarColors(background = 0xFFD9E2FF, foreground = 0xFF143A80),
        ContactAvatarColors(background = 0xFFD3F0E6, foreground = 0xFF00513F),
        ContactAvatarColors(background = 0xFFFFE3C5, foreground = 0xFF713A00),
        ContactAvatarColors(background = 0xFFFFD9E2, foreground = 0xFF7D1640),
    )
    val index = Math.floorMod(key.trim().lowercase(Locale.ROOT).hashCode(), palette.size)
    return palette[index]
}
