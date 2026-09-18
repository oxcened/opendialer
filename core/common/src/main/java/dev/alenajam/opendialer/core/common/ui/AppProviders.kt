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
    val darkTheme = rememberAppIsDarkTheme()

    CompositionLocalProvider(
        LocalAppIcons provides icons,
        LocalAppThemeExtension provides themeExtension,
        LocalInCallScreen provides inCallScreen
    ) {
        AppTheme(darkTheme = darkTheme) {
            content()
        }
    }
}

@Composable
fun rememberAppIsDarkTheme(): Boolean {
    return false
}
