package dev.alenajam.opendialer.core.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalInCallScreen = staticCompositionLocalOf<@Composable () -> Unit> {
    { /* Default empty content */ }
}
