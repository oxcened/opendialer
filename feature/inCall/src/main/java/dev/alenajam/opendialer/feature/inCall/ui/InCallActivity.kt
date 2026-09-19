package dev.alenajam.opendialer.feature.inCall.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.view.WindowManager
import android.content.IntentFilter
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import dev.alenajam.opendialer.core.common.ui.InCallUI
import dev.alenajam.opendialer.feature.inCall.service.CallEvent
import dev.alenajam.opendialer.feature.inCall.service.CallsHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@AndroidEntryPoint
class InCallActivity : ComponentActivity() {
    @Inject
    lateinit var inCallUI: InCallUI
    @Inject
    lateinit var callsHandler: CallsHandler
    
    private val viewModel: InCallViewModel by viewModels()

    var visibility: Boolean = false
        private set

    private val _isUiReady = MutableStateFlow(false)
    val isUiReady = _isUiReady.asStateFlow()

    private val userPresentReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_USER_PRESENT) updateUiReady()
        }
    }

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        CallEvent.FinishActivity -> finish()
                        is CallEvent.MissedCall -> Unit
                    }
                }
            }
        }

        setContent {
            inCallUI.Content()
        }
    }

    override fun onStart() {
        super.onStart()
        visibility = true
        val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(userPresentReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(userPresentReceiver, filter)
        }
        updateUiReady()
    }

    override fun onStop() {
        super.onStop()
        visibility = false
        unregisterReceiver(userPresentReceiver)
        updateUiReady()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        updateUiReady()
    }

    private fun updateUiReady() {
        _isUiReady.value = visibility &&
            !getSystemService(KeyguardManager::class.java).isKeyguardLocked
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
