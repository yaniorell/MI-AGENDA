package com.miagenda.app.ui.reflection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.miagenda.app.R

@Composable
fun ReflectionScreen(
    state: ReflectionUiState,
    onNoteChange: (String) -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onSave: () -> Unit,
    onOpenWeek: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            text = stringResource(R.string.reflection_title),
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .semantics { heading() }
        )
        Text(
            text = state.dateLabel,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onPreviousDay, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.reflection_previous))
            }
            TextButton(
                onClick = onNextDay,
                enabled = state.canGoNext,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(stringResource(R.string.reflection_next))
            }
        }
        OutlinedTextField(
            value = state.note,
            onValueChange = onNoteChange,
            label = { Text(stringResource(R.string.reflection_field)) },
            supportingText = { Text(stringResource(R.string.reflection_support)) },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            minLines = 5,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )
        Text(
            text = stringResource(R.string.reflection_discard_day),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        if (state.saveError) {
            Text(
                text = stringResource(R.string.error_save),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        if (state.saved) {
            Text(
                text = stringResource(R.string.reflection_saved),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        Button(
            onClick = onSave,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .heightIn(min = 48.dp)
        ) {
            Text(stringResource(R.string.reflection_save))
        }
        TextButton(onClick = onOpenWeek, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(stringResource(R.string.week_open))
        }
    }
}
