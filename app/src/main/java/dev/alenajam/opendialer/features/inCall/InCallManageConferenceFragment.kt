package dev.alenajam.opendialer.features.inCall

import android.content.Context
import android.os.Bundle
import android.telecom.Call
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import dev.alenajam.opendialer.R

@AndroidEntryPoint
class InCallManageConferenceFragment : Fragment() {
  private val viewModel: InCallViewModel by activityViewModels()
  private lateinit var calls: InCallManageConferenceCalls
  private var popBackstackInProgress = false

  companion object {
    fun newInstance() = InCallFragment()
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val rootView = LinearLayout(inflater.context).apply { orientation = LinearLayout.VERTICAL }
    val contentView = FrameLayout(inflater.context)

    rootView.setBackgroundColor(inflater.context.getColor(R.color.windowBackground))

    val toolbar = Toolbar(inflater.context).apply {
      setBackgroundColor(inflater.context.getColor(R.color.colorPrimary))
      title = inflater.context.getString(R.string.manageConferenceCall)
      setTitleTextColor(inflater.context.getColor(R.color.white))
      navigationIcon = inflater.context.getDrawable(R.drawable.ic_arrow_back)
      setNavigationOnClickListener { activity?.onBackPressed() }
    }
    rootView.addView(toolbar)

    calls = InCallManageConferenceCalls(inflater.context)
    contentView.addView(calls)
    rootView.addView(contentView)

    return rootView
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val callObserver = Observer<dev.alenajam.opendialer.model.OngoingCall> {
      getPrimaryCall()?.let {
        if (it == dev.alenajam.opendialer.model.OngoingCall.ONGOING_CALL_NULL) return@Observer

        renderCall(it)
      }
    }

    viewModel.primaryCall.observe(viewLifecycleOwner, callObserver)
    viewModel.secondaryCall.observe(viewLifecycleOwner, callObserver)

    calls.setListener(this::callClick)
  }

  private fun renderCall(call: dev.alenajam.opendialer.model.OngoingCall) {
    if (!call.call.details.can(Call.Details.CAPABILITY_MANAGE_CONFERENCE) && !popBackstackInProgress) {
      popBackstackInProgress = true
      findNavController().popBackStack()
    }

    val ongoingCalls = viewModel.calls.value
    val childCalls = call.call.children.mapNotNull { c -> ongoingCalls?.get(c) }
    calls.setList(childCalls)

    val canSeparate: Boolean =
      getSecondaryCall() == null || getSecondaryCall() == dev.alenajam.opendialer.model.OngoingCall.ONGOING_CALL_NULL
    calls.setCanSeparate(canSeparate)
  }

  private fun callClick(call: dev.alenajam.opendialer.model.OngoingCall, action: InCallManageConferenceCallAction) {
    when (action) {
      InCallManageConferenceCallAction.HANGUP -> call.hangup()
      InCallManageConferenceCallAction.SPLIT -> call.call.splitFromConference()
    }
  }

  private fun getPrimaryCall() = viewModel.primaryCall.value

  private fun getSecondaryCall() = viewModel.secondaryCall.value
}

class InCallManageConferenceCalls(context: Context, attrs: AttributeSet? = null) :
  RecyclerView(context, attrs) {
  private val myAdapter: InCallManageConferenceAdapter
  private var listener: ((call: dev.alenajam.opendialer.model.OngoingCall, action: InCallManageConferenceCallAction) -> Unit)? =
    null

  init {
    myAdapter = InCallManageConferenceAdapter(this::onCallClick)
    this.adapter = myAdapter
    layoutManager = LinearLayoutManager(context)
  }

  private fun onCallClick(call: dev.alenajam.opendialer.model.OngoingCall, action: InCallManageConferenceCallAction) {
    listener?.invoke(call, action)
  }

  fun setListener(listener: ((call: dev.alenajam.opendialer.model.OngoingCall, action: InCallManageConferenceCallAction) -> Unit)) {
    this.listener = listener
  }

  fun setList(calls: List<dev.alenajam.opendialer.model.OngoingCall>) {
    myAdapter.setList(calls)
  }

  fun setCanSeparate(canSeparate: Boolean) {
    myAdapter.setCanSeparate(canSeparate)
  }
}

class InCallManageConferenceAdapter(private val onItemClick: (call: dev.alenajam.opendialer.model.OngoingCall, action: InCallManageConferenceCallAction) -> Unit) :
  RecyclerView.Adapter<InCallManageConferenceAdapter.ViewHolder>() {
  private var calls: List<dev.alenajam.opendialer.model.OngoingCall> = listOf()
  private var canSeparate = false

  class ViewHolder(inflater: LayoutInflater, private val parent: ViewGroup) :
    RecyclerView.ViewHolder(
      inflater.inflate(
        R.layout.item_manage_conference_call,
        parent,
        false
      )
    ) {
    private val caller: TextView = itemView.findViewById(R.id.callerTextView)
    private val hangup: dev.alenajam.opendialer.view.MyButton = itemView.findViewById(R.id.hangupButton)
    private val split: dev.alenajam.opendialer.view.MyButton = itemView.findViewById(R.id.splitButton)

    fun bind(
      call: dev.alenajam.opendialer.model.OngoingCall,
      onItemClick: (call: dev.alenajam.opendialer.model.OngoingCall, action: InCallManageConferenceCallAction) -> Unit,
      canSeparate: Boolean
    ) {
      caller.text = call.callerName ?: call.callerNumber

      val showHangup =
        call.call.details.can(Call.Details.CAPABILITY_DISCONNECT_FROM_CONFERENCE)
      val showSplit =
        canSeparate && call.call.details.can(Call.Details.CAPABILITY_SEPARATE_FROM_CONFERENCE)

      hangup.visibility = if (showHangup) View.VISIBLE else View.GONE
      split.visibility = if (showSplit) View.VISIBLE else View.GONE

      hangup.setOnClickListener { onItemClick(call, InCallManageConferenceCallAction.HANGUP) }
      split.setOnClickListener { onItemClick(call, InCallManageConferenceCallAction.SPLIT) }
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
    ViewHolder(LayoutInflater.from(parent.context), parent)

  override fun getItemCount(): Int = calls.size

  override fun onBindViewHolder(holder: ViewHolder, position: Int) =
    holder.bind(calls[position], onItemClick, canSeparate)

  fun setList(calls: List<dev.alenajam.opendialer.model.OngoingCall>) {
    this.calls = calls
    notifyDataSetChanged()
  }

  fun clear() {
    calls = listOf()
    notifyDataSetChanged()
  }

  fun setCanSeparate(canSeparate: Boolean) {
    this.canSeparate = canSeparate
    notifyDataSetChanged()
  }
}

enum class InCallManageConferenceCallAction {
  HANGUP,
  SPLIT
}