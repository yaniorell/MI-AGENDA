package com.miagenda.app.domain.usecase

import com.miagenda.app.domain.backup.BackupCodec
import com.miagenda.app.domain.backup.BackupContents
import com.miagenda.app.domain.backup.BackupPreferencesException
import com.miagenda.app.domain.repository.AgendaBackupStore
import com.miagenda.app.domain.repository.UserPreferencesRepository
import com.miagenda.app.domain.time.TimeProvider
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.first

class ExportBackup(
    private val store: AgendaBackupStore,
    private val preferences: UserPreferencesRepository,
    private val codec: BackupCodec,
    private val timeProvider: TimeProvider
) {
    suspend operator fun invoke(): String {
        return codec.encode(
            BackupContents(
                exportedAt = timeProvider.now(),
                preferences = preferences.preferences.first(),
                snapshot = store.read()
            )
        )
    }
}

class ImportBackup(
    private val store: AgendaBackupStore,
    private val preferences: UserPreferencesRepository,
    private val codec: BackupCodec
) {
    suspend operator fun invoke(json: String) {
        val contents = codec.decode(json)
        store.replace(contents.snapshot)
        try {
            preferences.restore(contents.preferences)
        } catch (error: CancellationException) {
            throw error
        } catch (error: IOException) {
            throw BackupPreferencesException()
        }
    }
}
