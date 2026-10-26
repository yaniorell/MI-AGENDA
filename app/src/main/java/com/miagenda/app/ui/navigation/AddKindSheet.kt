package com.miagenda.app.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.miagenda.app.R
import com.miagenda.app.ui.theme.LocalReduceMotion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddKindSheet(
    onDismiss: () -> Unit,
    onPick: (String) -> Unit
) {
    if (LocalReduceMotion.current) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = stringResource(R.string.add_sheet_title)) },
            text = {
                AddKindOptions(onPick = onPick)
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.add_close))
                }
            }
        )
    } else {
        ModalBottomSheet(onDismissRequest = onDismiss) {
            Column(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.add_sheet_title),
                    style = MaterialTheme.typography.headlineSmall
                )
                AddKindOptions(onPick = onPick)
            }
        }
    }
}

@Composable
private fun AddKindOptions(onPick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AddOption(
            label = stringResource(R.string.add_class),
            icon = Icons.Rounded.School,
            onClick = { onPick(EditorKind.CLASS) }
        )
        AddOption(
            label = stringResource(R.string.add_activity),
            icon = Icons.Rounded.Event,
            onClick = { onPick(EditorKind.ACTIVITY) }
        )
        AddOption(
            label = stringResource(R.string.add_task),
            icon = Icons.Rounded.TaskAlt,
            onClick = { onPick(EditorKind.TASK) }
        )
        AddOption(
            label = stringResource(R.string.add_habit),
            icon = Icons.Rounded.Spa,
            onClick = { onPick(EditorKind.HABIT) }
        )
    }
}

@Composable
private fun AddOption(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Text(
            text = label,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
