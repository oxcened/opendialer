package dev.alenajam.opendialer.features.inCall

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import dev.alenajam.opendialer.R

private val MANAGE_CONFERENCE_TAG = InCallManageConferenceFragment::class.java.canonicalName

class InCallActivity : AppCompatActivity() {
  var visibility: Boolean = false
    private set

  private var manageConferenceFragment: InCallManageConferenceFragment? = null

  companion object {
    fun start(context: Context) {
      val intent = Intent(context, InCallActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(intent)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    dev.alenajam.opendialer.util.CallsHandler.setInCallActivity(this)

    requestWindowFeature(Window.FEATURE_NO_TITLE)
    val flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
      WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
      WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
      WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
      WindowManager.LayoutParams.FLAG_FULLSCREEN or
      WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES
    window.addFlags(flags)
    supportActionBar?.hide()
    setContentView(R.layout.activity_in_call)
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
    dev.alenajam.opendialer.util.CallsHandler.clearInCallActivity(this)

    super.onDestroy()
  }
}
