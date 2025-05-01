package com.chemecador.secretaria.ui.view.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.chemecador.secretaria.R
import com.chemecador.secretaria.ui.viewmodel.settings.SettingsViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {

    val email by viewModel.userRepository.userEmail.collectAsState(initial = null)

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text(
            text = stringResource(R.string.label_email),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = email ?: stringResource(R.string.label_data_not_provided),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}