package dev.alenajam.opendialer.core.common

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SharedPreferenceHelper {
    const val SP_QUICK_RESPONSES = "SP_QUICK_RESPONSES"
    const val KEY_SETTING_THEME = "theme"
    const val KEY_SETTING_DEFAULT = "default"
    const val KEY_SETTING_SOUND_VIBRATION = "sound"
    const val KEY_SETTING_QUICK_RESPONSES = "quick_responses"
    const val KEY_SETTING_BLOCKED_NUMBERS = "blockedNumbers"
    const val KEY_SETTING_NOTIFICATION_SETTINGS = "notificationSettings"
    private const val KEY_CALL_LOG_FAVORITES_EXPANDED = "call_log_favorites_expanded"

    enum class ThemeMode(val nightMode: Int) {
        LIGHT(AppCompatDelegate.MODE_NIGHT_NO),
        DARK(AppCompatDelegate.MODE_NIGHT_YES),
        SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        companion object {
            fun from(nightMode: Int) = entries.firstOrNull { it.nightMode == nightMode } ?: SYSTEM
        }
    }

    @JvmStatic
    fun getSharedPreferences(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    @JvmStatic
    fun init(context: Context) {
        val sharedPreferences = getSharedPreferences(context)

        val theme = sharedPreferences.getString(KEY_SETTING_THEME, null)
        try {
            CommonUtils.setTheme(if (theme == null) ThemeMode.SYSTEM.nightMode else theme.toInt())
        } catch (e: NumberFormatException) {
            Log.d(SharedPreferenceHelper::class.java.simpleName, e.localizedMessage ?: "")
        }

        if (!sharedPreferences.contains(SP_QUICK_RESPONSES)) {
            val quickResponses = context.resources.getStringArray(R.array.array_quick_responses)
            val quickResponseList = quickResponses.toMutableList()
            sharedPreferences.edit().putString(SP_QUICK_RESPONSES, Gson().toJson(quickResponseList)).apply()
        }
    }

    fun getQuickResponses(context: Context): List<String> {
        val fallback = context.resources.getStringArray(R.array.array_quick_responses).toList()
        val json = getSharedPreferences(context).getString(SP_QUICK_RESPONSES, null)
            ?: return fallback

        return runCatching {
            val type = object : TypeToken<List<String>>() {}.type
            Gson().fromJson<List<String>>(json, type)
                ?.map(String::trim)
                ?.filter(String::isNotEmpty)
                ?.takeIf { it.isNotEmpty() }
                ?: fallback
        }.getOrDefault(fallback)
    }

    fun saveQuickResponses(context: Context, responses: List<String>) {
        val cleanedResponses = responses.map(String::trim).filter(String::isNotEmpty)
        getSharedPreferences(context)
            .edit()
            .putString(SP_QUICK_RESPONSES, Gson().toJson(cleanedResponses))
            .apply()
    }

    fun getThemeMode(context: Context): ThemeMode {
        val savedNightMode = getSharedPreferences(context)
            .getString(KEY_SETTING_THEME, null)
            ?.toIntOrNull()
        return ThemeMode.from(savedNightMode ?: ThemeMode.SYSTEM.nightMode)
    }

    fun setThemeMode(context: Context, themeMode: ThemeMode) {
        getSharedPreferences(context)
            .edit()
            .putString(KEY_SETTING_THEME, themeMode.nightMode.toString())
            .apply()
        CommonUtils.setTheme(themeMode.nightMode)
    }

    fun isCallLogFavoritesExpanded(context: Context): Boolean =
        getSharedPreferences(context).getBoolean(KEY_CALL_LOG_FAVORITES_EXPANDED, true)

    fun setCallLogFavoritesExpanded(context: Context, expanded: Boolean) {
        getSharedPreferences(context)
            .edit()
            .putBoolean(KEY_CALL_LOG_FAVORITES_EXPANDED, expanded)
            .apply()
    }
}
