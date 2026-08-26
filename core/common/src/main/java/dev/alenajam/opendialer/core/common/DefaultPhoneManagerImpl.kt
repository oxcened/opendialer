package dev.alenajam.opendialer.core.common

import android.annotation.SuppressLint
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.TelecomManager

class DefaultPhoneManagerImpl(
    private val context: Context,
    private val sdkVersion: Int = Build.VERSION.SDK_INT
) : DefaultPhoneManager {

    @SuppressLint("NewApi")
    override fun isDefaultDialer(): Boolean {
        return if (sdkVersion >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager?
            roleManager?.isRoleAvailable(RoleManager.ROLE_DIALER) == true &&
                    roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
        } else {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager?
            val defaultDialer = telecomManager?.defaultDialerPackage
            defaultDialer != null && defaultDialer == context.packageName
        }
    }

    @SuppressLint("NewApi")
    override fun createRequestDefaultDialerIntent(): Intent? {
        return if (sdkVersion >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager?
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_DIALER) &&
                !roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
            ) {
                roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
            } else {
                null
            }
        } else {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager?
            if (telecomManager != null && telecomManager.defaultDialerPackage != context.packageName) {
                Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                    .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, context.packageName)
            } else {
                null
            }
        }
    }
}
