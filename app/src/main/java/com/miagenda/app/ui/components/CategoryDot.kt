package com.miagenda.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CategoryDot(
    colorArgb: Int?,
    modifier: Modifier = Modifier
) {
    val color = colorArgb ?: return
    Box(
        modifier = modifier
            .size(16.dp)
            .background(color = Color(color), shape = CircleShape)
    )
}
