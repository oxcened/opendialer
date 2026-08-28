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

    fun getDismissedUpdateVersion(): String?

    fun setDismissedUpdateVersion(version: String)

    fun getUpdateLastCheckMillis(): Long

    fun setUpdateLastCheckMillis(timestampMillis: Long)

    fun getUpdateEtag(): String?

    fun setUpdateEtag(etag: String?)

    fun getCachedAvailableUpdate(): AppUpdate?

    fun setCachedAvailableUpdate(update: AppUpdate?)
}

class SharedPreferencesCallLogPreferences @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : CallLogPreferences {
    override fun isFavoritesExpanded(): Boolean =
        SharedPreferenceHelper.isCallLogFavoritesExpanded(context)

    override fun setFavoritesExpanded(expanded: Boolean) {
        SharedPreferenceHelper.setCallLogFavoritesExpanded(context, expanded)
    }

    override fun getDismissedUpdateVersion(): String? =
        SharedPreferenceHelper.getDismissedUpdateVersion(context)

    override fun setDismissedUpdateVersion(version: String) {
        SharedPreferenceHelper.setDismissedUpdateVersion(context, version)
    }

    override fun getUpdateLastCheckMillis(): Long =
        SharedPreferenceHelper.getUpdateLastCheckMillis(context)

    override fun setUpdateLastCheckMillis(timestampMillis: Long) {
        SharedPreferenceHelper.setUpdateLastCheckMillis(context, timestampMillis)
    }

    override fun getUpdateEtag(): String? = SharedPreferenceHelper.getUpdateEtag(context)

    override fun setUpdateEtag(etag: String?) {
        SharedPreferenceHelper.setUpdateEtag(context, etag)
    }

    override fun getCachedAvailableUpdate(): AppUpdate? {
        val version = SharedPreferenceHelper.getAvailableUpdateVersion(context) ?: return null
        val releaseUrl = SharedPreferenceHelper.getAvailableUpdateUrl(context) ?: return null
        return AppUpdate(version, releaseUrl)
    }

    override fun setCachedAvailableUpdate(update: AppUpdate?) {
        SharedPreferenceHelper.setAvailableUpdate(context, update?.version, update?.releaseUrl)
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

    @Binds
    @Singleton
    abstract fun bindUpdateChecker(
        updateChecker: GitHubUpdateChecker,
    ): UpdateChecker
}
