package dev.alenajam.opendialer.features.dialer.calls

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dev.alenajam.opendialer.R
import dev.alenajam.opendialer.features.dialer.DialerViewModel
import dev.alenajam.opendialer.features.dialer.calls.cache.CacheRepositoryImpl
import kotlinx.android.synthetic.main.fragment_recents.buttonPermission
import kotlinx.android.synthetic.main.fragment_recents.permissionPrompt
import kotlinx.android.synthetic.main.fragment_recents.recycler_view_call_log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

class RecentsFragment : Fragment() {
  @Inject
  lateinit var viewModelFactory: ViewModelProvider.Factory

  @Inject
  lateinit var cacheRepository: CacheRepositoryImpl

  private val viewModel by activityViewModels<DialerViewModel> { viewModelFactory }

  lateinit var adapter: RecentsAdapter

  private var notCalledNumber = ""
  private var refreshNeeded = false

  @ExperimentalCoroutinesApi
  private val requestPermissions =
    registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { data ->
      /** Ensure that all permissions were allowed */
      if (dev.alenajam.opendialer.util.PermissionUtils.recentsPermissions.all { data[it] == true }) {
        /** Hide permission prompt */
        permissionPrompt.visibility = View.GONE

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
  ): View? {
    return inflater.inflate(R.layout.fragment_recents, container, false)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (activity?.application as? dev.alenajam.opendialer.App)?.applicationComponent?.inject(this)
  }

  override fun onStart() {
    super.onStart()
    viewModel.startCache()
  }

  override fun onStop() {
    super.onStop()
    viewModel.stopCache()
  }

  @ExperimentalCoroutinesApi
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    context?.let {
      adapter = RecentsAdapter(
        it,
        recycler_view_call_log,
        coroutineScope = lifecycleScope,
        onCallClick = { call -> call.contactInfo.number?.let { num -> makeCall(num) } },
        onContactClick = { call -> openContact(call) },
        onOptionClick = { call, option -> activity?.handleOptionClick(call, option) },
        updateContactInfo = viewModel::updateContactInfo
      )
      recycler_view_call_log.adapter = adapter
      recycler_view_call_log.layoutManager = LinearLayoutManager(context)
    }

    if (dev.alenajam.opendialer.util.PermissionUtils.hasRecentsPermission(context)) {
      observeCalls()
    } else {
      /** Show permission prompt */
      permissionPrompt.visibility = View.VISIBLE
      buttonPermission.setOnClickListener {
        requestPermissions.launch(dev.alenajam.opendialer.util.PermissionUtils.recentsPermissions)
      }
    }

    if (dev.alenajam.opendialer.util.PermissionUtils.hasContactsPermission(context)) {
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
      viewModel.calls.observe(viewLifecycleOwner, Observer {
        handleCalls(it)
        refreshNeeded = true
      })
    }
  }

  @ExperimentalCoroutinesApi
  private fun observeContacts() {
    viewModel.contacts.observe(viewLifecycleOwner, Observer {
      refreshNeeded = true
    })
  }

  private fun makeCall(number: String) {
    if (dev.alenajam.opendialer.util.PermissionUtils.hasMakeCallPermission(context)) {
      activity?.let { viewModel.makeCall(it, number) }
      notCalledNumber = ""
    } else {
      notCalledNumber = number
      requestMakeCallPermissions.launch(dev.alenajam.opendialer.util.PermissionUtils.makeCallPermissions)
    }
  }

  private fun openContact(call: DialerCall) = activity?.let { viewModel.openContact(it, call) }

  private fun Activity.handleOptionClick(call: DialerCall, option: dev.alenajam.opendialer.model.CallOption) =
    when (option.id) {
      dev.alenajam.opendialer.model.CallOption.ID_SEND_MESSAGE -> viewModel.sendMessage(this, call)
      dev.alenajam.opendialer.model.CallOption.ID_CALL_DETAILS -> viewModel.callDetail(findNavController(), call)
      dev.alenajam.opendialer.model.CallOption.ID_CREATE_CONTACT -> viewModel.createContact(this, call)
      dev.alenajam.opendialer.model.CallOption.ID_ADD_EXISTING -> viewModel.addToContact(this, call)
      else -> Unit
    }

  private fun handleCalls(calls: List<DialerCall>) {
    adapter.setData(calls)
  }

  private fun refreshData() {
    if (refreshNeeded) {
      refreshNeeded = false
      viewModel.invalidateCache()
      adapter.notifyDataSetChanged()
    }
  }
}