package dev.alenajam.opendialer.features.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import dev.alenajam.opendialer.R
import dev.alenajam.opendialer.core.functional.safeNavigate
import dev.alenajam.opendialer.databinding.FragmentHomeBinding
import dev.alenajam.opendialer.features.dialer.calls.RecentsFragment
import dev.alenajam.opendialer.features.dialer.contacts.ContactsFragment
import dev.alenajam.opendialer.features.dialer.searchContacts.SearchContactsFragment
import dev.alenajam.opendialer.features.main.MainFragmentDirections.Companion.actionHomeFragmentToSearchContactsFragment
import dev.alenajam.opendialer.features.profile.ProfileFragment
import dev.alenajam.opendialer.model.OnStatusBarColorChange
import dev.alenajam.opendialer.model.SearchOpenChangeListener
import dev.alenajam.opendialer.model.ToolbarListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_home.bottomNavigation
import kotlinx.android.synthetic.main.fragment_home.fab
import kotlinx.android.synthetic.main.fragment_home.viewPager
import javax.inject.Inject

class MainFragment :
  Fragment(),
  View.OnClickListener,
  BottomNavigationView.OnNavigationItemSelectedListener,
  SearchOpenChangeListener {
  companion object {
    fun newInstance() = MainFragment()
  }

  @Inject
  lateinit var viewModelFactory: ViewModelProvider.Factory

  private lateinit var viewPagerAdapter: FragmentStateAdapterImpl

  private var toolbarListener: ToolbarListener? = null
  private var onStatusBarColorChange: OnStatusBarColorChange? = null

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context is ToolbarListener) {
      toolbarListener = context
    }
    if (context is OnStatusBarColorChange) {
      onStatusBarColorChange = context
    }

    (activity?.application as? dev.alenajam.opendialer.App)?.applicationComponent?.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return FragmentHomeBinding.inflate(inflater).root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    onStatusBarColorChange?.onColorChange(view.context.getColor(R.color.windowBackground))

    bottomNavigation.setOnNavigationItemSelectedListener(this)
    bottomNavigation.itemIconTintList = null

    fab.setOnClickListener(this)

    viewPagerAdapter = FragmentStateAdapterImpl(this)
    viewPager.apply {
      adapter = viewPagerAdapter
      isUserInputEnabled = false
      registerOnPageChangeCallback(OnPageChange())
    }
  }

  private fun setPage(fragment: Tab) {
    viewPager.setCurrentItem(fragment.ordinal, false)
  }

  override fun onClick(v: View?) {
    if (v?.id == fab.id) {
      val action =
        actionHomeFragmentToSearchContactsFragment(SearchContactsFragment.InitiationType.DIALPAD)
      findNavController().safeNavigate(action)
    }
  }

  override fun onNavigationItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.recents -> setPage(Tab.RECENTS)
      R.id.contacts -> setPage(Tab.CONTACTS)
      R.id.profile -> setPage(Tab.PROFILE)
      else -> return false
    }

    return true
  }

  override fun onOpenChange(isOpen: Boolean) {
    if (isOpen) {
      findNavController().safeNavigate(
        actionHomeFragmentToSearchContactsFragment(
          SearchContactsFragment.InitiationType.REGULAR
        )
      )
    }
  }

  private inner class OnPageChange : ViewPager2.OnPageChangeCallback() {
    override fun onPageSelected(position: Int) {
      /** Handle toolbar visibility */
      when (position) {
        Tab.RECENTS.ordinal,
        Tab.CONTACTS.ordinal -> toolbarListener?.showToolbar(false)

        else -> toolbarListener?.hideToolbar(false)
      }

      /** Handle fab visibility */
      fab.visibility = when (position) {
        Tab.RECENTS.ordinal,
        Tab.CONTACTS.ordinal -> View.VISIBLE

        else -> View.GONE
      }

      context?.getColor(R.color.windowBackground)?.let {
        onStatusBarColorChange?.onColorChange(it)
      }
    }
  }

  class FragmentStateAdapterImpl(fragment: MainFragment) : FragmentStateAdapter(fragment) {
    private val fragments: List<Tab> = listOf(
      Tab.RECENTS,
      Tab.CONTACTS,
      Tab.PROFILE
    )

    override fun createFragment(position: Int): Fragment {
      return when (position) {
        Tab.RECENTS.ordinal -> RecentsFragment()
        Tab.PROFILE.ordinal -> ProfileFragment()
        else -> ContactsFragment()
      }
    }

    override fun getItemCount(): Int = fragments.size
  }

  enum class Tab {
    RECENTS,
    CONTACTS,
    PROFILE
  }
}