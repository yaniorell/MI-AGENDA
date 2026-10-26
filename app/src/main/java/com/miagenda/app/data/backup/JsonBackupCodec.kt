package com.miagenda.app.data.backup

import com.miagenda.app.domain.backup.BackupCodec
import com.miagenda.app.domain.backup.BackupContents
import com.miagenda.app.domain.backup.BackupFormatException
import com.miagenda.app.domain.backup.BackupProblem
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

internal const val BACKUP_FORMAT = "mi-agenda-backup"

internal const val BACKUP_VERSION = 1

internal object JsonBackupCodec : BackupCodec {
    private val json = Json {
        ignoreUnknownKeys = false
        encodeDefaults = true
        explicitNulls = true
        prettyPrint = true
    }

    override fun encode(contents: BackupContents): String {
        return json.encodeToString(
            BackupDocument.serializer(),
            contents.checked().toDocument()
        )
    }

    override fun decode(text: String): BackupContents {
        val element = try {
            json.parseToJsonElement(text)
        } catch (error: SerializationException) {
            throw BackupFormatException(BackupProblem.UNREADABLE)
        } catch (error: IllegalArgumentException) {
            throw BackupFormatException(BackupProblem.UNREADABLE)
        }
        if (element !is JsonObject) {
            throw BackupFormatException(BackupProblem.UNREADABLE)
        }
        val format = element.stringOrNull("format")
        if (format == null) {
            throw BackupFormatException(BackupProblem.UNREADABLE)
        }
        if (format != BACKUP_FORMAT) {
            throw BackupFormatException(BackupProblem.WRONG_FORMAT)
        }
        val version = element.intOrNull("version")
            ?: throw BackupFormatException(BackupProblem.UNREADABLE)
        if (version != BACKUP_VERSION) {
            throw BackupFormatException(BackupProblem.UNSUPPORTED_VERSION)
        }
        val document = try {
            json.decodeFromJsonElement(BackupDocument.serializer(), element)
        } catch (error: SerializationException) {
            throw BackupFormatException(BackupProblem.UNREADABLE)
        }
        return document.toContents().checked()
    }
}

private fun JsonObject.stringOrNull(key: String): String? {
    val value = this[key] ?: return null
    return try {
        val primitive = value.jsonPrimitive
        if (primitive.isString) primitive.content else null
    } catch (error: IllegalArgumentException) {
        null
    }
}

private fun JsonObject.intOrNull(key: String): Int? {
    val value = this[key] ?: return null
    return try {
        val primitive = value.jsonPrimitive
        if (primitive.isString) null else primitive.intOrNull
    } catch (error: IllegalArgumentException) {
        null
    }
}
