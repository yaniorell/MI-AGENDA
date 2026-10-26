package com.miagenda.app.ui.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miagenda.app.R
import com.miagenda.app.ui.LocalViewModelFactory

@Composable
fun BackupSection(modifier: Modifier = Modifier) {
    val viewModel: BackupViewModel = viewModel(factory = LocalViewModelFactory.current)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmRestore by rememberSaveable { mutableStateOf(false) }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportTo(uri)
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importFrom(uri)
        }
    }

    if (confirmRestore) {
        AlertDialog(
            onDismissRequest = { confirmRestore = false },
            title = { Text(text = stringResource(R.string.backup_restore_title)) },
            text = { Text(text = stringResource(R.string.backup_restore_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmRestore = false
                        importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                    }
                ) {
                    Text(text = stringResource(R.string.backup_replace))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmRestore = false }) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            }
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.backup_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            text = stringResource(R.string.backup_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            onClick = { exportLauncher.launch(viewModel.suggestedFileName()) },
            enabled = !state.busy,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Text(text = stringResource(R.string.backup_export))
        }
        OutlinedButton(
            onClick = { confirmRestore = true },
            enabled = !state.busy,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Text(text = stringResource(R.string.backup_import))
        }
        val message = state.message
        if (message != null) {
            Text(
                text = message.label(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
            )
        }
    }
}

@Composable
private fun BackupMessage.label(): String {
    return stringResource(
        when (this) {
            BackupMessage.EXPORTED -> R.string.backup_exported
            BackupMessage.RESTORED -> R.string.backup_restored
            BackupMessage.UNREADABLE -> R.string.backup_unreadable
            BackupMessage.WRONG_FORMAT -> R.string.backup_format
            BackupMessage.UNSUPPORTED_VERSION -> R.string.backup_version
            BackupMessage.INVALID -> R.string.backup_invalid
            BackupMessage.WRITE_FAILED -> R.string.backup_write_failed
            BackupMessage.READ_FAILED -> R.string.backup_read_failed
            BackupMessage.PREFERENCES_FAILED -> R.string.backup_preferences_failed
        }
    )
}
