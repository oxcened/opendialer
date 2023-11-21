package dev.alenajam.opendialer.core.di

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ApplicationModule(private val app: dev.alenajam.opendialer.App) {
  @Provides
  @Singleton
  fun provideApplication(): dev.alenajam.opendialer.App = app
}