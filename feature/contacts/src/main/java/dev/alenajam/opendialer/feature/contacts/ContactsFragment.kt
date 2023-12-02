package dev.alenajam.opendialer.feature.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dev.alenajam.opendialer.core.common.PermissionUtils
import dev.alenajam.opendialer.data.contacts.DialerContact
import dev.alenajam.opendialer.feature.contacts.databinding.FragmentContactsBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
class ContactsFragment : Fragment() {
  private val viewModel: DialerViewModel by viewModels()
  lateinit var adapter: ContactAdapter
  private var _binding: FragmentContactsBinding? = null
  private val binding get() = _binding!!

  @ExperimentalCoroutinesApi
  private val requestPermissions =
    registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { data ->
      /** Ensure that all permissions were allowed */
      if (PermissionUtils.contactsPermissions.all { data[it] == true }) {
        /** Hide permission promp */
        binding.permissionPrompt.visibility = View.GONE
        observeContacts()
      }
    }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentContactsBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  @ExperimentalCoroutinesApi
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    adapter = ContactAdapter(activity)
    binding.recyclerViewContacts.adapter = adapter
    binding.recyclerViewContacts.layoutManager = LinearLayoutManager(context)

    if (PermissionUtils.hasContactsPermission(context)) {
      observeContacts()
    } else {
      /** Show permission prompt */
      binding.permissionPrompt.visibility = View.VISIBLE
      binding.buttonPermission.setOnClickListener {
        requestPermissions.launch(PermissionUtils.contactsPermissions)
      }
    }
  }

  @ExperimentalCoroutinesApi
  private fun observeContacts() {
    viewModel.contacts.observe(viewLifecycleOwner) { handleContacts(it) }
  }

  private fun handleContacts(list: List<DialerContact>) {
    adapter.contacts = ArrayList(list)
  }
}