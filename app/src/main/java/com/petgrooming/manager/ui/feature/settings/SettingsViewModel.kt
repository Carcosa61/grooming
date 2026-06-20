package com.petgrooming.manager.ui.feature.settings

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.petgrooming.manager.R
import com.petgrooming.manager.data.backup.DriveBackupFile
import com.petgrooming.manager.data.backup.DriveBackupRepository
import com.petgrooming.manager.data.backup.GoogleAuthManager
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
    val accountEmail: String? = null,
    val lastBackupMillis: Long = 0L,
    val isBackingUp: Boolean = false,
    val isRestoring: Boolean = false,
    val isLoadingBackups: Boolean = false,
    val backups: List<DriveBackupFile> = emptyList(),
    val messageResId: Int? = null,
    val restoreComplete: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authManager: GoogleAuthManager,
    private val backupRepository: DriveBackupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _languageChanged = MutableStateFlow(false)
    val languageChanged: StateFlow<Boolean> = _languageChanged.asStateFlow()

    private val _backupState = MutableStateFlow(BackupUiState())
    val backupState: StateFlow<BackupUiState> = _backupState.asStateFlow()

    private var account: GoogleSignInAccount? = null

    init {
        loadSettings()
        observeLastBackup()
        restoreSession()
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

    private fun restoreSession() {
        account = authManager.currentAccount()
        _backupState.update { it.copy(accountEmail = account?.email) }
        if (account != null) loadBackups()
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

    // --- Google Drive backup ---

    fun signInIntent(): Intent = authManager.signInIntent

    fun onAccountConnected(connectedAccount: GoogleSignInAccount) {
        account = connectedAccount
        _backupState.update { it.copy(accountEmail = connectedAccount.email) }
        loadBackups()
    }

    fun onSignInFailed() {
        _backupState.update { it.copy(messageResId = R.string.backup_sign_in_failed) }
    }

    fun signOut() {
        authManager.signOut()
        account = null
        _backupState.update {
            it.copy(accountEmail = null, backups = emptyList())
        }
    }

    fun backupNow() {
        val driveAccount = account?.account ?: return
        viewModelScope.launch {
            _backupState.update { it.copy(isBackingUp = true) }
            val result = backupRepository.backup(driveAccount)
            _backupState.update {
                it.copy(
                    isBackingUp = false,
                    messageResId = if (result.isSuccess) R.string.backup_success else R.string.backup_failed
                )
            }
            if (result.isSuccess) loadBackups()
        }
    }

    fun loadBackups() {
        val driveAccount = account?.account ?: return
        viewModelScope.launch {
            _backupState.update { it.copy(isLoadingBackups = true) }
            val result = backupRepository.listBackups(driveAccount)
            _backupState.update {
                it.copy(
                    isLoadingBackups = false,
                    backups = result.getOrDefault(emptyList()),
                    messageResId = if (result.isFailure) R.string.backup_failed else it.messageResId
                )
            }
        }
    }

    fun restore(fileId: String) {
        val driveAccount = account?.account ?: return
        viewModelScope.launch {
            _backupState.update { it.copy(isRestoring = true) }
            val result = backupRepository.restore(driveAccount, fileId)
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
