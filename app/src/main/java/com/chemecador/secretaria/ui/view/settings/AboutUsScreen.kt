package com.chemecador.secretaria.ui.view.settings


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chemecador.secretaria.R
import com.chemecador.secretaria.ui.view.components.SecretariaButton

@Composable
fun AboutUsScreen(
    onGithubClick: (String) -> Unit,
    onContactClick: () -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val linkColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Image(
            painter = painterResource(R.mipmap.ic_launcher),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier.size(100.dp)
        )

        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.what_is_secretaria_title),
            style = MaterialTheme.typography.bodyLarge,
            color = textColor
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.what_is_secretaria_description),
            style = MaterialTheme.typography.bodyMedium,
            color = textColor
        )

        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.label_source_code),
            style = MaterialTheme.typography.headlineSmall,
            color = textColor
        )

        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            val gitHubLink = stringResource(R.string.github_link)
            Image(
                painter = painterResource(R.drawable.ic_github),
                contentDescription = "GitHub",
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.github_link),
                style = MaterialTheme.typography.bodyMedium,
                color = linkColor,
                modifier = Modifier.clickable {
                    onGithubClick(gitHubLink)
                }
            )
        }

        Spacer(Modifier.height(32.dp))
        Text(
            text = stringResource(R.string.label_contact_us),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Spacer(Modifier.height(8.dp))
        SecretariaButton(
            onClick    = onContactClick,
            icon       = Icons.Default.Email,
            text       = stringResource(R.string.btn_contact_us),
            iconTint   = linkColor,
            borderColor = MaterialTheme.colorScheme.secondary
        )
    }
}

