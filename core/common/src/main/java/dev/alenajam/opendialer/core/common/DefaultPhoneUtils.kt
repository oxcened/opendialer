package dev.alenajam.opendialer.core.common

import android.annotation.SuppressLint
import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.TelecomManager
import androidx.fragment.app.Fragment

object DefaultPhoneUtils {

    @JvmStatic
    fun hasDefault(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager?
            roleManager?.isRoleAvailable(RoleManager.ROLE_DIALER) == true &&
                    roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
        } else {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager?
            val defaultDialer = telecomManager?.defaultDialerPackage
            defaultDialer != null && defaultDialer == context.packageName
        }
    }

    @JvmStatic
    fun requestDefault(activity: Activity, requestId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = activity.getSystemService(Context.ROLE_SERVICE) as RoleManager?
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_DIALER) &&
                !roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
            ) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                activity.startActivityForResult(intent, requestId)
            }
        } else {
            val telecomManager = activity.getSystemService(Context.TELECOM_SERVICE) as TelecomManager?
            if (telecomManager != null && telecomManager.defaultDialerPackage != activity.packageName) {
                val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                    .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, activity.packageName)
                activity.startActivityForResult(intent, requestId)
            }
        }
    }

    @JvmStatic
    fun requestDefault(fragment: Fragment, requestId: Int) {
        val context = fragment.context ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager?
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_DIALER) &&
                !roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
            ) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                fragment.startActivityForResult(intent, requestId)
            }
        } else {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager?
            if (telecomManager != null && telecomManager.defaultDialerPackage != context.packageName) {
                val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                    .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, context.packageName)
                fragment.startActivityForResult(intent, requestId)
            }
        }
    }
}
