package dev.alenajam.opendialer.feature.callDetail

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.squareup.picasso.Transformation
import dagger.hilt.android.AndroidEntryPoint
import dev.alenajam.opendialer.core.common.CALL_DETAIL_PARAM_CALL_IDS
import dev.alenajam.opendialer.core.common.CircleTransform
import dev.alenajam.opendialer.core.common.OnStatusBarColorChange
import dev.alenajam.opendialer.core.common.PermissionUtils
import dev.alenajam.opendialer.core.common.SharedPreferenceHelper
import dev.alenajam.opendialer.core.common.ToolbarListener
import dev.alenajam.opendialer.data.calls.CallOption
import dev.alenajam.opendialer.data.calls.DialerCall
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
class CallDetailFragment : Fragment(), View.OnClickListener {
  private val viewModel: DialerViewModel by viewModels()
  private lateinit var callIds: List<Int>
  private lateinit var call: DialerCall
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

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context is ToolbarListener) {
      toolbarListener = context
    }
    if (context is OnStatusBarColorChange) {
      onStatusBarColorChange = context
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val pref = SharedPreferenceHelper.getSharedPreferences(context)
    val idsStr = pref.getString(CALL_DETAIL_PARAM_CALL_IDS, null)

    idsStr?.let {
      pref.edit().remove(CALL_DETAIL_PARAM_CALL_IDS).apply()
      callIds = it.split(',').map { id -> id.toInt() }
    }

    if (callIds.isEmpty()) {
      throw IllegalArgumentException("No valid call IDs were set in SharedPreferences before ${CallDetailFragment::class.java.simpleName} onCreate")
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    /*_binding = FragmentCallDetailBinding.inflate(
      inflater,
      container,
      false
    )
    return binding.root*/

    return ComposeView(requireContext()).apply {
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
      setContent {
        CallDetailScreen()
      }
    }
  }

  @ExperimentalCoroutinesApi
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    onStatusBarColorChange?.onColorChange(view.context.getColor(R.color.colorPrimaryDark))
    toolbarListener?.hideToolbar(false)

    /*binding.toolbarLayout.toolbar.setNavigationOnClickListener { goBack() }
    context?.let { binding.toolbarLayout.toolbar.setTitle(R.string.call_details) }

    viewModel.call.observe(viewLifecycleOwner) {
      this.call = it
      renderCall()
    }
    viewModel.detailOptions.observe(viewLifecycleOwner) { handleOptions(it) }
    viewModel.deletedDetailCalls.observe(viewLifecycleOwner,
      dev.alenajam.opendialer.core.common.functional.EventObserver { goBack() })
    viewModel.blockedCaller.observe(
      viewLifecycleOwner,
      dev.alenajam.opendialer.core.common.functional.EventObserver { handleBlockedCaller(true) })
    viewModel.unblockedCaller.observe(
      viewLifecycleOwner,
      dev.alenajam.opendialer.core.common.functional.EventObserver { handleBlockedCaller(false) })

    binding.callButton.setOnClickListener(this)
    binding.contactIcon.setOnClickListener(this)*/

    viewModel.getCallByIds(callIds)
  }

  private fun renderCall() {
    /*context?.let { context ->
      viewModel.getDetailOptions(call)

      Picasso.get()
        .load(call.contactInfo.photoUri)
        .transform(circleTransform)
        .into(binding.contactIcon)
    }

    when {
      call.isAnonymous() -> {
        binding.title.text = context?.getString(R.string.anonymous)
        binding.subtitle.visibility = View.GONE
        binding.callButton.visibility = View.GONE
      }

      call.contactInfo.name.isNullOrBlank() -> {
        binding.title.text = call.contactInfo.number
        binding.subtitle.visibility = View.GONE
      }

      else -> {
        binding.title.text = call.contactInfo.name
        binding.subtitle.text = call.contactInfo.number
      }
    }

    binding.recyclerViewCallDetails.layoutManager = LinearLayoutManager(context)
    binding.recyclerViewCallDetails.adapter = CallDetailsAdapter(call.childCalls, context)

    binding.recyclerViewCallDetailsOptions.layoutManager = LinearLayoutManager(context)
    optionsAdapter = CallOptionsAdapter { option ->
      handleOptionClick(option)
    }
    binding.recyclerViewCallDetailsOptions.adapter = optionsAdapter*/
  }

  private fun handleOptions(options: List<CallOption>) {
    // optionsAdapter?.setData(options)
  }

  private fun handleOptionClick(option: CallOption) {
    when (option.id) {
      CallOption.ID_COPY_NUMBER -> viewModel.copyNumber(call)
      CallOption.ID_EDIT_BEFORE_CALL -> activity?.let {
        viewModel.editNumberBeforeCall(
          it,
          call
        )
      }

      CallOption.ID_DELETE -> viewModel.deleteCalls(call)
      CallOption.ID_BLOCK_CALLER -> viewModel.blockCaller(call)
      CallOption.ID_UNBLOCK_CALLER -> viewModel.unblockCaller(call)
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
    if (PermissionUtils.hasMakeCallPermission(context)) {
      activity?.let { call.contactInfo.number?.let { num -> viewModel.makeCall(it, num) } }
    } else {
      requestMakeCallPermissions.launch(PermissionUtils.makeCallPermissions)
    }
  }

  override fun onClick(v: View?) {
    /*if (v?.id == binding.callButton.id) {
      activity?.let { makeCall() }
    } else if (v?.id == binding.contactIcon.id) {
      activity?.let { viewModel.openContact(it, call) }
    }*/
  }

  private fun goBack() {
    findNavController().popBackStack()
  }
}