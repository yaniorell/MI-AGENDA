package com.miagenda.app.domain.repository

import com.miagenda.app.domain.backup.AgendaSnapshot

interface AgendaBackupStore {
    suspend fun read(): AgendaSnapshot

    suspend fun replace(snapshot: AgendaSnapshot)
}
