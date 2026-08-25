package dev.alenajam.opendialer.data.callsCache.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.alenajam.opendialer.data.callsCache.CacheRepository
import dev.alenajam.opendialer.data.callsCache.CacheRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CallsCacheModule {
    @Binds
    @Singleton
    abstract fun bindCacheRepository(impl: CacheRepositoryImpl): CacheRepository
}
