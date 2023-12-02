package dev.alenajam.opendialer.core.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class SharedPreferenceHelper {
  public static final String SP_QUICK_RESPONSES = "SP_QUICK_RESPONSES";
  public static final String KEY_SETTING_THEME = "theme";
  public static final String KEY_SETTING_DEFAULT = "default";
  public static final String KEY_SETTING_SOUND_VIBRATION = "sound";
  public static final String KEY_SETTING_QUICK_RESPONSES = "quick_responses";
  public static final String KEY_SETTING_BLOCKED_NUMBERS = "blockedNumbers";
  public static final String KEY_SETTING_NOTIFICATION_SETTINGS = "notificationSettings";

  public static SharedPreferences getSharedPreferences(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context);
  }

  public static void init(Context context) {
    SharedPreferences sharedPreferences = getSharedPreferences(context);

    String theme = sharedPreferences.getString(SharedPreferenceHelper.KEY_SETTING_THEME, null);
    try {
      CommonUtils.setTheme(theme == null ? AppCompatDelegate.MODE_NIGHT_NO : Integer.parseInt(theme));
    } catch (NumberFormatException e) {
      if (e.getLocalizedMessage() != null)
        Log.d(SharedPreferenceHelper.class.getSimpleName(), e.getLocalizedMessage());
    }

    if (!sharedPreferences.contains(SharedPreferenceHelper.SP_QUICK_RESPONSES)) {
      String[] quickResponses = context.getResources().getStringArray(R.array.array_quick_responses);
      ArrayList<String> quickResponseList = new ArrayList<>(Arrays.asList(quickResponses));
      sharedPreferences.edit().putString(SharedPreferenceHelper.SP_QUICK_RESPONSES, new Gson().toJson(quickResponseList)).apply();
    }
  }
}
