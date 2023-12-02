package dev.alenajam.opendialer.util;

import androidx.appcompat.app.AppCompatDelegate;

public abstract class CommonUtils {
  public static void setTheme(int mode) {
    AppCompatDelegate.setDefaultNightMode(mode);
  }
}