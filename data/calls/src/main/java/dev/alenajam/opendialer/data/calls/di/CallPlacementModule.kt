package dev.alenajam.opendialer.data.calls.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.alenajam.opendialer.core.common.telecom.CallPlacementRepository
import dev.alenajam.opendialer.data.calls.CallPlacementRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class CallPlacementModule {
    @Binds
    abstract fun bindCallPlacementRepository(
        repository: CallPlacementRepositoryImpl,
    ): CallPlacementRepository
}
