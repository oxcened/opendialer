package dev.alenajam.opendialer.core.common.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.alenajam.opendialer.core.common.DefaultPhoneManager
import dev.alenajam.opendialer.core.common.DefaultPhoneManagerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CommonModule {
    @Provides
    @Singleton
    fun provideDefaultPhoneManager(@ApplicationContext context: Context): DefaultPhoneManager {
        return DefaultPhoneManagerImpl(context)
    }
}
