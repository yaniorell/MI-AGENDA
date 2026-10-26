package com.miagenda.app.domain.backup

enum class BackupProblem {
    UNREADABLE,
    WRONG_FORMAT,
    UNSUPPORTED_VERSION,
    INVALID
}

class BackupFormatException(
    val problem: BackupProblem
) : IllegalArgumentException(problem.name)

/**
 * La agenda ya se sustituyó y el archivo de ajustes no se pudo escribir.
 * El nombre, el tema, el color y las secciones siguen como estaban.
 */
class BackupPreferencesException : IllegalStateException(
    "Preferences were not saved after the agenda was replaced"
)

interface BackupCodec {
    fun encode(contents: BackupContents): String

    fun decode(text: String): BackupContents
}
