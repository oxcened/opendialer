package dev.alenajam.opendialer.data.voicemail.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.alenajam.opendialer.data.voicemail.VoicemailRepository
import dev.alenajam.opendialer.data.voicemail.VoicemailRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class VoicemailModule {
    @Binds
    @Singleton
    abstract fun bindVoicemailRepository(impl: VoicemailRepositoryImpl): VoicemailRepository
}
