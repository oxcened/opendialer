package dev.alenajam.opendialer.features.inCall

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.TelecomManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import dev.alenajam.opendialer.R
import kotlinx.android.synthetic.main.fragment_in_call.*
import kotlinx.android.synthetic.main.in_call_active_run_button.*
import kotlinx.android.synthetic.main.incoming_buttons.*
import java.util.*
import java.util.concurrent.*
import javax.inject.Inject

class InCallFragment : Fragment(), dev.alenajam.opendialer.view.DtmfKeypadBottomDialog.OnKeyClickListener, View.OnClickListener,
  dev.alenajam.opendialer.view.RefuseWithMessageDialog.RefuseWithMessageDialogChoiceListener {
  @Inject
  lateinit var viewModelFactory: ViewModelProvider.Factory
  private lateinit var viewModel: InCallViewModel
  private var telecomManager: TelecomManager? = null
  private var dtmfKeypadBottomDialog: dev.alenajam.opendialer.view.DtmfKeypadBottomDialog? = null
  private var refuseWithMessageDialog: dev.alenajam.opendialer.view.RefuseWithMessageDialog? = null

  private lateinit var callTimeScheduler: ScheduledExecutorService
  lateinit var callTimeHandler: ScheduledFuture<*>
  private val callTimeRunnable: Runnable = Runnable {
    getPrimaryCall()?.let {
      if (it !== dev.alenajam.opendialer.model.OngoingCall.ONGOING_CALL_NULL && it.state == Call.STATE_ACTIVE) {
        val differenceTime = dev.alenajam.opendialer.util.CommonUtils.getCurrentTime() - it.startTime + it.totalTime
        activity?.runOnUiThread {
          subtitle?.text = dev.alenajam.opendialer.util.CommonUtils.getDurationTimeString(differenceTime)
        }
      }
    }
  }

  companion object {
    fun newInstance() = InCallFragment()
    val handler = Handler(Looper.getMainLooper())
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (activity?.application as? dev.alenajam.opendialer.App)?.applicationComponent?.inject(this)
    val viewModel by activityViewModels<InCallViewModel> { viewModelFactory }
    this.viewModel = viewModel
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = inflater.inflate(R.layout.fragment_in_call, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val callObserver = Observer<dev.alenajam.opendialer.model.OngoingCall> {
      getPrimaryCall()?.let {
        if (it == dev.alenajam.opendialer.model.OngoingCall.ONGOING_CALL_NULL) return@Observer

        renderMainCall(it)
        renderCallState(it.state)
      }
    }

    viewModel.primaryCall.observe(viewLifecycleOwner, callObserver)
    viewModel.secondaryCall.observe(viewLifecycleOwner, callObserver)

    viewModel.audioState.observe(viewLifecycleOwner, Observer { renderAudioState(it) })
    viewModel.canAddCall.observe(viewLifecycleOwner, Observer { renderCanAddCall(it) })

    listOf(fight, run, option, hide, runSingle).map { it.setOnClickListener(this) }
    inCallButtons.setListener(this::onCallButtonClick)

    if (savedInstanceState == null) {
      /*childFragmentManager.beginTransaction()
          .replace(R.id.splash, InCallSplashFragment.newInstance())
          .commitNow()*/
    }

    context?.let { telecomManager = getSystemService(it, TelecomManager::class.java) }

    callTimeScheduler = Executors.newSingleThreadScheduledExecutor()
    callTimeHandler =
      callTimeScheduler.scheduleAtFixedRate(callTimeRunnable, 0, 1, TimeUnit.SECONDS)
  }

  private fun onCallButtonClick(button: InCallActiveButton) {
    when (button.id) {
      InCallActiveButton.InCallActiveButtonId.MUTE -> viewModel.turnMute()
      InCallActiveButton.InCallActiveButtonId.KEYPAD -> keypadClick()
      InCallActiveButton.InCallActiveButtonId.SPEAKER -> viewModel.turnSpeaker()
      InCallActiveButton.InCallActiveButtonId.ADD_CALL -> viewModel.addCall(this)
      InCallActiveButton.InCallActiveButtonId.HOLD -> getPrimaryCall()?.let {
        viewModel.hold(
          it
        )
      }

      InCallActiveButton.InCallActiveButtonId.SWAP -> viewModel.switch()
      InCallActiveButton.InCallActiveButtonId.BLUETOOTH -> viewModel.turnBluetooth()
      InCallActiveButton.InCallActiveButtonId.MERGE -> getPrimaryCall()?.let {
        viewModel.merge(
          it
        )
      }

      InCallActiveButton.InCallActiveButtonId.MANAGE_CONFERENCE -> viewModel.manageConference(
        this
      )
    }
  }

  private fun keypadClick() {
    dtmfKeypadBottomDialog = dev.alenajam.opendialer.view.DtmfKeypadBottomDialog(context)
    dtmfKeypadBottomDialog?.apply {
      listener = this@InCallFragment
      keysText = getPrimaryCall()?.keypadText
      show()
    }
  }

  override fun onClick(v: View?) {
    getPrimaryCall()?.let {
      when (v) {
        run -> viewModel.hangup(it)
        fight -> viewModel.answer(it)
        option -> {
          refuseWithMessageDialog =
            dev.alenajam.opendialer.view.RefuseWithMessageDialog(context, this)
          refuseWithMessageDialog?.show()
        }

        hide -> finish()
        runSingle -> viewModel.hangup(it)
        else -> Unit
      }
    }
  }

  @SuppressLint("MissingPermission")
  private fun renderMainCall(call: dev.alenajam.opendialer.model.OngoingCall) = call.let {
    title.text = it.callerName

    if (dev.alenajam.opendialer.util.PermissionUtils.hasMakeCallPermission(context) && (telecomManager?.callCapablePhoneAccounts?.size
        ?: 0) > 1
    ) {
      simContainer.visibility = View.VISIBLE
      sim.text = telecomManager?.getPhoneAccount(it.call.details.accountHandle)?.label
    }
  }

  private fun renderCallState(state: Int) {
    handler.removeCallbacksAndMessages(null)

    getPrimaryCall()?.let {
      updateButtons()

      when (state) {
        Call.STATE_ACTIVE -> {

        }

        else -> subtitle.text = when (state) {
          Call.STATE_RINGING -> getString(R.string.call_ringing_title)
          Call.STATE_CONNECTING -> getString(R.string.call_connecting_title)
          Call.STATE_HOLDING -> getString(R.string.call_holding_title)
          Call.STATE_DIALING -> getString(R.string.call_dialing_title)
          Call.STATE_DISCONNECTING -> getString(R.string.call_disconnecting_title)
          Call.STATE_DISCONNECTED -> getString(R.string.call_disconnected_title)
          else -> null
        }
      }
    }
  }

  private fun renderAudioState(audioState: CallAudioState) {
    updateButtons()
  }

  private fun renderCanAddCall(canAddCall: Boolean) {
    updateButtons()
  }

  private fun updateButtons() {
    inCallButtons.updateButtons(
      getPrimaryCall(),
      getSecondaryCall(),
      viewModel.canAddCall.value,
      viewModel.audioState.value
    )
  }

  override fun onKeypadChoice(whichKey: dev.alenajam.opendialer.view.DtmfKeypadBottomDialog.Key?) {
    getPrimaryCall()?.let {
      it.keypadText = it.keypadText + whichKey!!.value.toString()
      viewModel.playDtmf(it, whichKey.value)
    }
  }

  override fun onRefuseWithMessageChoice(message: String?) {
    getPrimaryCall()?.let { viewModel.hangup(it, message) }
  }

  private fun getPrimaryCall() = viewModel.primaryCall.value

  private fun getSecondaryCall() = viewModel.secondaryCall.value

  private fun finish() {
    activity?.finish()
  }

  override fun onDestroyView() {
    if (refuseWithMessageDialog?.isShowing == true) refuseWithMessageDialog?.hide()
    if (dtmfKeypadBottomDialog?.isShowing == true) dtmfKeypadBottomDialog?.hide()
    callTimeHandler.cancel(true)
    callTimeScheduler.shutdown()
    super.onDestroyView()
  }
}
