package com.miagenda.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.miagenda.app.domain.model.AccentColor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import org.junit.Assert.assertTrue
import org.junit.Test

class ContrastTest {
    @Test
    fun lasLetrasSeLeenSobreElFondo() {
        val darkBackgrounds = listOf(
            Night,
            NightElevated,
            NightTealWash,
            SurfaceNight,
            SurfaceNightRaised
        )
        val lightBackgrounds = listOf(Paper, Cream, Color.White, LightTealWash)
        AccentColor.entries.forEach { accent ->
            val dark = miAgendaColorScheme(darkTheme = true, accentColor = accent)
            val light = miAgendaColorScheme(darkTheme = false, accentColor = accent)
            assertText("${accent.name} oscuro sobre el fondo", dark.primary, darkBackgrounds)
            assertText("${accent.name} oscuro en el botón", dark.onPrimary, listOf(dark.primary))
            assertText(
                "${accent.name} oscuro sobre la pastilla",
                dark.onPrimaryContainer,
                listOf(dark.primaryContainer)
            )
            assertText("${accent.name} claro sobre el fondo", light.primary, lightBackgrounds)
            assertText("${accent.name} claro en el botón", light.onPrimary, listOf(light.primary))
            assertText(
                "${accent.name} claro sobre la pastilla",
                light.onPrimaryContainer,
                listOf(light.primaryContainer)
            )
        }
        val dark = miAgendaColorScheme(darkTheme = true, accentColor = AccentColor.CORAL)
        val light = miAgendaColorScheme(darkTheme = false, accentColor = AccentColor.CORAL)
        assertText("texto oscuro", dark.onSurface, darkBackgrounds)
        assertText("texto secundario oscuro", dark.onSurfaceVariant, darkBackgrounds)
        assertText("texto claro", light.onSurface, lightBackgrounds)
        assertText("texto secundario claro", light.onSurfaceVariant, lightBackgrounds)
        assertBoundary("borde oscuro", dark.outline, darkBackgrounds)
        assertBoundary("borde claro", light.outline, lightBackgrounds)
    }

    private fun assertText(label: String, foreground: Color, backgrounds: List<Color>) {
        backgrounds.forEach { background ->
            val ratio = contrast(foreground, background)
            assertTrue("$label contra $background quedó en $ratio", ratio >= 4.5)
        }
    }

    private fun assertBoundary(label: String, foreground: Color, backgrounds: List<Color>) {
        backgrounds.forEach { background ->
            val ratio = contrast(foreground, background)
            assertTrue("$label contra $background quedó en $ratio", ratio >= 3.0)
        }
    }

    private fun contrast(first: Color, second: Color): Double {
        val lighter = max(luminance(first), luminance(second))
        val darker = min(luminance(first), luminance(second))
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun luminance(color: Color): Double {
        fun channel(value: Float): Double {
            val encoded = value.toDouble()
            return if (encoded <= 0.04045) {
                encoded / 12.92
            } else {
                ((encoded + 0.055) / 1.055).pow(2.4)
            }
        }
        return 0.2126 * channel(color.red) + 0.7152 * channel(color.green) + 0.0722 * channel(color.blue)
    }
}
