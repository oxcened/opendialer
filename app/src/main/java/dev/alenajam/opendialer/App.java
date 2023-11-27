package dev.alenajam.opendialer;

import android.app.Application;
import android.content.SharedPreferences;

import com.jakewharton.threetenabp.AndroidThreeTen;

import dagger.hilt.android.HiltAndroidApp;
import dev.alenajam.opendialer.helper.NotificationHelper;
import dev.alenajam.opendialer.helper.SharedPreferenceHelper;

@HiltAndroidApp
public class App extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    NotificationHelper.setupNotificationChannels(this);
    AndroidThreeTen.init(this);
    SharedPreferenceHelper.init(this);
  }

  public SharedPreferences getAppSharedPreferences() {
    return SharedPreferenceHelper.getSharedPreferences(this);
  }
}
