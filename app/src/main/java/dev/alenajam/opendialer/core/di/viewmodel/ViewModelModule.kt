package dev.alenajam.opendialer.core.di.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.alenajam.opendialer.features.dialer.DialerViewModel
import dev.alenajam.opendialer.features.dialer.searchContacts.SearchContactsViewModel
import dev.alenajam.opendialer.features.inCall.InCallViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Modules for [ViewModelFactory]
 * All of the app's [ViewModel]s must be added here
 */
@Module
abstract class ViewModelModule {
  @Binds
  internal abstract fun bindViewModelFactory(viewModelFactory: ViewModelFactory): ViewModelProvider.Factory

  @Binds
  @IntoMap
  @ViewModelKey(InCallViewModel::class)
  abstract fun bindInCallViewModel(inCallViewModel: InCallViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(DialerViewModel::class)
  abstract fun bindDialerViewModel(dialerViewModel: DialerViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(SearchContactsViewModel::class)
  abstract fun bindSearchContactsViewModel(searchContactsViewModel: SearchContactsViewModel): ViewModel
}