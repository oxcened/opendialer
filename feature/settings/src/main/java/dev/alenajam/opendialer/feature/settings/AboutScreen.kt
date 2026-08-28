package dev.alenajam.opendialer.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.alenajam.opendialer.core.common.copy
import dev.alenajam.opendialer.core.common.ui.AppIcon
import dev.alenajam.opendialer.core.common.ui.LocalAppIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val appName = stringResource(R.string.app_name)
    val openDialerName = stringResource(R.string.opendialer_name)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.screen_about_title))
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack
                    ) {
                        AppIcon(LocalAppIcons.current.arrowLeft, contentDescription = null)
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(
                    innerPadding.copy(
                        top = innerPadding.calculateTopPadding() + 16.dp,
                        start = 16.dp,
                        end = 16.dp
                    )
                )
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = appName,
                style = MaterialTheme.typography.headlineMedium
            )

            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

            Text(
                text = stringResource(R.string.version, packageInfo.versionName.orEmpty()),
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = packageInfo.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                modifier = Modifier.padding(top = 12.dp),
                text = stringResource(R.string.about_section_developer),
                style = MaterialTheme.typography.titleMedium
            )

            Column {
                AboutLinkCard(
                    title = stringResource(R.string.developer_credit),
                    description = stringResource(
                        R.string.developer_website,
                        stringResource(R.string.developer_domain)
                    ),
                    uri = stringResource(R.string.url_developer),
                    roundTop = true,
                    roundBottom = true
                )
            }

            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = stringResource(R.string.about_section_community),
                style = MaterialTheme.typography.titleMedium
            )

            Column {
                val additionalTranslationProjectTitle =
                    stringResource(R.string.additional_translation_project_title)
                val additionalTranslationProjectDescription =
                    stringResource(R.string.contribute_translations_description, appName)
                val additionalTranslationProjectUri =
                    stringResource(R.string.url_additional_translation_project)

                AboutLinkCard(
                    title = stringResource(R.string.join_discord),
                    description = stringResource(R.string.discord_description),
                    uri = stringResource(R.string.url_discord),
                    roundTop = true,
                    roundBottom = false
                )

                if (additionalTranslationProjectTitle.isNotBlank() &&
                    additionalTranslationProjectDescription.isNotBlank() &&
                    additionalTranslationProjectUri.isNotBlank()
                ) {
                    AboutLinkCard(
                        title = additionalTranslationProjectTitle,
                        description = additionalTranslationProjectDescription,
                        uri = additionalTranslationProjectUri,
                        roundTop = false,
                        roundBottom = false
                    )
                }

                AboutLinkCard(
                    title = stringResource(R.string.contribute_translations),
                    description = stringResource(
                        R.string.contribute_translations_description,
                        openDialerName
                    ),
                    uri = stringResource(R.string.url_crowdin_opendialer),
                    roundTop = false,
                    roundBottom = false
                )

                AboutLinkCard(
                    title = stringResource(R.string.feature_requests),
                    description = stringResource(R.string.feature_requests_description, appName),
                    uri = stringResource(R.string.url_feature_requests),
                    roundTop = false,
                    roundBottom = true
                )
            }

            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = stringResource(R.string.about_section_open_source),
                style = MaterialTheme.typography.titleMedium
            )

            Column {
                AboutLinkCard(
                    title = stringResource(R.string.view_source_code),
                    description = stringResource(R.string.open_source_description, appName),
                    uri = stringResource(R.string.url_github_opendialer),
                    roundTop = true,
                    roundBottom = false
                )

                AboutLinkCard(
                    title = stringResource(R.string.report_an_issue),
                    description = stringResource(R.string.issues_description),
                    uri = stringResource(R.string.url_github_issues),
                    roundTop = false,
                    roundBottom = false
                )

                AboutLinkCard(
                    title = stringResource(R.string.contact_developer),
                    description = stringResource(R.string.contact_email),
                    uri = stringResource(R.string.url_contact_email),
                    roundTop = false,
                    roundBottom = true
                )
            }

            val fontName = stringResource(R.string.font_name)
            val fontAuthor = stringResource(R.string.font_author)
            val fontUri = stringResource(R.string.url_font)
            if (fontName.isNotBlank() && fontAuthor.isNotBlank()) {
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = stringResource(R.string.about_section_credits),
                    style = MaterialTheme.typography.titleMedium
                )
                if (fontUri.isBlank()) {
                    AboutInfoCard(
                        title = stringResource(R.string.typography_credit_title),
                        description = stringResource(R.string.font_credit_format, fontName, fontAuthor),
                        roundTop = true,
                        roundBottom = true
                    )
                } else {
                    AboutLinkCard(
                        title = stringResource(R.string.typography_credit_title),
                        description = stringResource(R.string.font_credit_format, fontName, fontAuthor),
                        uri = fontUri,
                        roundTop = true,
                        roundBottom = true
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutInfoCard(
    title: String,
    description: String,
    roundTop: Boolean,
    roundBottom: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
        shape = groupedCardShape(roundTop, roundBottom),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        AboutCardContent(title = title, description = description)
    }
}

@Composable
private fun AboutLinkCard(
    title: String,
    description: String,
    uri: String,
    roundTop: Boolean,
    roundBottom: Boolean
) {
    val uriHandler = LocalUriHandler.current

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
        onClick = { uriHandler.openUri(uri) },
        shape = groupedCardShape(roundTop, roundBottom),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        AboutCardContent(title = title, description = description)
    }
}

private fun groupedCardShape(roundTop: Boolean, roundBottom: Boolean) = RoundedCornerShape(
    topStart = if (roundTop) 20.dp else 2.dp,
    topEnd = if (roundTop) 20.dp else 2.dp,
    bottomStart = if (roundBottom) 20.dp else 2.dp,
    bottomEnd = if (roundBottom) 20.dp else 2.dp
)

@Composable
private fun AboutCardContent(
    title: String,
    description: String
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
