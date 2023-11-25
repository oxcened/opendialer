package dev.alenajam.opendialer.features.dialer.searchContacts

import android.content.Context
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dev.alenajam.opendialer.R
import dev.alenajam.opendialer.databinding.FragmentSearchContactBinding
import dev.alenajam.opendialer.features.dialer.searchContacts.SearchContactsAdapter.Item
import dev.alenajam.opendialer.model.BackPressedListener
import dev.alenajam.opendialer.model.KeyboardSearchListener
import dev.alenajam.opendialer.model.OnStatusBarColorChange
import dev.alenajam.opendialer.model.OpenSearchListener
import dev.alenajam.opendialer.model.SearchListener
import dev.alenajam.opendialer.model.SearchOpenChangeListener
import dev.alenajam.opendialer.model.ToolbarListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

private const val PARAM_INITIATION_TYPE = "initiationType"
private const val PARAM_PREFILLED_NUMBER = "prefilledNumber"

class SearchContactsFragment : Fragment(), BackPressedListener, SearchListener,
  SearchOpenChangeListener, View.OnTouchListener {
  @Inject
  lateinit var viewModelFactory: ViewModelProvider.Factory
  private val viewModel by viewModels<SearchContactsViewModel> { viewModelFactory }
  lateinit var adapter: SearchContactsAdapter
  lateinit var initiationType: InitiationType
  var prefilledNumber: String? = null
  private var toolbarListener: ToolbarListener? = null
  private var openSearchListener: OpenSearchListener? = null
  private var keyboardSearchListener: KeyboardSearchListener? = null
  private var onStatusBarColorChange: OnStatusBarColorChange? = null
  private var notExecutedQuery = ""
  private var notCalledNumber = ""
  private var _binding: FragmentSearchContactBinding? = null
  private val binding get() = _binding!!

  @ExperimentalCoroutinesApi
  private val requestPermissions =
    registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { data ->
      /** Ensure that all permissions were allowed */
      if (dev.alenajam.opendialer.util.PermissionUtils.searchPermissions.all { data[it] == true }) {
        /** Hide permission prompt */
        binding.permissionPrompt.visibility = View.GONE

        /** Retry query, if necessary */
        if (notExecutedQuery.isNotBlank()) {
          search(notExecutedQuery)
        }
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

  companion object {
    fun newInstance(initiationType: InitiationType) = SearchContactsFragment().apply {
      arguments = Bundle().apply {
        putSerializable(PARAM_INITIATION_TYPE, initiationType)
      }
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (activity?.application as? dev.alenajam.opendialer.App)?.applicationComponent?.inject(this)
    if (context is ToolbarListener) {
      toolbarListener = context
    }
    if (context is OpenSearchListener) {
      openSearchListener = context
    }
    if (context is KeyboardSearchListener) {
      keyboardSearchListener = context
    }
    if (context is OnStatusBarColorChange) {
      onStatusBarColorChange = context
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    arguments?.let {
      initiationType = it.getSerializable(PARAM_INITIATION_TYPE) as InitiationType
      prefilledNumber = it.getString(PARAM_PREFILLED_NUMBER)
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View {
    _binding = FragmentSearchContactBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  override fun onStart() {
    super.onStart()
    binding.dialpadLayout.dialpad.init()
  }

  override fun onStop() {
    super.onStop()
    binding.dialpadLayout.dialpad.tearDown()
  }

  @ExperimentalCoroutinesApi
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    onStatusBarColorChange?.onColorChange(view.context.getColor(R.color.windowBackground))

    adapter = SearchContactsAdapter { item ->
      activity?.let {
        if (item is Item.Contact) {
          makeCall(item.value.number)
        } else if (item is Item.Action) {
          when (item.type) {
            Item.Action.ActionType.CREATE_NEW_CONTACT -> viewModel.createContact(
              it, adapter.query
            )

            Item.Action.ActionType.ADD_TO_CONTACT -> viewModel.addToContact(
              it, adapter.query
            )

            Item.Action.ActionType.SEND_MESSAGE -> viewModel.sendMessage(
              it, adapter.query
            )

            Item.Action.ActionType.MAKE_CALL -> viewModel.makeCall(it, adapter.query)
          }
        }
      }
    }
    binding.recyclerView.adapter = adapter
    binding.recyclerView.layoutManager = LinearLayoutManager(context)

    viewModel.result.observe(viewLifecycleOwner, { handleResult(it) })

    if (!dev.alenajam.opendialer.util.PermissionUtils.hasSearchPermission(context)) {
      binding.placeholder.text = getString(R.string.placeholder_search_permissions)
      binding.permissionPrompt.visibility = View.VISIBLE
      binding.buttonPermission.setOnClickListener {
        requestPermissions.launch(dev.alenajam.opendialer.util.PermissionUtils.searchPermissions)
      }
    }

    if (!isRegularSearch()) {
      context?.getColor(R.color.windowBackground)
        ?.let { binding.toolbarLayout.toolbar.setBackgroundColor(it) }
      binding.toolbarLayout.toolbar.setNavigationOnClickListener { goBack() }
      binding.toolbarLayout.toolbar.visibility = View.VISIBLE

      toolbarListener?.hideToolbar(false)

      binding.dialpadLayout.dialpad.apply {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.dialpadLayout.dialpad)

        setTextChangeListener {
          if (isRegularSearch()) return@setTextChangeListener

          /** Dialpad search */
          val normalized =
            dev.alenajam.opendialer.util.smartDialUtils.SmartDialNameMatcher.normalizeNumber(
              context, it
            )
          search(normalized)
        }

        setCallListener {
          if (it.isNotEmpty()) {
            makeCall(it)
          }
        }

        if (!prefilledNumber.isNullOrBlank()) {
          text = prefilledNumber
        }

        open()
      }

      binding.fab.visibility = View.VISIBLE
    } else {
      binding.toolbarLayout.toolbar.visibility = View.GONE
    }

    binding.fab.setOnClickListener { binding.dialpadLayout.dialpad.open() }
  }

  private fun handleResult(result: SearchContactsViewModel.Result) {
    val data: MutableList<Item?> = mutableListOf()

    data.add(getHeader(result.contacts))
    data.addAll(getContacts(result.contacts))
    data.addAll(getActions(result.query))

    adapter.setData(data.filterNotNull())
    adapter.query = result.query
  }

  private fun getHeader(list: List<DialerSearchContact>): Item? {
    return if (list.isNotEmpty()) {
      return Item.Header(getString(R.string.searchContactsHeader))
    } else null
  }

  private fun getContacts(list: List<DialerSearchContact>): List<Item> {
    return list.map { Item.Contact(it) }
  }

  private fun getActions(query: String): List<Item> {
    val isDialableNumber = PhoneNumberUtils.isGlobalPhoneNumber(query)

    val list: MutableList<Item.Action.ActionType> = mutableListOf()

    if (query.isNotBlank() && query.length > 1 && isDialableNumber) {
      if (isRegularSearch()) {
        list.add(Item.Action.ActionType.MAKE_CALL)
      } else {
        list.add(Item.Action.ActionType.CREATE_NEW_CONTACT)
        list.add(Item.Action.ActionType.ADD_TO_CONTACT)
      }
      list.add(Item.Action.ActionType.SEND_MESSAGE)
    }

    return list.map { Item.Action(it) }
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

  private fun search(query: String) {
    if (dev.alenajam.opendialer.util.PermissionUtils.hasSearchPermission(context)) {
      if (isRegularSearch()) {
        /** Regular */
        viewModel.searchContacts(query)
      } else {
        /** Dialpad */
        viewModel.searchContactsDialpad(query)
      }
      notExecutedQuery = ""
    } else {
      notExecutedQuery = query
    }
  }

  override fun onBackPressed(): Boolean {
    if (isRegularSearch()) {
      /** Close search */
      openSearchListener?.closeSearch()
    } else if (binding.dialpadLayout.dialpad.text.isBlank() || binding.dialpadLayout.dialpad.isClosed) {
      /** Close search */
      return true
    } else {
      /** Close dialpad */
      binding.dialpadLayout.dialpad.close()
    }

    return false
  }

  override fun onSearch(query: String) {
    if (!isRegularSearch()) return

    /** Regular search */
    search(query)
  }

  override fun onOpenChange(isOpen: Boolean) {
    if (!isOpen) {
      goBack()
    }
  }

  private fun goBack() = findNavController().popBackStack()

  override fun onTouch(v: View?, event: MotionEvent?): Boolean {
    if (event?.action == MotionEvent.ACTION_UP) {
      v?.performClick()
    }

    if (event?.action == MotionEvent.ACTION_DOWN) {
      if (isRegularSearch()) {
        /** Close keyboard */
        keyboardSearchListener?.closeSearchKeyboard()
      } else {
        /** Close dialpad */
        binding.dialpadLayout.dialpad.close()
      }
    }

    return false
  }

  private fun isRegularSearch(): Boolean = initiationType == InitiationType.REGULAR

  @Keep
  enum class InitiationType {
    REGULAR, DIALPAD
  }
}