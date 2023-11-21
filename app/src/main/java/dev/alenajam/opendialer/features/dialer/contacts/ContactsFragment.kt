package dev.alenajam.opendialer.features.dialer.contacts

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
import androidx.recyclerview.widget.LinearLayoutManager
import dev.alenajam.opendialer.R
import dev.alenajam.opendialer.adapter.ContactAdapter
import dev.alenajam.opendialer.features.dialer.DialerViewModel
import kotlinx.android.synthetic.main.fragment_contacts.buttonPermission
import kotlinx.android.synthetic.main.fragment_contacts.permissionPrompt
import kotlinx.android.synthetic.main.fragment_contacts.recycler_view_contacts
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

class ContactsFragment : Fragment() {
  @Inject
  lateinit var viewModelFactory: ViewModelProvider.Factory

  private val viewModel by activityViewModels<DialerViewModel> { viewModelFactory }

  lateinit var adapter: ContactAdapter

  @ExperimentalCoroutinesApi
  private val requestPermissions =
    registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { data ->
      /** Ensure that all permissions were allowed */
      if (dev.alenajam.opendialer.util.PermissionUtils.contactsPermissions.all { data[it] == true }) {
        /** Hide permission promp */
        permissionPrompt.visibility = View.GONE

        observeContacts()
      }
    }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_contacts, container, false)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (activity?.application as? dev.alenajam.opendialer.App)?.applicationComponent?.inject(this)
  }

  @ExperimentalCoroutinesApi
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    adapter = ContactAdapter(activity)
    recycler_view_contacts.adapter = adapter
    recycler_view_contacts.layoutManager = LinearLayoutManager(context)

    if (dev.alenajam.opendialer.util.PermissionUtils.hasContactsPermission(context)) {
      observeContacts()
    } else {
      /** Show permission prompt */
      permissionPrompt.visibility = View.VISIBLE
      buttonPermission.setOnClickListener {
        requestPermissions.launch(dev.alenajam.opendialer.util.PermissionUtils.contactsPermissions)
      }
    }
  }

  @ExperimentalCoroutinesApi
  private fun observeContacts() {
    viewModel.contacts.observe(viewLifecycleOwner, Observer { handleContacts(it) })
  }

  private fun handleContacts(list: List<DialerContact>) {
    adapter.contacts = ArrayList(list)
  }
}