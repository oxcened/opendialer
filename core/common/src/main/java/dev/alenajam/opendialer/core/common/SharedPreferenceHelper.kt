package dev.alenajam.opendialer.core.common

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.google.gson.Gson

object SharedPreferenceHelper {
    const val SP_QUICK_RESPONSES = "SP_QUICK_RESPONSES"
    const val KEY_SETTING_THEME = "theme"
    const val KEY_SETTING_DEFAULT = "default"
    const val KEY_SETTING_SOUND_VIBRATION = "sound"
    const val KEY_SETTING_QUICK_RESPONSES = "quick_responses"
    const val KEY_SETTING_BLOCKED_NUMBERS = "blockedNumbers"
    const val KEY_SETTING_NOTIFICATION_SETTINGS = "notificationSettings"

    @JvmStatic
    fun getSharedPreferences(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    @JvmStatic
    fun init(context: Context) {
        val sharedPreferences = getSharedPreferences(context)

        val theme = sharedPreferences.getString(KEY_SETTING_THEME, null)
        try {
            CommonUtils.setTheme(if (theme == null) AppCompatDelegate.MODE_NIGHT_NO else theme.toInt())
        } catch (e: NumberFormatException) {
            Log.d(SharedPreferenceHelper::class.java.simpleName, e.localizedMessage ?: "")
        }

        if (!sharedPreferences.contains(SP_QUICK_RESPONSES)) {
            val quickResponses = context.resources.getStringArray(R.array.array_quick_responses)
            val quickResponseList = quickResponses.toMutableList()
            sharedPreferences.edit().putString(SP_QUICK_RESPONSES, Gson().toJson(quickResponseList)).apply()
        }
    }
}
