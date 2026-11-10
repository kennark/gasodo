package com.gasodoapp.gasodo.feature.about

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.gasodoapp.gasodo.BuildConfig
import com.gasodoapp.gasodo.R
import com.gasodoapp.gasodo.ui.components.MediumLabelText
import com.gasodoapp.gasodo.ui.components.TopBarScaffold
import com.gasodoapp.gasodo.ui.icons.arrow_back

private val LOGO_CANVAS_SIZE = 131.dp

private val LOGO_CIRCLE_SIZE = 88.dp

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    TopBarScaffold(
        title = stringResource(R.string.about_title),
        applyBottomPadding = true,
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = arrow_back,
                    contentDescription = stringResource(R.string.cd_back)
                )
            }
        }
    ) { innerPadding ->
        AboutContent(
            modifier = Modifier.padding(innerPadding),
            onOpenLink = { url -> context.openLink(url) }
        )
    }
}

@Composable
private fun AboutContent(
    onOpenLink: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val appName = stringResource(R.string.app_name)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppHeader(appName = appName)

        AboutCard(title = stringResource(R.string.about_app_info)) {
            InfoRow(
                label = stringResource(R.string.about_developer),
                value = stringResource(R.string.about_developer_name)
            )
            HorizontalDivider()
            InfoRow(
                label = stringResource(R.string.about_version),
                value = BuildConfig.VERSION_NAME
            )
            HorizontalDivider()
            InfoRow(
                label = stringResource(R.string.about_package),
                value = BuildConfig.APPLICATION_ID
            )
            HorizontalDivider()
            InfoRow(
                label = stringResource(R.string.about_license),
                value = stringResource(R.string.about_license_name)
            )
        }

        AboutCard(title = stringResource(R.string.about_links)) {
            val repositoryUrl = stringResource(R.string.about_repository_url)
            FilledTonalButton(
                onClick = { onOpenLink(repositoryUrl) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.about_source_code))
            }
            Text(
                text = repositoryUrl,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        PrivacyCard()

        Spacer(modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun AppHeader(appName: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 24.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = colorResource(R.color.launcher_icon_background),
            modifier = Modifier.size(LOGO_CIRCLE_SIZE)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(R.drawable.launcher_icon_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(LOGO_CANVAS_SIZE),
                    contentScale = ContentScale.None
                )
            }
        }
        Text(
            text = appName,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.about_version_value, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AboutCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MediumLabelText(label)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Composable
private fun PrivacyCard() {
    AboutCard(title = stringResource(R.string.about_privacy)) {
        Text(
            text = stringResource(R.string.about_privacy_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Opens [url] in a browser, ignoring failures if no activity can handle the intent.
 */
private fun Context.openLink(url: String) {
    runCatching {
        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }
}

@Preview(showBackground = true)
@Composable
private fun AboutContentPreview() {
    AboutContent(onOpenLink = {})
}
