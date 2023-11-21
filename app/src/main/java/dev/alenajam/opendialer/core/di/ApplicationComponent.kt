package dev.alenajam.opendialer.core.di

import dev.alenajam.opendialer.core.di.viewmodel.ViewModelModule
import dev.alenajam.opendialer.features.dialer.calls.RecentsFragment
import dev.alenajam.opendialer.features.dialer.calls.detailCall.CallDetailFragment
import dev.alenajam.opendialer.features.dialer.contacts.ContactsFragment
import dev.alenajam.opendialer.features.dialer.searchContacts.SearchContactsFragment
import dev.alenajam.opendialer.features.inCall.InCallFragment
import dev.alenajam.opendialer.features.inCall.InCallManageConferenceFragment
import dev.alenajam.opendialer.features.main.MainFragment
import dev.alenajam.opendialer.features.profile.ProfileFragment
import dagger.Component
import javax.inject.Singleton

/**
 * Also called Injector, it creates the dependency graph which tells how dependencies must be injected
 * All activities and fragments which need injected dependencies must be added here
 */
@Singleton
@Component(modules = [ApplicationModule::class, ViewModelModule::class])
interface ApplicationComponent {
  fun inject(mainActivity: dev.alenajam.opendialer.features.main.MainActivity)
  fun inject(profileFragment: ProfileFragment)
  fun inject(inCallFragment: InCallFragment)
  fun inject(inCallServiceImpl: dev.alenajam.opendialer.service.InCallServiceImpl)
  fun inject(inCallManageConferenceFragment: InCallManageConferenceFragment)
  fun inject(recentsFragment: RecentsFragment)
  fun inject(contactsFragment: ContactsFragment)
  fun inject(callDetailFragment: CallDetailFragment)
  fun inject(searchContactsFragment: SearchContactsFragment)
  fun inject(settingsFragment: dev.alenajam.opendialer.activity.SettingsActivity.SettingsFragment)
  fun inject(mainFragment: MainFragment)
}