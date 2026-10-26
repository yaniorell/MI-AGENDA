package com.miagenda.app.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.miagenda.app.R
import com.miagenda.app.domain.model.Category
import com.miagenda.app.ui.components.RoundedCard

@Composable
fun CategoryManagerScreen(
    state: CategoryManagerUiState,
    onNameChange: (String) -> Unit,
    onColorChange: (Int) -> Unit,
    onSave: () -> Unit,
    onArchive: (Category, Boolean) -> Unit,
    onDelete: (Category) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pendingDelete by rememberSaveable { mutableStateOf(-1L) }
    val categoryToDelete = state.categories.firstOrNull { it.id == pendingDelete }
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
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
        Text(
            text = stringResource(R.string.categories_title),
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .semantics { heading() }
        )
        Text(
            text = stringResource(R.string.categories_add),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        OutlinedTextField(
            value = state.name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.categories_name)) },
            supportingText = if (state.nameError) {
                { Text(stringResource(R.string.categories_empty_name)) }
            } else {
                null
            },
            isError = state.nameError,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categoryPalette.forEach { color ->
                val selected = state.colorArgb == color
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (selected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
                        .clickable { onColorChange(color) },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(color), CircleShape)
                    )
                }
            }
        }
        Button(
            onClick = onSave,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .heightIn(min = 48.dp)
        ) {
            Text(stringResource(R.string.categories_save))
        }
        state.categories.forEach { category ->
            RoundedCard(modifier = Modifier.padding(horizontal = 8.dp)) {
                Text(text = category.name, style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .toggleable(
                            value = category.isArchived,
                            role = Role.Switch,
                            onValueChange = { onArchive(category, it) }
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.categories_archive))
                    Switch(checked = category.isArchived, onCheckedChange = null)
                }
                TextButton(onClick = { pendingDelete = category.id }) {
                    Text(stringResource(R.string.categories_delete))
                }
            }
        }
    }
    if (categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = { pendingDelete = -1L },
            title = { Text(stringResource(R.string.categories_delete_title)) },
            text = { Text(stringResource(R.string.categories_delete_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(categoryToDelete)
                        pendingDelete = -1L
                    }
                ) {
                    Text(stringResource(R.string.action_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = -1L }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}
