package dev.alenajam.opendialer.feature.callDetail

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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import dagger.hilt.android.AndroidEntryPoint
import dev.alenajam.opendialer.core.common.CircleTransform
import dev.alenajam.opendialer.core.common.OnStatusBarColorChange
import dev.alenajam.opendialer.core.common.PermissionUtils
import dev.alenajam.opendialer.core.common.ToolbarListener
import dev.alenajam.opendialer.data.calls.CallOption
import dev.alenajam.opendialer.data.calls.DialerCall
import dev.alenajam.opendialer.feature.callDetail.databinding.FragmentCallDetailBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi

private const val PARAM_CALL = "call"
private val circleTransform: Transformation = CircleTransform()
private val colorList = listOf(
  Color.parseColor("#4FAF44"),
  Color.parseColor("#F6D145"),
  Color.parseColor("#FF9526"),
  Color.parseColor("#EF4423"),
  Color.parseColor("#328AF0")
)
private val generator = ColorGenerator.create(colorList)

@AndroidEntryPoint
class CallDetailFragment : Fragment(), View.OnClickListener {
  private val viewModel: DialerViewModel by activityViewModels()
  lateinit var adapter: RecentsAdapter
  private lateinit var call: DialerCall
  private var optionsAdapter: CallOptionsAdapter? = null
  private var toolbarListener: ToolbarListener? = null
  private var onStatusBarColorChange: OnStatusBarColorChange? = null
  private var _binding: FragmentCallDetailBinding? = null
  private val binding get() = _binding!!
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
    // TODO fetch data by id
    // val id = it.getInt(PARAM_CALL)
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentCallDetailBinding.inflate(
      inflater,
      container,
      false
    )
    return binding.root
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  @ExperimentalCoroutinesApi
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    onStatusBarColorChange?.onColorChange(view.context.getColor(R.color.colorPrimaryDark))
    toolbarListener?.hideToolbar(false)

    binding.toolbarLayout.toolbar.setNavigationOnClickListener { goBack() }
    context?.let { binding.toolbarLayout.toolbar.setTitle(R.string.call_details) }

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
    binding.contactIcon.setOnClickListener(this)

    context?.let { context ->
      viewModel.getDetailOptions(call)

      Picasso.get()
        .load(call.contactInfo.photoUri)
        .placeholder(context.getContactImagePlaceholder(call, generator))
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
    binding.recyclerViewCallDetailsOptions.adapter = optionsAdapter
  }

  private fun handleOptions(options: List<CallOption>) {
    optionsAdapter?.setData(options)
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
    if (v?.id == binding.callButton.id) {
      activity?.let { makeCall() }
    } else if (v?.id == binding.contactIcon.id) {
      activity?.let { viewModel.openContact(it, call) }
    }
  }

  private fun goBack() {
    findNavController().popBackStack()
  }
}