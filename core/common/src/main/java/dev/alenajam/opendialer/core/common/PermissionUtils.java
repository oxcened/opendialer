package dev.alenajam.opendialer.core.common;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.fragment.app.Fragment;

public abstract class PermissionUtils {
  public static String[] recentsPermissions = new String[]{Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG};
  public static String[] contactsPermissions = new String[]{Manifest.permission.READ_CONTACTS};
  public static String[] searchPermissions = new String[]{Manifest.permission.READ_CONTACTS};
  public static String[] makeCallPermissions = new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE};

  public static boolean hasRecentsPermission(Context context) {
    return hasPermissions(context, recentsPermissions);
  }

  public static boolean hasContactsPermission(Context context) {
    return hasPermissions(context, contactsPermissions);
  }

  public static boolean hasSearchPermission(Context context) {
    return hasPermissions(context, searchPermissions);
  }

  public static boolean hasMakeCallPermission(Context context) {
    return hasPermissions(context, makeCallPermissions);
  }

  private static boolean hasPermissions(Context context, String[] permissions) {
    if (context == null) return false;
    for (String permission : permissions) {
      if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
        return false;
      }
    }
    return true;
  }

  public static void requestRecentsPermission(Activity activity, int requestCode) {
    activity.requestPermissions(recentsPermissions, requestCode);
  }

  public static void requestContactsPermission(Fragment fragment, int requestCode) {
    fragment.requestPermissions(contactsPermissions, requestCode);
  }

  public static void requestContactsPermission(Activity activity, int requestCode) {
    activity.requestPermissions(contactsPermissions, requestCode);
  }

  public static void requestMakeCallPermission(Activity activity, int requestCode) {
    activity.requestPermissions(makeCallPermissions, requestCode);
  }

  public static void requestSearchPermission(Activity activity, int requestCode) {
    activity.requestPermissions(searchPermissions, requestCode);
  }
}
