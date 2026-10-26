package com.miagenda.app.domain.model

enum class HomeSection {
    NEXT_ACTIVITY,
    DAY_PROGRESS,
    TODAY_AGENDA,
    TODAY_HABITS,
    REFLECTION,
    QUICK_LINKS;

    companion object {
        fun fromStorage(stored: String?): Set<HomeSection> {
            if (stored == null) {
                return entries.toSet()
            }
            if (stored.isBlank()) {
                return emptySet()
            }
            return stored.split(',')
                .mapNotNull { token -> entries.find { section -> section.name == token } }
                .toSet()
        }

        fun toStorage(sections: Set<HomeSection>): String {
            return entries.filter { section -> section in sections }
                .joinToString(separator = ",") { section -> section.name }
        }
    }
}
