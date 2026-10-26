package com.miagenda.app.ui.theme

import android.app.Activity
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.miagenda.app.domain.model.AccentColor
import com.miagenda.app.domain.model.ThemeMode

@Immutable
data class MiAgendaExtraColors(
    val gradientTop: Color,
    val gradientMiddle: Color,
    val gradientBottom: Color,
    val hero: Color,
    val heroContent: Color,
    val heroMuted: Color,
    val accentCoral: Color,
    val accentTeal: Color,
    val accentYellow: Color,
    val accent: Color,
    val onAccent: Color,
    val accentDeep: Color
)

val DarkExtraColors = MiAgendaExtraColors(
    gradientTop = Night,
    gradientMiddle = NightElevated,
    gradientBottom = NightTealWash,
    hero = Cream,
    heroContent = Ink,
    heroMuted = MutedDay,
    accentCoral = Coral,
    accentTeal = Teal,
    accentYellow = Yellow,
    accent = Coral,
    onAccent = Ink,
    accentDeep = CoralDeep
)

val LightExtraColors = MiAgendaExtraColors(
    gradientTop = Paper,
    gradientMiddle = Cream,
    gradientBottom = LightTealWash,
    hero = Cream,
    heroContent = Ink,
    heroMuted = MutedDay,
    accentCoral = CoralDay,
    accentTeal = TealDay,
    accentYellow = Yellow,
    accent = CoralDay,
    onAccent = Color.White,
    accentDeep = Color(0xFFB83B5C)
)

val LocalMiAgendaColors = staticCompositionLocalOf { DarkExtraColors }
val LocalReduceMotion = staticCompositionLocalOf { false }

private val DarkColorScheme = darkColorScheme(
    primary = Lilac,
    onPrimary = Ink,
    primaryContainer = Violet,
    onPrimaryContainer = TextLight,
    secondary = Teal,
    onSecondary = Color(0xFF062830),
    tertiary = Coral,
    onTertiary = Ink,
    background = Night,
    onBackground = TextLight,
    surface = SurfaceNight,
    onSurface = TextLight,
    surfaceVariant = SurfaceNightRaised,
    onSurfaceVariant = TextMuted,
    outline = OutlineNight,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val LightColorScheme = lightColorScheme(
    primary = VioletDay,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE4D9FF),
    onPrimaryContainer = Ink,
    secondary = TealDay,
    onSecondary = Color.White,
    tertiary = CoralDay,
    onTertiary = Color.White,
    background = Paper,
    onBackground = InkDay,
    surface = Color.White,
    onSurface = InkDay,
    surfaceVariant = Cream,
    onSurfaceVariant = MutedDay,
    outline = OutlineDay,
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

@Composable
fun MiAgendaTheme(
    themeMode: ThemeMode,
    accentColor: AccentColor = AccentColor.CORAL,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(activity.window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }
    val reduceMotion = rememberReduceMotion()
    val palette = accentColor.palette(darkTheme)
    val extraColors = (if (darkTheme) DarkExtraColors else LightExtraColors).copy(
        accent = palette.primary,
        onAccent = palette.onPrimary,
        accentDeep = palette.deep
    )
    val colorScheme = (if (darkTheme) DarkColorScheme else LightColorScheme).copy(
        primary = palette.primary,
        onPrimary = palette.onPrimary,
        primaryContainer = palette.container,
        onPrimaryContainer = palette.onContainer
    )
    CompositionLocalProvider(
        LocalMiAgendaColors provides extraColors,
        LocalReduceMotion provides reduceMotion
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = miAgendaTypography(),
            shapes = MiAgendaShapes,
            content = content
        )
    }
}

@Composable
private fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return androidx.compose.runtime.remember {
        runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            ) == 0f
        }.getOrDefault(false)
    }
}

@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = LocalMiAgendaColors.current
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    colors.gradientTop,
                    colors.gradientMiddle,
                    colors.gradientBottom
                )
            )
        ),
        content = content
    )
}
