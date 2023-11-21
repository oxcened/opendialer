package dev.alenajam.opendialer;

import android.app.Application;
import android.content.SharedPreferences;

import dev.alenajam.opendialer.core.di.ApplicationComponent;
import dev.alenajam.opendialer.core.di.ApplicationModule;
import dev.alenajam.opendialer.core.di.DaggerApplicationComponent;
import dev.alenajam.opendialer.helper.NotificationHelper;
import dev.alenajam.opendialer.helper.SharedPreferenceHelper;
import com.jakewharton.threetenabp.AndroidThreeTen;

public class App extends Application {
  ApplicationComponent applicationComponent;

  @Override
  public void onCreate() {
    super.onCreate();
    applicationComponent = DaggerApplicationComponent
        .builder()
        .applicationModule(new ApplicationModule(this))
        .build();
    NotificationHelper.setupNotificationChannels(this);
    AndroidThreeTen.init(this);
    SharedPreferenceHelper.init(this);
  }

  public SharedPreferences getAppSharedPreferences() {
    return SharedPreferenceHelper.getSharedPreferences(this);
  }

  public ApplicationComponent getApplicationComponent() {
    return applicationComponent;
  }
}
