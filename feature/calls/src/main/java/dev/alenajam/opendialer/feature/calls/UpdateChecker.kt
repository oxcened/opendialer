package dev.alenajam.opendialer.feature.calls

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

data class AppUpdate(
    val version: String,
    val releaseUrl: String,
)

interface UpdateChecker {
    val bypassCache: Boolean

    suspend fun checkForUpdate(etag: String?): UpdateCheckResult
}

sealed interface UpdateCheckResult {
    data class Success(
        val update: AppUpdate?,
        val etag: String?,
    ) : UpdateCheckResult

    data object NotModified : UpdateCheckResult

    data object Failed : UpdateCheckResult
}

@Singleton
class GitHubUpdateChecker @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : UpdateChecker {
    override val bypassCache: Boolean =
        context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

    override suspend fun checkForUpdate(etag: String?): UpdateCheckResult {
        if (bypassCache) {
            return UpdateCheckResult.Success(
                update = AppUpdate(
                    version = context.getString(R.string.mock_update_version),
                    releaseUrl = context.getString(R.string.github_releases_url),
                ),
                etag = null,
            )
        }

        return checkGitHubForUpdate(etag)
    }

    private suspend fun checkGitHubForUpdate(etag: String?): UpdateCheckResult = withContext(Dispatchers.IO) {
        val installedVersion = context.packageManager
            .getPackageInfo(context.packageName, 0)
            .versionName
            ?.toSemanticVersionOrNull(allowSuffix = true)
            ?: return@withContext UpdateCheckResult.Failed

        runCatching {
            val connection = (URL(context.getString(R.string.github_latest_release_api)).openConnection()
                as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = NETWORK_TIMEOUT_MILLIS
                readTimeout = NETWORK_TIMEOUT_MILLIS
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "OpenDialer")
                etag?.let { setRequestProperty("If-None-Match", it) }
            }

            try {
                if (connection.responseCode == HTTP_NOT_MODIFIED) {
                    return@runCatching UpdateCheckResult.NotModified
                }
                if (connection.responseCode !in HTTP_SUCCESS_RANGE) return@runCatching UpdateCheckResult.Failed

                val release = connection.inputStream.bufferedReader().use { reader ->
                    json.decodeFromString<GitHubRelease>(reader.readText())
                }
                val releaseVersion = release.tagName.toSemanticVersionOrNull()
                    ?: return@runCatching UpdateCheckResult.Failed
                val update = if (releaseVersion > installedVersion) {
                    AppUpdate(version = releaseVersion.toString(), releaseUrl = release.htmlUrl)
                } else {
                    null
                }

                UpdateCheckResult.Success(update, connection.getHeaderField("ETag"))
            } finally {
                connection.disconnect()
            }
        }.getOrDefault(UpdateCheckResult.Failed)
    }

    private companion object {
        const val NETWORK_TIMEOUT_MILLIS = 10_000
        const val HTTP_NOT_MODIFIED = 304
        val HTTP_SUCCESS_RANGE = 200..299
        val json = Json { ignoreUnknownKeys = true }
    }
}

@Serializable
private data class GitHubRelease(
    @SerialName("tag_name") val tagName: String,
    @SerialName("html_url") val htmlUrl: String,
)

internal data class SemanticVersion(
    val major: Int,
    val minor: Int,
    val patch: Int,
) : Comparable<SemanticVersion> {
    override fun compareTo(other: SemanticVersion): Int = compareValuesBy(
        this,
        other,
        SemanticVersion::major,
        SemanticVersion::minor,
        SemanticVersion::patch,
    )

    override fun toString(): String = "$major.$minor.$patch"
}

internal fun String.toSemanticVersionOrNull(allowSuffix: Boolean = false): SemanticVersion? {
    val version = removePrefix("v")
    if (!allowSuffix && '-' in version) return null
    val match = SEMANTIC_VERSION.matchEntire(version.substringBefore('-')) ?: return null
    return SemanticVersion(
        major = match.groupValues[1].toInt(),
        minor = match.groupValues[2].toInt(),
        patch = match.groupValues[3].toInt(),
    )
}

private val SEMANTIC_VERSION = Regex("(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)")
