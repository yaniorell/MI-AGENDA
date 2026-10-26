package com.miagenda.app.ui.backup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miagenda.app.data.backup.BackupFiles
import com.miagenda.app.domain.backup.BackupFormatException
import com.miagenda.app.domain.backup.BackupPreferencesException
import com.miagenda.app.domain.backup.BackupProblem
import com.miagenda.app.domain.home.DateProvider
import com.miagenda.app.domain.usecase.ExportBackup
import com.miagenda.app.domain.usecase.ImportBackup
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class BackupMessage {
    EXPORTED,
    RESTORED,
    UNREADABLE,
    WRONG_FORMAT,
    UNSUPPORTED_VERSION,
    INVALID,
    WRITE_FAILED,
    READ_FAILED,
    PREFERENCES_FAILED
}

data class BackupUiState(
    val busy: Boolean = false,
    val message: BackupMessage? = null
)

class BackupViewModel(
    private val exportBackup: ExportBackup,
    private val importBackup: ImportBackup,
    private val backupFiles: BackupFiles,
    private val dateProvider: DateProvider
) : ViewModel() {
    private val state = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = state.asStateFlow()

    fun suggestedFileName(): String = "mi-agenda-${dateProvider.today()}.json"

    fun exportTo(uri: Uri) {
        if (state.value.busy) {
            return
        }
        viewModelScope.launch {
            state.update { current -> current.copy(busy = true, message = null) }
            try {
                backupFiles.write(uri, exportBackup())
                state.update { current ->
                    current.copy(busy = false, message = BackupMessage.EXPORTED)
                }
            } catch (error: CancellationException) {
                state.update { current -> current.copy(busy = false) }
                throw error
            } catch (error: BackupFormatException) {
                state.update { current ->
                    current.copy(busy = false, message = error.problem.toMessage())
                }
            } catch (error: IOException) {
                state.update { current ->
                    current.copy(busy = false, message = BackupMessage.WRITE_FAILED)
                }
            }
        }
    }

    fun importFrom(uri: Uri) {
        if (state.value.busy) {
            return
        }
        viewModelScope.launch {
            state.update { current -> current.copy(busy = true, message = null) }
            try {
                importBackup(backupFiles.read(uri))
                state.update { current ->
                    current.copy(busy = false, message = BackupMessage.RESTORED)
                }
            } catch (error: CancellationException) {
                state.update { current -> current.copy(busy = false) }
                throw error
            } catch (error: BackupFormatException) {
                state.update { current ->
                    current.copy(busy = false, message = error.problem.toMessage())
                }
            } catch (error: BackupPreferencesException) {
                state.update { current ->
                    current.copy(busy = false, message = BackupMessage.PREFERENCES_FAILED)
                }
            } catch (error: IOException) {
                state.update { current ->
                    current.copy(busy = false, message = BackupMessage.READ_FAILED)
                }
            }
        }
    }

    private fun BackupProblem.toMessage(): BackupMessage {
        return when (this) {
            BackupProblem.UNREADABLE -> BackupMessage.UNREADABLE
            BackupProblem.WRONG_FORMAT -> BackupMessage.WRONG_FORMAT
            BackupProblem.UNSUPPORTED_VERSION -> BackupMessage.UNSUPPORTED_VERSION
            BackupProblem.INVALID -> BackupMessage.INVALID
        }
    }
}
