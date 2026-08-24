package dev.alenajam.opendialer.data.calls.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.alenajam.opendialer.data.calls.CallsRepository
import dev.alenajam.opendialer.data.calls.CallsRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CallsModule {
    @Binds
    @Singleton
    abstract fun bindCallsRepository(
        callsRepositoryImpl: CallsRepositoryImpl
    ): CallsRepository
}
