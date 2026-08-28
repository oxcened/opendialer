package dev.alenajam.opendialer.feature.calls

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.alenajam.opendialer.core.common.SharedPreferenceHelper
import javax.inject.Inject
import javax.inject.Singleton

interface CallLogPreferences {
    fun isFavoritesExpanded(): Boolean

    fun setFavoritesExpanded(expanded: Boolean)
}

class SharedPreferencesCallLogPreferences @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : CallLogPreferences {
    override fun isFavoritesExpanded(): Boolean =
        SharedPreferenceHelper.isCallLogFavoritesExpanded(context)

    override fun setFavoritesExpanded(expanded: Boolean) {
        SharedPreferenceHelper.setCallLogFavoritesExpanded(context, expanded)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class CallLogPreferencesModule {
    @Binds
    @Singleton
    abstract fun bindCallLogPreferences(
        preferences: SharedPreferencesCallLogPreferences,
    ): CallLogPreferences
}
