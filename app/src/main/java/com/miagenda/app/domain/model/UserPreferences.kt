package com.miagenda.app.domain.model

data class UserPreferences(
    val displayName: String = "",
    val themeMode: ThemeMode = ThemeMode.DARK,
    val accentColor: AccentColor = AccentColor.CORAL,
    val visibleSections: Set<HomeSection> = HomeSection.entries.toSet(),
    val recoveredFromError: Boolean = false
)
