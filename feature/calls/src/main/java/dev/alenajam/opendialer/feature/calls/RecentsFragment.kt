package dev.alenajam.opendialer.feature.calls

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dev.alenajam.opendialer.core.common.PermissionUtils
import dev.alenajam.opendialer.data.calls.CallOption
import dev.alenajam.opendialer.data.calls.ContactInfo
import dev.alenajam.opendialer.data.calls.DialerCall
import dev.alenajam.opendialer.feature.calls.databinding.FragmentRecentsBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
class RecentsFragment : Fragment() {
  private val viewModel: DialerViewModel by activityViewModels()
  lateinit var adapter: RecentsAdapter
  private var notCalledNumber = ""
  private var refreshNeeded = false
  private var _binding: FragmentRecentsBinding? = null
  private val binding get() = _binding!!

  @ExperimentalCoroutinesApi
  private val requestPermissions =
    registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { data ->
      /** Ensure that all permissions were allowed */
      if (PermissionUtils.recentsPermissions.all { data[it] == true }) {
        /** Hide permission prompt */
        binding.permissionPrompt.visibility = View.GONE

        observeCalls()
      }
    }

  private val requestMakeCallPermissions =
    registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { data ->
      /** Ensure that all permissions were allowed */
      if (!data.containsValue(false)) {
        /** Retry call, if necessary */
        if (notCalledNumber.isNotBlank()) {
          makeCall(notCalledNumber)
        }
      }
    }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentRecentsBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  override fun onStart() {
    super.onStart()
    //viewModel.startCache()
  }

  override fun onStop() {
    super.onStop()
    //viewModel.stopCache()
  }

  @ExperimentalCoroutinesApi
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    context?.let {
      adapter = RecentsAdapter(
        it,
        binding.recyclerViewCallLog,
        coroutineScope = lifecycleScope,
        onCallClick = { call -> call.contactInfo.number?.let { num -> makeCall(num) } },
        onContactClick = { call -> openContact(call) },
        onOptionClick = { call, option -> activity?.handleOptionClick(call, option) },
        updateContactInfo = { s: String?, s1: String?, contactInfo: ContactInfo -> } // viewModel::updateContactInfo
      )
      binding.recyclerViewCallLog.adapter = adapter
      binding.recyclerViewCallLog.layoutManager = LinearLayoutManager(context)
    }

    if (PermissionUtils.hasRecentsPermission(context)) {
      observeCalls()
    } else {
      /** Show permission prompt */
      binding.permissionPrompt.visibility = View.VISIBLE
      binding.buttonPermission.setOnClickListener {
        requestPermissions.launch(PermissionUtils.recentsPermissions)
      }
    }

    if (PermissionUtils.hasContactsPermission(context)) {
      observeContacts()
    }
  }

  override fun onResume() {
    super.onResume()
    refreshData()
  }

  @ExperimentalCoroutinesApi
  private fun observeCalls() {
    /** Ensure that observable isn't observed already */
    if (!viewModel.calls.hasObservers()) {
      viewModel.calls.observe(viewLifecycleOwner) {
        handleCalls(it)
        refreshNeeded = true
      }
    }
  }

  @ExperimentalCoroutinesApi
  private fun observeContacts() {
//    viewModel.contacts.observe(viewLifecycleOwner, Observer {
//      refreshNeeded = true
//    })
  }

  private fun makeCall(number: String) {
    if (PermissionUtils.hasMakeCallPermission(context)) {
//      activity?.let { viewModel.makeCall(it, number) }
      notCalledNumber = ""
    } else {
      notCalledNumber = number
      requestMakeCallPermissions.launch(PermissionUtils.makeCallPermissions)
    }
  }

  private fun openContact(call: DialerCall) = activity?.let { /*viewModel.openContact(it, call)*/ }

  private fun Activity.handleOptionClick(
    call: DialerCall,
    option: CallOption
  ) = Unit
//    when (option.id) {
//      CallOption.ID_SEND_MESSAGE -> viewModel.sendMessage(this, call)
//      CallOption.ID_CALL_DETAILS -> viewModel.callDetail(
//        findNavController(),
//        call
//      )
//
//      CallOption.ID_CREATE_CONTACT -> viewModel.createContact(
//        this,
//        call
//      )
//
//      CallOption.ID_ADD_EXISTING -> viewModel.addToContact(this, call)
//      else -> Unit
//    }

  private fun handleCalls(calls: List<DialerCall>) {
    adapter.setData(calls)
  }

  private fun refreshData() {
    if (refreshNeeded) {
      refreshNeeded = false
      //viewModel.invalidateCache()
      adapter.notifyDataSetChanged()
    }
  }
}