package dev.alenajam.opendialer;

import android.app.Application;

import dagger.hilt.android.HiltAndroidApp;
import dev.alenajam.opendialer.helper.NotificationHelper;
import dev.alenajam.opendialer.helper.SharedPreferenceHelper;

@HiltAndroidApp
public class App extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    NotificationHelper.setupNotificationChannels(this);
    SharedPreferenceHelper.init(this);
  }
}
