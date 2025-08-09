package dev.alenajam.opendialer.feature.inCall.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import dev.alenajam.opendialer.feature.inCall.service.CallsHandler

@AndroidEntryPoint
class InCallActivity : ComponentActivity() {
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
        super.onCreate(savedInstanceState)
        CallsHandler.setInCallActivity(this)
        val flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES
        window.addFlags(flags)
        enableEdgeToEdge()
        setContent {
            InCallScreen()
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
        CallsHandler.clearInCallActivity(this)
    }
}
