package dev.alenajam.opendialer.core.common.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import dev.alenajam.opendialer.core.common.SharedPreferenceHelper

@Composable
fun AppProviders(
    icons: AppIcons = DefaultAppIcons,
    themeExtension: AppThemeExtension = AppThemeExtension(),
    inCallScreen: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    var themeMode by remember { mutableStateOf(SharedPreferenceHelper.getThemeMode(context)) }
    val preferences = remember(context) { SharedPreferenceHelper.getSharedPreferences(context) }
    val systemIsDark = isSystemInDarkTheme()

    DisposableEffect(preferences) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == SharedPreferenceHelper.KEY_SETTING_THEME) {
                themeMode = SharedPreferenceHelper.getThemeMode(context)
            }
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        onDispose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    val darkTheme = when (themeMode) {
        SharedPreferenceHelper.ThemeMode.LIGHT -> false
        SharedPreferenceHelper.ThemeMode.DARK -> true
        SharedPreferenceHelper.ThemeMode.SYSTEM -> systemIsDark
    }

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
