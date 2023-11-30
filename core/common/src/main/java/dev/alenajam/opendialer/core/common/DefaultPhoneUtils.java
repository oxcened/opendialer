package dev.alenajam.opendialer.core.common;

import static android.content.Context.ROLE_SERVICE;
import static android.content.Context.TELECOM_SERVICE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.telecom.TelecomManager;

import androidx.fragment.app.Fragment;

public abstract class DefaultPhoneUtils {

  public static boolean hasDefault(Context context) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
      @SuppressLint("WrongConstant") RoleManager roleManager = (RoleManager) context.getSystemService(ROLE_SERVICE);
      if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_DIALER))
        return roleManager.isRoleHeld(RoleManager.ROLE_DIALER);
    } else {
      TelecomManager telecomManager = (TelecomManager) context.getSystemService(TELECOM_SERVICE);
      String defaultDialer = telecomManager.getDefaultDialerPackage();
      if (telecomManager != null && defaultDialer != null)
        return defaultDialer.equals(context.getPackageName());
    }
    return false;
  }

  public static void requestDefault(Activity activity, int requestId) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
      @SuppressLint("WrongConstant") RoleManager roleManager = (RoleManager) activity.getSystemService(ROLE_SERVICE);
      if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_DIALER) && !roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
        Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER);
        activity.startActivityForResult(intent, requestId);
      }
    } else {
      TelecomManager telecomManager = (TelecomManager) activity.getSystemService(TELECOM_SERVICE);
      if (telecomManager != null && !telecomManager.getDefaultDialerPackage().equals(activity.getPackageName())) {
        Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, activity.getPackageName());
        activity.startActivityForResult(intent, requestId);
      }
    }
  }

  public static void requestDefault(Fragment fragment, int requestId) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
      @SuppressLint("WrongConstant") RoleManager roleManager = (RoleManager) fragment.getContext().getSystemService(ROLE_SERVICE);
      if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_DIALER) && !roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
        Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER);
        fragment.startActivityForResult(intent, requestId);
      }
    } else {
      TelecomManager telecomManager = (TelecomManager) fragment.getContext().getSystemService(TELECOM_SERVICE);
      if (telecomManager != null && !telecomManager.getDefaultDialerPackage().equals(fragment.getContext().getPackageName())) {
        Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, fragment.getContext().getPackageName());
        fragment.startActivityForResult(intent, requestId);
      }
    }
  }
}
