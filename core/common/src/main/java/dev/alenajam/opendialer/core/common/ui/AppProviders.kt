package dev.alenajam.opendialer.core.common.ui

import androidx.compose.runtime.Composable

@Composable
fun AppProviders(
    content: @Composable () -> Unit
) {
    AppTheme { content() }
}