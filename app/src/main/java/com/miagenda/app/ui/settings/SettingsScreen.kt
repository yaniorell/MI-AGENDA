package com.miagenda.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.miagenda.app.R
import com.miagenda.app.domain.model.AccentColor
import com.miagenda.app.domain.model.HomeSection
import com.miagenda.app.domain.model.ThemeMode
import com.miagenda.app.ui.theme.palette
import com.miagenda.app.domain.preferences.MAX_DISPLAY_NAME_LENGTH

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onNameEdited: () -> Unit,
    onSaveName: (String) -> Unit,
    onThemeSelected: (ThemeMode) -> Unit,
    onAccentSelected: (AccentColor) -> Unit,
    onSectionVisibleChange: (HomeSection, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    footer: @Composable () -> Unit = {}
) {
    var draft by rememberSaveable { mutableStateOf("") }
    var draftReady by rememberSaveable { mutableStateOf(false) }
    val shownName = if (draftReady) draft else state.savedName
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.semantics { heading() }
        )
        if (state.preferencesRecovered) {
            Text(
                text = stringResource(R.string.settings_recovered),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        OutlinedTextField(
            value = shownName,
            onValueChange = { incoming ->
                draftReady = true
                draft = incoming.replace("\n", "").take(MAX_DISPLAY_NAME_LENGTH)
                onNameEdited()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.settings_name_label)) },
            supportingText = { Text(text = stringResource(R.string.settings_name_support)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            isError = state.nameError == NameFieldError.TooLong
        )
        if (state.nameError == NameFieldError.TooLong) {
            Text(
                text = stringResource(R.string.settings_name_too_long),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Button(
            onClick = { onSaveName(shownName) },
            modifier = Modifier.heightIn(min = 48.dp)
        ) {
            Text(text = stringResource(R.string.settings_save_name))
        }
        if (state.showSaved) {
            Text(
                text = stringResource(R.string.settings_name_saved),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
            )
        }
        if (state.saveFailed) {
            Text(
                text = stringResource(R.string.settings_name_failed),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
            )
        }
        Text(
            text = stringResource(R.string.settings_theme_title),
            style = MaterialTheme.typography.titleMedium
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThemeChoice(
                label = stringResource(R.string.theme_dark),
                selected = state.themeMode == ThemeMode.DARK,
                onClick = { onThemeSelected(ThemeMode.DARK) }
            )
            ThemeChoice(
                label = stringResource(R.string.theme_light),
                selected = state.themeMode == ThemeMode.LIGHT,
                onClick = { onThemeSelected(ThemeMode.LIGHT) }
            )
            ThemeChoice(
                label = stringResource(R.string.theme_system),
                selected = state.themeMode == ThemeMode.SYSTEM,
                onClick = { onThemeSelected(ThemeMode.SYSTEM) }
            )
        }
        Text(
            text = stringResource(R.string.settings_accent_title),
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = stringResource(R.string.settings_accent_support),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
        AccentColor.entries.forEach { accent ->
            AccentChoice(
                label = stringResource(accent.labelRes()),
                color = accent.palette(darkTheme).primary,
                selected = state.accentColor == accent,
                onClick = { onAccentSelected(accent) }
            )
        }
        Text(
            text = stringResource(R.string.settings_sections_title),
            style = MaterialTheme.typography.titleMedium
        )
        SectionToggle(
            label = stringResource(R.string.section_next),
            checked = HomeSection.NEXT_ACTIVITY in state.visibleSections,
            onCheckedChange = { visible ->
                onSectionVisibleChange(HomeSection.NEXT_ACTIVITY, visible)
            }
        )
        SectionToggle(
            label = stringResource(R.string.section_progress),
            checked = HomeSection.DAY_PROGRESS in state.visibleSections,
            onCheckedChange = { visible ->
                onSectionVisibleChange(HomeSection.DAY_PROGRESS, visible)
            }
        )
        SectionToggle(
            label = stringResource(R.string.section_agenda),
            checked = HomeSection.TODAY_AGENDA in state.visibleSections,
            onCheckedChange = { visible ->
                onSectionVisibleChange(HomeSection.TODAY_AGENDA, visible)
            }
        )
        SectionToggle(
            label = stringResource(R.string.section_habits),
            checked = HomeSection.TODAY_HABITS in state.visibleSections,
            onCheckedChange = { visible ->
                onSectionVisibleChange(HomeSection.TODAY_HABITS, visible)
            }
        )
        SectionToggle(
            label = stringResource(R.string.section_reflection),
            checked = HomeSection.REFLECTION in state.visibleSections,
            onCheckedChange = { visible ->
                onSectionVisibleChange(HomeSection.REFLECTION, visible)
            }
        )
        SectionToggle(
            label = stringResource(R.string.section_links),
            checked = HomeSection.QUICK_LINKS in state.visibleSections,
            onCheckedChange = { visible ->
                onSectionVisibleChange(HomeSection.QUICK_LINKS, visible)
            }
        )
        footer()
    }
}

private fun AccentColor.labelRes(): Int {
    return when (this) {
        AccentColor.CORAL -> R.string.accent_coral
        AccentColor.TEAL -> R.string.accent_teal
        AccentColor.VIOLET -> R.string.accent_violet
        AccentColor.YELLOW -> R.string.accent_yellow
    }
}

@Composable
private fun ThemeChoice(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    if (selected) {
        Button(onClick = onClick, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(text = label)
        }
    } else {
        OutlinedButton(onClick = onClick, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(text = label)
        }
    }
}

@Composable
private fun AccentChoice(
    label: String,
    color: androidx.compose.ui.graphics.Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
private fun SectionToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = onCheckedChange
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(checked = checked, onCheckedChange = null)
    }
}
