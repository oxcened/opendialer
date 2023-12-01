package dev.alenajam.opendialer.feature.inCall.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.telecom.Call
import android.telecom.CallAudioState
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.alenajam.opendialer.feature.inCall.R
import dev.alenajam.opendialer.feature.inCall.databinding.InCallActiveRunButtonBinding
import dev.alenajam.opendialer.feature.inCall.databinding.IncomingButtonsBinding
import dev.alenajam.opendialer.feature.inCall.service.OngoingCall

class InCallActiveButton(val id: InCallActiveButtonId, var checked: Boolean = false) {
  enum class InCallActiveButtonId {
    MUTE,
    KEYPAD,
    SPEAKER,
    ADD_CALL,
    HOLD,
    SWAP,
    BLUETOOTH,
    MERGE,
    MANAGE_CONFERENCE
  }
}

class InCallButtons(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
  //private val incomingButtons: View = inflate(context, R.layout.incoming_buttons, null)
  val incomingButtons = IncomingButtonsBinding.inflate(LayoutInflater.from(context), this, false)
  private val activeButtons = InCallActiveButtons(context)
  val activeEndCallButton =
    InCallActiveRunButtonBinding.inflate(LayoutInflater.from(context), this, false)
  private var listener: ((button: InCallActiveButton) -> Unit)? = null

  init {
    addView(incomingButtons.root)
    activeButtons.layoutParams =
      LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
    activeButtons.setListener { b -> listener?.invoke(b) }
    addView(activeButtons)
    addView(activeEndCallButton.root)
    clear()
  }

  fun updateButtons(
    primary: OngoingCall?,
    secondary: OngoingCall?,
    canAddCall: Boolean?,
    audioState: CallAudioState?
  ) {
    clear()
    primary?.let {
      if (it === OngoingCall.ONGOING_CALL_NULL) return

      if (it.state == Call.STATE_RINGING) {
        showIncomingButtons(true)
      } else {
        showActiveButtons(true)

        val showMute = it.call.details.can(Call.Details.CAPABILITY_MUTE)
        val showSpeaker =
          audioState?.supportedRouteMask?.and(CallAudioState.ROUTE_SPEAKER) != 0
        val showAddCall = canAddCall == true
        val showSwap =
          secondary != null && secondary !== OngoingCall.ONGOING_CALL_NULL && it.state == Call.STATE_ACTIVE
        val showHold = !showSwap && it.call.details.can(Call.Details.CAPABILITY_HOLD)
        val showMerge = it.canBeMerged()
        val showBluetooth =
          audioState?.supportedRouteMask?.and(CallAudioState.ROUTE_BLUETOOTH) != 0
        val showManageConference =
          it.call.details.can(Call.Details.CAPABILITY_MANAGE_CONFERENCE)

        showActiveButton(InCallActiveButton.InCallActiveButtonId.MUTE, showMute)
        showActiveButton(InCallActiveButton.InCallActiveButtonId.KEYPAD, true)
        showActiveButton(InCallActiveButton.InCallActiveButtonId.SPEAKER, showSpeaker)
        showActiveButton(InCallActiveButton.InCallActiveButtonId.ADD_CALL, showAddCall)
        showActiveButton(InCallActiveButton.InCallActiveButtonId.SWAP, showSwap)
        showActiveButton(InCallActiveButton.InCallActiveButtonId.HOLD, showHold)
        showActiveButton(InCallActiveButton.InCallActiveButtonId.MERGE, showMerge)
        showActiveButton(InCallActiveButton.InCallActiveButtonId.BLUETOOTH, showBluetooth)
        showActiveButton(
          InCallActiveButton.InCallActiveButtonId.MANAGE_CONFERENCE,
          showManageConference
        )

        val checkMute = audioState?.isMuted == true
        val checkSpeaker = audioState?.route == CallAudioState.ROUTE_SPEAKER
        val checkHold = it.call.state == Call.STATE_HOLDING
        val checkBluetooth = audioState?.route == CallAudioState.ROUTE_BLUETOOTH

        checkActiveButton(InCallActiveButton.InCallActiveButtonId.MUTE, checkMute)
        checkActiveButton(InCallActiveButton.InCallActiveButtonId.SPEAKER, checkSpeaker)
        checkActiveButton(InCallActiveButton.InCallActiveButtonId.HOLD, checkHold)
        checkActiveButton(InCallActiveButton.InCallActiveButtonId.BLUETOOTH, checkBluetooth)
      }
    }
  }

  private fun showIncomingButtons(show: Boolean) {
    incomingButtons.root.visibility = if (show) View.VISIBLE else View.GONE
  }

  private fun showActiveButtons(show: Boolean) {
    activeButtons.visibility = if (show) View.VISIBLE else View.GONE
    activeEndCallButton.root.visibility = if (show) View.VISIBLE else View.GONE
  }

  private fun showActiveButton(buttonId: InCallActiveButton.InCallActiveButtonId, show: Boolean) {
    if (activeButtons.visibility == View.VISIBLE) {
      activeButtons.showButton(buttonId, show)
    }
  }

  private fun checkActiveButton(
    buttonId: InCallActiveButton.InCallActiveButtonId,
    select: Boolean
  ) {
    if (activeButtons.visibility == View.VISIBLE) {
      activeButtons.checkButton(buttonId, select)
    }
  }

  private fun hideAllActiveButtons() {
    activeButtons.hideAllButtons()
  }

  private fun clear() {
    showIncomingButtons(false)
    showActiveButtons(false)
    hideAllActiveButtons()
  }

  fun setListener(listener: ((button: InCallActiveButton) -> Unit)) {
    this.listener = listener
  }
}

class InCallActiveButtons(context: Context, attrs: AttributeSet? = null) :
  RecyclerView(context, attrs) {
  private val myAdapter: InCallButtonsAdapter
  private var listener: ((button: InCallActiveButton) -> Unit)? = null

  init {
    myAdapter = InCallButtonsAdapter(this::onButtonClick)
    this.adapter = myAdapter
    layoutManager = GridLayoutManager(context, 3)
    overScrollMode = OVER_SCROLL_NEVER
  }

  private fun onButtonClick(button: InCallActiveButton) {
    listener?.invoke(button)
  }

  fun showButton(buttonId: InCallActiveButton.InCallActiveButtonId, show: Boolean) {
    if (show) {
      myAdapter.addItem(InCallActiveButton(buttonId))
    } else {
      myAdapter.removeItem(buttonId)
    }
  }

  fun checkButton(buttonId: InCallActiveButton.InCallActiveButtonId, select: Boolean) {
    myAdapter.checkButton(buttonId, select)
  }

  fun setListener(listener: ((button: InCallActiveButton) -> Unit)) {
    this.listener = listener
  }

  fun hideAllButtons() {
    myAdapter.clear()
  }
}

class InCallButtonsAdapter(private val onItemClick: (button: InCallActiveButton) -> Unit) :
  RecyclerView.Adapter<InCallButtonsAdapter.ViewHolder>() {
  private var buttons: MutableList<InCallActiveButton> = mutableListOf()

  class ViewHolder(inflater: LayoutInflater, private val parent: ViewGroup) :
    RecyclerView.ViewHolder(
      inflater.inflate(
        R.layout.item_in_call_active_button,
        parent,
        false
      )
    ) {
    private val image: ImageView = itemView.findViewById(R.id.inCallButtonImage)
    private val layout: FrameLayout = itemView.findViewById(R.id.frameLayout)

    fun bind(button: InCallActiveButton?, onItemClick: (button: InCallActiveButton) -> Unit) {
      button?.let {
        image.setImageDrawable(getButtonImage(it))
        image.imageTintList =
          ColorStateList.valueOf(parent.context.getColor(if (button.checked) R.color.colorPrimary else R.color.colorControlNormal))
        //layout.setBackgroundColor(parent.context.getColor(if (button.checked) R.color.colorPrimary else android.R.color.transparent))
        layout.setOnClickListener { _ -> onItemClick(it) }
      }
    }

    private fun getButtonImage(button: InCallActiveButton): Drawable? {
      val resId = when (button.id) {
        InCallActiveButton.InCallActiveButtonId.MUTE -> R.drawable.baseline_mic_off_24
        InCallActiveButton.InCallActiveButtonId.KEYPAD -> R.drawable.baseline_dialpad_24
        InCallActiveButton.InCallActiveButtonId.SPEAKER -> R.drawable.baseline_volume_up_24
        InCallActiveButton.InCallActiveButtonId.ADD_CALL -> R.drawable.baseline_add_ic_call_24
        InCallActiveButton.InCallActiveButtonId.HOLD -> R.drawable.baseline_pause_24
        InCallActiveButton.InCallActiveButtonId.SWAP -> R.drawable.baseline_compare_arrows_24
        InCallActiveButton.InCallActiveButtonId.BLUETOOTH -> R.drawable.baseline_bluetooth_24
        InCallActiveButton.InCallActiveButtonId.MERGE -> R.drawable.baseline_call_merge_24
        InCallActiveButton.InCallActiveButtonId.MANAGE_CONFERENCE -> R.drawable.baseline_people_24
      }
      return AppCompatResources.getDrawable(parent.context, resId)
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
    ViewHolder(LayoutInflater.from(parent.context), parent)

  override fun getItemCount(): Int = buttons.size

  override fun onBindViewHolder(holder: ViewHolder, position: Int) =
    holder.bind(buttons[position], onItemClick)

  fun addItem(item: InCallActiveButton) {
    // If exists, skip
    if (buttons.indexOfFirst { it.id === item.id } != -1) return

    buttons.add(item)
    notifyItemInserted(buttons.size - 1)
  }

  fun removeItem(id: InCallActiveButton.InCallActiveButtonId) {
    val index = buttons.indexOfFirst { it.id === id }
    if (index != -1) {
      buttons.removeAt(index)
      notifyItemRemoved(index)
    }
  }

  fun checkButton(id: InCallActiveButton.InCallActiveButtonId, check: Boolean) {
    val index = buttons.indexOfFirst { it.id === id }
    if (index != -1) {
      buttons[index].checked = check
      notifyItemChanged(index)
    }
  }

  fun clear() {
    buttons.clear()
    notifyDataSetChanged()
  }
}