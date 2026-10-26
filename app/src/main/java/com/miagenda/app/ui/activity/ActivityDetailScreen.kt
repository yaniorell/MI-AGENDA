package com.miagenda.app.ui.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.miagenda.app.R
import com.miagenda.app.domain.model.ActivityStatus

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActivityDetailScreen(
    state: ActivityDetailUiState,
    onStatus: (ActivityStatus) -> Unit,
    onEdit: () -> Unit,
    onSkip: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var confirm by rememberSaveable { mutableStateOf("") }
    val item = state.item
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.cd_back)
            )
        }
        if (state.missing || item == null) {
            Text(
                text = stringResource(R.string.activity_missing),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            return@Column
        }
        Text(
            text = item.title,
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .semantics { heading() }
        )
        Text(
            text = item.timeLabel,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        if (item.categoryName != null) {
            Text(
                text = item.categoryName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        if (state.notes.isNotBlank()) {
            Text(
                text = state.notes,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActivityStatus.entries.forEach { status ->
                FilterChip(
                    selected = item.status == status,
                    onClick = { onStatus(status) },
                    label = { Text(statusLabel(status)) }
                )
            }
        }
        TextButton(
            onClick = onEdit,
            modifier = Modifier.heightIn(min = 48.dp)
        ) {
            Text(
                stringResource(
                    if (state.isRecurring) R.string.detail_edit else R.string.detail_edit_single
                )
            )
        }
        if (state.isRecurring) {
            TextButton(
                onClick = { confirm = "skip" },
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(stringResource(R.string.detail_skip))
            }
        }
        TextButton(
            onClick = { confirm = "delete" },
            modifier = Modifier.heightIn(min = 48.dp)
        ) {
            Text(
                stringResource(
                    if (state.isRecurring) R.string.detail_delete else R.string.detail_delete_single
                )
            )
        }
    }
    if (confirm.isNotEmpty()) {
        val recurring = state.isRecurring
        AlertDialog(
            onDismissRequest = { confirm = "" },
            title = {
                Text(
                    stringResource(
                        if (confirm == "skip") R.string.detail_skip_title else R.string.detail_delete_title
                    )
                )
            },
            text = {
                Text(
                    stringResource(
                        when {
                            confirm == "skip" -> R.string.detail_skip_body
                            recurring -> R.string.detail_delete_body
                            else -> R.string.detail_delete_one_body
                        }
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val action = confirm
                        confirm = ""
                        if (action == "skip") onSkip() else onDelete()
                    }
                ) {
                    Text(stringResource(R.string.action_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirm = "" }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}
