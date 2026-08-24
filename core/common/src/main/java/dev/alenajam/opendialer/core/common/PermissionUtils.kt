package dev.alenajam.opendialer.core.common

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.fragment.app.Fragment

object PermissionUtils {
    @JvmField
    val recentsPermissions = arrayOf(Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG)

    @JvmField
    val contactsPermissions = arrayOf(Manifest.permission.READ_CONTACTS)

    @JvmField
    val searchPermissions = arrayOf(Manifest.permission.READ_CONTACTS)

    @JvmField
    val makeCallPermissions = arrayOf(Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE)

    @JvmStatic
    fun hasRecentsPermission(context: Context?): Boolean = hasPermissions(context, recentsPermissions)

    @JvmStatic
    fun hasContactsPermission(context: Context?): Boolean = hasPermissions(context, contactsPermissions)

    @JvmStatic
    fun hasSearchPermission(context: Context?): Boolean = hasPermissions(context, searchPermissions)

    @JvmStatic
    fun hasMakeCallPermission(context: Context?): Boolean = hasPermissions(context, makeCallPermissions)

    private fun hasPermissions(context: Context?, permissions: Array<String>): Boolean {
        if (context == null) return false
        return permissions.all { permission ->
            context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    @JvmStatic
    fun requestRecentsPermission(activity: Activity, requestCode: Int) {
        activity.requestPermissions(recentsPermissions, requestCode)
    }

    @JvmStatic
    fun requestContactsPermission(fragment: Fragment, requestCode: Int) {
        fragment.requestPermissions(contactsPermissions, requestCode)
    }

    @JvmStatic
    fun requestContactsPermission(activity: Activity, requestCode: Int) {
        activity.requestPermissions(contactsPermissions, requestCode)
    }

    @JvmStatic
    fun requestMakeCallPermission(activity: Activity, requestCode: Int) {
        activity.requestPermissions(makeCallPermissions, requestCode)
    }

    @JvmStatic
    fun requestSearchPermission(activity: Activity, requestCode: Int) {
        activity.requestPermissions(searchPermissions, requestCode)
    }
}
