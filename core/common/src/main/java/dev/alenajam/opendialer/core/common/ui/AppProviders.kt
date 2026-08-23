package dev.alenajam.opendialer.core.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun AppProviders(
    icons: AppIcons = DefaultAppIcons,
    themeExtension: AppThemeExtension = AppThemeExtension(),
    inCallScreen: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalAppIcons provides icons,
        LocalAppThemeExtension provides themeExtension,
        LocalInCallScreen provides inCallScreen
    ) {
        AppTheme {
            content()
        }
    }
}