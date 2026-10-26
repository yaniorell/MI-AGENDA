package com.miagenda.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.miagenda.app.domain.model.AccentColor

data class AccentPalette(
    val primary: Color,
    val onPrimary: Color,
    val container: Color,
    val onContainer: Color,
    val deep: Color
)

fun AccentColor.palette(darkTheme: Boolean): AccentPalette {
    return when (this) {
        AccentColor.CORAL -> if (darkTheme) {
            AccentPalette(
                primary = Coral,
                onPrimary = Ink,
                container = CoralDeep,
                onContainer = Ink,
                deep = CoralDeep
            )
        } else {
            AccentPalette(
                primary = CoralDay,
                onPrimary = Color.White,
                container = Color(0xFFFFD5E0),
                onContainer = Ink,
                deep = Color(0xFFB83B5C)
            )
        }
        AccentColor.TEAL -> if (darkTheme) {
            AccentPalette(
                primary = Teal,
                onPrimary = Color(0xFF062830),
                container = TealDay,
                onContainer = Color(0xFFE5F6F8),
                deep = TealDay
            )
        } else {
            AccentPalette(
                primary = TealDay,
                onPrimary = Color.White,
                container = Color(0xFFD4F4F8),
                onContainer = Ink,
                deep = Color(0xFF085863)
            )
        }
        AccentColor.VIOLET -> if (darkTheme) {
            AccentPalette(
                primary = Lilac,
                onPrimary = Ink,
                container = Violet,
                onContainer = TextLight,
                deep = Violet
            )
        } else {
            AccentPalette(
                primary = VioletDay,
                onPrimary = Color.White,
                container = Color(0xFFE4D9FF),
                onContainer = Ink,
                deep = Color(0xFF3E2870)
            )
        }
        AccentColor.YELLOW -> if (darkTheme) {
            AccentPalette(
                primary = Yellow,
                onPrimary = Ink,
                container = Color(0xFFE0B83A),
                onContainer = Ink,
                deep = Color(0xFFE0B83A)
            )
        } else {
            AccentPalette(
                primary = Color(0xFF8A6400),
                onPrimary = Color.White,
                container = Color(0xFFFFE8A3),
                onContainer = Ink,
                deep = Color(0xFF6E4E00)
            )
        }
    }
}
