package com.miagenda.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.miagenda.app.R
import com.miagenda.app.ui.theme.LocalMiAgendaColors

@Composable
fun CircularActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMiAgendaColors.current
    Box(
        modifier = modifier
            .size(64.dp)
            .shadow(elevation = 6.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(colors.accent, colors.accentDeep)))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = stringResource(R.string.cd_add),
            tint = colors.onAccent,
            modifier = Modifier.size(32.dp)
        )
    }
}
