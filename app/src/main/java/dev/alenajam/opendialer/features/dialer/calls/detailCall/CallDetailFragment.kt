package dev.alenajam.opendialer.features.dialer.calls.detailCall

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dev.alenajam.opendialer.R
import dev.alenajam.opendialer.core.functional.EventObserver
import dev.alenajam.opendialer.features.dialer.DialerViewModel
import dev.alenajam.opendialer.features.dialer.calls.CallOptionsAdapter
import dev.alenajam.opendialer.features.dialer.calls.DialerCall
import dev.alenajam.opendialer.features.dialer.calls.RecentsAdapter
import dev.alenajam.opendialer.model.OnStatusBarColorChange
import dev.alenajam.opendialer.model.ToolbarListener
import dev.alenajam.opendialer.util.getContactImagePlaceholder
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import kotlinx.android.synthetic.main.fragment_call_detail.callButton
import kotlinx.android.synthetic.main.fragment_call_detail.contactIcon
import kotlinx.android.synthetic.main.fragment_call_detail.recycler_view_call_details
import kotlinx.android.synthetic.main.fragment_call_detail.recycler_view_call_details_options
import kotlinx.android.synthetic.main.fragment_call_detail.subtitle
import kotlinx.android.synthetic.main.fragment_call_detail.title
import kotlinx.android.synthetic.main.toolbar.toolbar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

private const val PARAM_CALL = "call"
private val circleTransform: Transformation = dev.alenajam.opendialer.util.CircleTransform()
private val colorList = listOf(
  Color.parseColor("#4FAF44"),
  Color.parseColor("#F6D145"),
  Color.parseColor("#FF9526"),
  Color.parseColor("#EF4423"),
  Color.parseColor("#328AF0")
)
private val generator = ColorGenerator.create(colorList)

class CallDetailFragment : Fragment(), View.OnClickListener {
  @Inject
  lateinit var viewModelFactory: ViewModelProvider.Factory

  private val viewModel by activityViewModels<DialerViewModel> { viewModelFactory }

  lateinit var adapter: RecentsAdapter

  private lateinit var call: DialerCall
  private var optionsAdapter: CallOptionsAdapter? = null
  private var toolbarListener: ToolbarListener? = null
  private var onStatusBarColorChange: OnStatusBarColorChange? = null

  private val requestMakeCallPermissions =
    registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { data ->
      /** Ensure that all permissions were allowed */
      if (!data.containsValue(false)) {
        /** Retry call */
        makeCall()
      }
    }

  companion object {
    fun newInstance(call: DialerCall) = CallDetailFragment().apply {
      arguments = Bundle().apply {
        putSerializable(PARAM_CALL, call)
      }
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (activity?.application as? dev.alenajam.opendialer.App)?.applicationComponent?.inject(this)
    if (context is ToolbarListener) {
      toolbarListener = context
    }
    if (context is OnStatusBarColorChange) {
      onStatusBarColorChange = context
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    arguments?.let {
      call = it.getSerializable(PARAM_CALL) as DialerCall
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_call_detail, container, false)
  }

  @ExperimentalCoroutinesApi
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    onStatusBarColorChange?.onColorChange(view.context.getColor(R.color.colorPrimaryDark))
    toolbarListener?.hideToolbar(false)

    toolbar.setNavigationOnClickListener { goBack() }
    context?.let { toolbar.setTitle(R.string.call_details) }

    viewModel.detailOptions.observe(viewLifecycleOwner, Observer { handleOptions(it) })
    viewModel.deletedDetailCalls.observe(viewLifecycleOwner, EventObserver { goBack() })
    viewModel.blockedCaller.observe(
      viewLifecycleOwner,
      EventObserver { handleBlockedCaller(true) })
    viewModel.unblockedCaller.observe(
      viewLifecycleOwner,
      EventObserver { handleBlockedCaller(false) })

    callButton.setOnClickListener(this)
    contactIcon.setOnClickListener(this)

    context?.let { context ->
      viewModel.getDetailOptions(call)

      Picasso.get()
        .load(call.contactInfo.photoUri)
        .placeholder(context.getContactImagePlaceholder(call, generator))
        .transform(circleTransform)
        .into(contactIcon)
    }

    when {
      call.isAnonymous() -> {
        title.text = context?.getString(R.string.anonymous)
        subtitle.visibility = View.GONE
        callButton.visibility = View.GONE
      }

      call.contactInfo.name.isNullOrBlank() -> {
        title.text = call.contactInfo.number
        subtitle.visibility = View.GONE
      }

      else -> {
        title.text = call.contactInfo.name
        subtitle.text = call.contactInfo.number
      }
    }

    recycler_view_call_details.layoutManager = LinearLayoutManager(context)
    recycler_view_call_details.adapter =
      dev.alenajam.opendialer.adapter.CallDetailsAdapter(call.childCalls, context)

    recycler_view_call_details_options.layoutManager = LinearLayoutManager(context)
    optionsAdapter = CallOptionsAdapter { option -> handleOptionClick(option) }
    recycler_view_call_details_options.adapter = optionsAdapter
  }

  private fun handleOptions(options: List<dev.alenajam.opendialer.model.CallOption>) {
    optionsAdapter?.setData(options)
  }

  private fun handleOptionClick(option: dev.alenajam.opendialer.model.CallOption) {
    when (option.id) {
      dev.alenajam.opendialer.model.CallOption.ID_COPY_NUMBER -> viewModel.copyNumber(call)
      dev.alenajam.opendialer.model.CallOption.ID_EDIT_BEFORE_CALL -> activity?.let {
        viewModel.editNumberBeforeCall(
          it,
          call
        )
      }

      dev.alenajam.opendialer.model.CallOption.ID_DELETE -> viewModel.deleteCalls(call)
      dev.alenajam.opendialer.model.CallOption.ID_BLOCK_CALLER -> viewModel.blockCaller(call)
      dev.alenajam.opendialer.model.CallOption.ID_UNBLOCK_CALLER -> viewModel.unblockCaller(call)
      else -> Unit
    }
  }

  private fun handleBlockedCaller(blocked: Boolean) {
    viewModel.getDetailOptions(call)
    Toast.makeText(
      context,
      getString(
        if (blocked) R.string.numberBlocked else R.string.numberUnblocked,
        call.contactInfo.number
      ),
      Toast.LENGTH_SHORT
    ).show()
  }

  private fun makeCall() {
    if (dev.alenajam.opendialer.util.PermissionUtils.hasMakeCallPermission(context)) {
      activity?.let { call.contactInfo.number?.let { num -> viewModel.makeCall(it, num) } }
    } else {
      requestMakeCallPermissions.launch(dev.alenajam.opendialer.util.PermissionUtils.makeCallPermissions)
    }
  }

  override fun onClick(v: View?) {
    if (v?.id == callButton.id) {
      activity?.let { makeCall() }
    } else if (v?.id == contactIcon.id) {
      activity?.let { viewModel.openContact(it, call) }
    }
  }

  private fun goBack() {
    findNavController().popBackStack()
  }
}