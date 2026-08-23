package dev.alenajam.opendialer.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.alenajam.opendialer.core.common.ui.InCallUI
import dev.alenajam.opendialer.feature.inCall.ui.DefaultInCallUI
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UIModule {
    @Binds
    @Singleton
    abstract fun bindInCallUI(impl: DefaultInCallUI): InCallUI
}
