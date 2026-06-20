package com.petgrooming.manager.ui.feature.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petgrooming.manager.R
import com.petgrooming.manager.data.backup.BackupRepository
import com.petgrooming.manager.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val currentLanguage: String = UserPreferencesRepository.THAI,
    val isLoading: Boolean = false
)

data class BackupUiState(
    val lastBackupMillis: Long = 0L,
    val isBackingUp: Boolean = false,
    val isRestoring: Boolean = false,
    val shareUri: Uri? = null,
    val messageResId: Int? = null,
    val restoreComplete: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val backupRepository: BackupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _languageChanged = MutableStateFlow(false)
    val languageChanged: StateFlow<Boolean> = _languageChanged.asStateFlow()

    private val _backupState = MutableStateFlow(BackupUiState())
    val backupState: StateFlow<BackupUiState> = _backupState.asStateFlow()

    init {
        loadSettings()
        observeLastBackup()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            userPreferencesRepository.languageFlow.collect { language ->
                _uiState.value = _uiState.value.copy(currentLanguage = language)
            }
        }
    }

    private fun observeLastBackup() {
        viewModelScope.launch {
            userPreferencesRepository.lastBackupFlow.collect { timestamp ->
                _backupState.update { it.copy(lastBackupMillis = timestamp) }
            }
        }
    }

    fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            userPreferencesRepository.setLanguage(languageCode)
            _languageChanged.value = true
        }
    }

    fun resetLanguageChanged() {
        _languageChanged.value = false
    }

    // --- Backup & restore via the system share sheet / file picker ---

    /** Creates a backup archive and, on success, emits a [BackupUiState.shareUri] to launch the share sheet. */
    fun backupNow() {
        if (_backupState.value.isBackingUp) return
        viewModelScope.launch {
            _backupState.update { it.copy(isBackingUp = true) }
            val result = backupRepository.createBackup()
            _backupState.update {
                it.copy(
                    isBackingUp = false,
                    shareUri = result.getOrNull(),
                    messageResId = if (result.isFailure) R.string.backup_failed else it.messageResId
                )
            }
        }
    }

    fun consumeShareUri() {
        _backupState.update { it.copy(shareUri = null) }
    }

    fun restoreFromUri(uri: Uri) {
        if (_backupState.value.isRestoring) return
        viewModelScope.launch {
            _backupState.update { it.copy(isRestoring = true) }
            val result = backupRepository.restoreFromUri(uri)
            _backupState.update {
                it.copy(
                    isRestoring = false,
                    restoreComplete = result.isSuccess,
                    messageResId = if (result.isSuccess) R.string.restore_success else R.string.restore_failed
                )
            }
        }
    }

    fun consumeMessage() {
        _backupState.update { it.copy(messageResId = null) }
    }
}
