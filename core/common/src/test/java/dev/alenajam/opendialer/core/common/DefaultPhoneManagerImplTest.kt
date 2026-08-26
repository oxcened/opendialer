package dev.alenajam.opendialer.core.common

import android.app.role.RoleManager
import android.content.Context
import android.os.Build
import android.telecom.TelecomManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class DefaultPhoneManagerImplTest {

    @Test
    fun `isDefaultDialer returns true when role is held on Android Q and above`() {
        val roleManager = mock<RoleManager> {
            on { isRoleAvailable(RoleManager.ROLE_DIALER) } doReturn true
            on { isRoleHeld(RoleManager.ROLE_DIALER) } doReturn true
        }
        val context = mock<Context> {
            on { getSystemService(Context.ROLE_SERVICE) } doReturn roleManager
        }

        val manager = DefaultPhoneManagerImpl(context, sdkVersion = Build.VERSION_CODES.Q)
        assertTrue(manager.isDefaultDialer())
    }

    @Test
    fun `isDefaultDialer returns false when role is not held on Android Q and above`() {
        val roleManager = mock<RoleManager> {
            on { isRoleAvailable(RoleManager.ROLE_DIALER) } doReturn true
            on { isRoleHeld(RoleManager.ROLE_DIALER) } doReturn false
        }
        val context = mock<Context> {
            on { getSystemService(Context.ROLE_SERVICE) } doReturn roleManager
        }

        val manager = DefaultPhoneManagerImpl(context, sdkVersion = Build.VERSION_CODES.Q)
        assertFalse(manager.isDefaultDialer())
    }

    @Test
    fun `isDefaultDialer returns true when package matches on legacy Android`() {
        val telecomManager = mock<TelecomManager> {
            on { defaultDialerPackage } doReturn "com.example.app"
        }
        val context = mock<Context> {
            on { getSystemService(Context.TELECOM_SERVICE) } doReturn telecomManager
            on { packageName } doReturn "com.example.app"
        }

        val manager = DefaultPhoneManagerImpl(context, sdkVersion = Build.VERSION_CODES.P)
        assertTrue(manager.isDefaultDialer())
    }
}
