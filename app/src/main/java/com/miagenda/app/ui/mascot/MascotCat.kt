package com.miagenda.app.ui.mascot

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.miagenda.app.R
import com.miagenda.app.domain.mascot.MascotStage
import com.miagenda.app.ui.components.RoundedCard
import com.miagenda.app.ui.theme.LocalReduceMotion

val LocalMascot = staticCompositionLocalOf { MascotUiState() }

private val CatBody = Color(0xFF7C5CBF)
private val CatBelly = Color(0xFFFFF4E8)
private val CatEar = Color(0xFFFF829F)
private val CatInk = Color(0xFF2A1430)
private val CatStar = Color(0xFFE0B83A)
private val CatBook = Color(0xFF5B3E9E)

@Composable
fun MascotCat(
    stage: MascotStage,
    modifier: Modifier = Modifier,
    contentDescription: String = stringResource(stage.descriptionRes())
) {
    val reduceMotion = LocalReduceMotion.current
    val bob = if (reduceMotion || stage == MascotStage.SLEEPING) {
        0f
    } else {
        val transition = rememberInfiniteTransition(label = "luma")
        val shift by transition.animateFloat(
            initialValue = 0f,
            targetValue = -6f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "luma-bob"
        )
        shift
    }
    Canvas(
        modifier = modifier
            .graphicsLayer { translationY = bob }
            .semantics { this.contentDescription = contentDescription }
    ) {
        val width = size.width
        val height = size.height
        fun px(fraction: Float) = width * fraction
        fun py(fraction: Float) = height * fraction
        drawOval(
            color = CatBody,
            topLeft = Offset(px(0.62f), py(0.58f)),
            size = Size(width * 0.28f, height * 0.16f)
        )
        drawOval(
            color = CatBody,
            topLeft = Offset(px(0.16f), py(0.50f)),
            size = Size(width * 0.66f, height * 0.40f)
        )
        drawOval(
            color = CatBelly,
            topLeft = Offset(px(0.30f), py(0.64f)),
            size = Size(width * 0.38f, height * 0.20f)
        )
        val leftEar = Path().apply {
            moveTo(px(0.28f), py(0.36f))
            lineTo(px(0.36f), py(0.12f))
            lineTo(px(0.48f), py(0.34f))
            close()
        }
        val rightEar = Path().apply {
            moveTo(px(0.52f), py(0.34f))
            lineTo(px(0.64f), py(0.12f))
            lineTo(px(0.72f), py(0.36f))
            close()
        }
        drawPath(leftEar, CatBody)
        drawPath(rightEar, CatBody)
        drawCircle(color = CatBody, radius = width * 0.24f, center = Offset(px(0.50f), py(0.42f)))
        drawCircle(color = CatEar, radius = width * 0.055f, center = Offset(px(0.34f), py(0.48f)))
        drawCircle(color = CatEar, radius = width * 0.055f, center = Offset(px(0.66f), py(0.48f)))
        if (stage == MascotStage.SLEEPING) {
            val eye = Size(width * 0.12f, height * 0.08f)
            drawArc(
                color = CatInk,
                startAngle = 200f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(px(0.30f), py(0.36f)),
                size = eye,
                style = Stroke(width = width * 0.025f)
            )
            drawArc(
                color = CatInk,
                startAngle = 200f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(px(0.54f), py(0.36f)),
                size = eye,
                style = Stroke(width = width * 0.025f)
            )
        } else {
            drawCircle(color = CatInk, radius = width * 0.045f, center = Offset(px(0.40f), py(0.40f)))
            drawCircle(color = CatInk, radius = width * 0.045f, center = Offset(px(0.60f), py(0.40f)))
            drawCircle(color = Color.White, radius = width * 0.016f, center = Offset(px(0.414f), py(0.388f)))
            drawCircle(color = Color.White, radius = width * 0.016f, center = Offset(px(0.614f), py(0.388f)))
        }
        drawCircle(color = CatEar, radius = width * 0.022f, center = Offset(px(0.50f), py(0.48f)))
        if (stage == MascotStage.STUDYING) {
            drawRoundRect(
                color = CatBook,
                topLeft = Offset(px(0.62f), py(0.62f)),
                size = Size(width * 0.24f, height * 0.16f),
                cornerRadius = CornerRadius(width * 0.03f)
            )
            drawRoundRect(
                color = CatBelly,
                topLeft = Offset(px(0.66f), py(0.65f)),
                size = Size(width * 0.16f, height * 0.10f),
                cornerRadius = CornerRadius(width * 0.02f)
            )
        }
        if (stage == MascotStage.SPECIAL) {
            val star = Path().apply {
                moveTo(px(0.78f), py(0.16f))
                lineTo(px(0.82f), py(0.28f))
                lineTo(px(0.94f), py(0.30f))
                lineTo(px(0.84f), py(0.38f))
                lineTo(px(0.88f), py(0.50f))
                lineTo(px(0.78f), py(0.42f))
                lineTo(px(0.68f), py(0.50f))
                lineTo(px(0.72f), py(0.38f))
                lineTo(px(0.62f), py(0.30f))
                lineTo(px(0.74f), py(0.28f))
                close()
            }
            drawPath(star, CatStar)
        }
    }
}

@Composable
fun MascotCard(stage: MascotStage, modifier: Modifier = Modifier) {
    RoundedCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.heightIn(min = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MascotCat(stage = stage, modifier = Modifier.size(72.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.mascot_name),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(stage.messageRes()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun MascotStage.messageRes(): Int {
    return when (this) {
        MascotStage.SLEEPING -> R.string.mascot_sleeping
        MascotStage.AWAKE -> R.string.mascot_awake
        MascotStage.STUDYING -> R.string.mascot_studying
        MascotStage.SPECIAL -> R.string.mascot_special
    }
}

private fun MascotStage.descriptionRes(): Int {
    return when (this) {
        MascotStage.SLEEPING -> R.string.mascot_cd_sleeping
        MascotStage.AWAKE -> R.string.mascot_cd_awake
        MascotStage.STUDYING -> R.string.mascot_cd_studying
        MascotStage.SPECIAL -> R.string.mascot_cd_special
    }
}
