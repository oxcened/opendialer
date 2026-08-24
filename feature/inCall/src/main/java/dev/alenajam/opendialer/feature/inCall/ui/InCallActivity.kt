package dev.alenajam.opendialer.feature.inCall.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import dev.alenajam.opendialer.core.common.ui.InCallUI
import dev.alenajam.opendialer.feature.inCall.service.CallsHandler
import javax.inject.Inject

@AndroidEntryPoint
class InCallActivity : ComponentActivity() {
    @Inject
    lateinit var inCallUI: InCallUI
    @Inject
    lateinit var callsHandler: CallsHandler

    var visibility: Boolean = false
        private set

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, InCallActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        val flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES
        window.addFlags(flags)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            inCallUI.Content()
        }
    }

    override fun onStart() {
        super.onStart()
        visibility = true
    }

    override fun onStop() {
        super.onStop()
        visibility = false
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
