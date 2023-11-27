package dev.alenajam.opendialer.activity;

import static dev.alenajam.opendialer.helper.SharedPreferenceHelper.KEY_SETTING_BLOCKED_NUMBERS;
import static dev.alenajam.opendialer.helper.SharedPreferenceHelper.KEY_SETTING_DEFAULT;
import static dev.alenajam.opendialer.helper.SharedPreferenceHelper.KEY_SETTING_NOTIFICATION_SETTINGS;
import static dev.alenajam.opendialer.helper.SharedPreferenceHelper.KEY_SETTING_QUICK_RESPONSES;
import static dev.alenajam.opendialer.helper.SharedPreferenceHelper.KEY_SETTING_SOUND_VIBRATION;
import static dev.alenajam.opendialer.helper.SharedPreferenceHelper.KEY_SETTING_THEME;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.BlockedNumberContract;
import android.provider.Settings;
import android.telecom.TelecomManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import dev.alenajam.opendialer.R;
import dev.alenajam.opendialer.util.CommonUtils;
import dev.alenajam.opendialer.util.DefaultPhoneUtils;

public class SettingsActivity extends AppCompatActivity {
  private static final int REQUEST_ID_DEFAULT = 1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);

    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.settings, new SettingsFragment())
        .commit();

    ActionBar actionBar = getSupportActionBar();

    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
    }
  }

  public static class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
    Preference defaultPreference;
    private TelecomManager telecomManager;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
      if (getContext() != null) {
        telecomManager = (TelecomManager) getContext().getSystemService(TELECOM_SERVICE);
      }

      setPreferencesFromResource(R.xml.root_preferences, rootKey);

      ListPreference themePreference = getPreferenceManager().findPreference(KEY_SETTING_THEME);
      if (themePreference != null) themePreference.setOnPreferenceChangeListener(this);

      defaultPreference = getPreferenceManager().findPreference(KEY_SETTING_DEFAULT);
      if (defaultPreference != null) defaultPreference.setOnPreferenceClickListener(this);

      Preference soundPreference = getPreferenceManager().findPreference(KEY_SETTING_SOUND_VIBRATION);
      if (soundPreference != null) soundPreference.setOnPreferenceClickListener(this);

      Preference quickResponsesPreference = getPreferenceManager().findPreference(KEY_SETTING_QUICK_RESPONSES);
      if (quickResponsesPreference != null)
        quickResponsesPreference.setOnPreferenceClickListener(this);

      Preference blockedNumbers = getPreferenceManager().findPreference(KEY_SETTING_BLOCKED_NUMBERS);
      if (blockedNumbers != null) {
        blockedNumbers.setVisible(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && BlockedNumberContract.canCurrentUserBlockNumbers(getContext()));
        blockedNumbers.setOnPreferenceClickListener(this);
      }

      Preference notificationSettings = getPreferenceManager().findPreference(KEY_SETTING_NOTIFICATION_SETTINGS);
      if (notificationSettings != null) {
        notificationSettings.setVisible(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O);
        notificationSettings.setOnPreferenceClickListener(this);
      }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
      switch (preference.getKey()) {
        case KEY_SETTING_THEME:
          CommonUtils.setTheme(Integer.parseInt(newValue.toString()));
          break;
      }
      return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults);

      if (requestCode == REQUEST_ID_DEFAULT) {
        for (int g : grantResults) {
          if (g != PackageManager.PERMISSION_GRANTED) return;
        }
        if (defaultPreference != null) defaultPreference.setEnabled(false);
      }
    }

    @Override
    public void onResume() {
      super.onResume();

      if (defaultPreference != null) {
        boolean hasDefault = DefaultPhoneUtils.hasDefault(getContext());
        defaultPreference.setEnabled(!hasDefault);
        defaultPreference.setTitle(hasDefault ? R.string.default_title_on : R.string.default_title_off);
      }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
      switch (preference.getKey()) {
        case KEY_SETTING_DEFAULT:
          DefaultPhoneUtils.requestDefault(this, REQUEST_ID_DEFAULT);
          break;
        case KEY_SETTING_SOUND_VIBRATION:
          startActivityForResult(new Intent(Settings.ACTION_SOUND_SETTINGS), 0);
          break;
        case KEY_SETTING_QUICK_RESPONSES:
          startActivity(new Intent(getContext(), CustomizeQuickResponsesActivity.class));
          break;
        case KEY_SETTING_BLOCKED_NUMBERS:
          if (telecomManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            startActivity(telecomManager.createManageBlockedNumbersIntent());
          }
          break;
        case KEY_SETTING_NOTIFICATION_SETTINGS:
          if (getContext() != null) {
            CommonUtils.openNotificationSettings(getContext());
          }
          break;
      }
      return true;
    }
  }
}