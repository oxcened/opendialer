package dev.alenajam.opendialer.util;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public abstract class DeviceDetectUtils {

  public static boolean isMiui() {
    return !TextUtils.isEmpty(getSystemProperty("ro.miui.ui.version.name"));
  }

  private static String getSystemProperty(String propName) {
    String line;
    BufferedReader input = null;
    try {
      java.lang.Process p = Runtime.getRuntime().exec("getprop " + propName);
      input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
      line = input.readLine();
      input.close();
    } catch (IOException ex) {
      return null;
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return line;
  }
}