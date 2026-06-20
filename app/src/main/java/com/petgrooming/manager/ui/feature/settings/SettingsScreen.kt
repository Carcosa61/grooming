package com.petgrooming.manager.ui.feature.settings

import android.app.LocaleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.LocaleList
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.petgrooming.manager.BuildConfig
import com.petgrooming.manager.R
import com.petgrooming.manager.data.backup.DriveBackupFile
import com.petgrooming.manager.data.preferences.UserPreferencesRepository
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val languageChanged by viewModel.languageChanged.collectAsState()
    val backupState by viewModel.backupState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            viewModel.onAccountConnected(account)
        } catch (e: ApiException) {
            viewModel.onSignInFailed()
        }
    }

    LaunchedEffect(languageChanged) {
        if (languageChanged) {
            applyLanguage(context, uiState.currentLanguage)
            viewModel.resetLanguageChanged()
        }
    }

    val messageResId = backupState.messageResId
    LaunchedEffect(messageResId) {
        if (messageResId != null) {
            snackbarHostState.showSnackbar(context.getString(messageResId))
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cancel))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Language Section
            Text(
                text = stringResource(R.string.language_section),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                LanguageOption(
                    languageCode = UserPreferencesRepository.THAI,
                    languageName = "ไทย",
                    languageNativeName = "Thai",
                    isSelected = uiState.currentLanguage == UserPreferencesRepository.THAI,
                    onClick = { viewModel.setLanguage(UserPreferencesRepository.THAI) }
                )

                HorizontalDivider()

                LanguageOption(
                    languageCode = UserPreferencesRepository.ENGLISH,
                    languageName = "English",
                    languageNativeName = "English",
                    isSelected = uiState.currentLanguage == UserPreferencesRepository.ENGLISH,
                    onClick = { viewModel.setLanguage(UserPreferencesRepository.ENGLISH) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Info text
            Text(
                text = stringResource(R.string.language_restart_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Backup Section
            Text(
                text = stringResource(R.string.backup_settings),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )

            BackupSection(
                state = backupState,
                onConnect = { signInLauncher.launch(viewModel.signInIntent()) },
                onSignOut = viewModel::signOut,
                onBackupNow = viewModel::backupNow,
                onOpenRestore = viewModel::loadBackups,
                onRestore = viewModel::restore
            )

            Spacer(modifier = Modifier.height(24.dp))

            // About Section
            Text(
                text = stringResource(R.string.settings_about),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.app_name)) },
                    supportingContent = { Text(stringResource(R.string.settings_version, BuildConfig.VERSION_NAME)) },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (backupState.restoreComplete) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text(stringResource(R.string.restore_complete_title)) },
            text = { Text(stringResource(R.string.restore_complete_message)) },
            confirmButton = {
                Button(onClick = { restartApp(context) }) {
                    Text(stringResource(R.string.restore_restart))
                }
            }
        )
    }
}

@Composable
private fun BackupSection(
    state: BackupUiState,
    onConnect: () -> Unit,
    onSignOut: () -> Unit,
    onBackupNow: () -> Unit,
    onOpenRestore: () -> Unit,
    onRestore: (String) -> Unit
) {
    var showRestoreList by remember { mutableStateOf(false) }
    var pendingRestore by remember { mutableStateOf<DriveBackupFile?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        if (state.accountEmail == null) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.backup_drive_title)) },
                supportingContent = { Text(stringResource(R.string.backup_drive_subtitle)) },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
            Button(
                onClick = onConnect,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(stringResource(R.string.backup_connect_account))
            }
        } else {
            ListItem(
                headlineContent = { Text(state.accountEmail) },
                overlineContent = { Text(stringResource(R.string.backup_connected_account)) },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingContent = {
                    TextButton(onClick = onSignOut) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(stringResource(R.string.backup_sign_out))
                    }
                },
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            HorizontalDivider()

            val lastBackupText = if (state.lastBackupMillis > 0L) {
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                    .format(Date(state.lastBackupMillis))
            } else {
                stringResource(R.string.backup_last_never)
            }
            Text(
                text = stringResource(R.string.last_backup, lastBackupText),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onBackupNow,
                    enabled = !state.isBackingUp && !state.isRestoring,
                    modifier = Modifier.weight(1f)
                ) {
                    if (state.isBackingUp) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(stringResource(R.string.backup_in_progress))
                    } else {
                        Icon(
                            imageVector = Icons.Default.Backup,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(stringResource(R.string.backup_now))
                    }
                }
                OutlinedButton(
                    onClick = {
                        showRestoreList = true
                        onOpenRestore()
                    },
                    enabled = !state.isBackingUp && !state.isRestoring,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(stringResource(R.string.restore))
                }
            }
        }
    }

    if (showRestoreList) {
        RestoreListDialog(
            isLoading = state.isLoadingBackups,
            backups = state.backups,
            onSelect = {
                showRestoreList = false
                pendingRestore = it
            },
            onDismiss = { showRestoreList = false }
        )
    }

    pendingRestore?.let { backup ->
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            title = { Text(stringResource(R.string.restore_confirm_title)) },
            text = { Text(stringResource(R.string.restore_confirm_message)) },
            confirmButton = {
                Button(onClick = {
                    onRestore(backup.id)
                    pendingRestore = null
                }) {
                    Text(stringResource(R.string.restore))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestore = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (state.isRestoring) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = { },
            title = { Text(stringResource(R.string.restore_in_progress)) },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(stringResource(R.string.restore_in_progress))
                }
            }
        )
    }
}

@Composable
private fun RestoreListDialog(
    isLoading: Boolean,
    backups: List<DriveBackupFile>,
    onSelect: (DriveBackupFile) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.backup_select_title)) },
        text = {
            when {
                isLoading -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.size(12.dp))
                        Text(stringResource(R.string.backup_loading))
                    }
                }
                backups.isEmpty() -> {
                    Text(stringResource(R.string.backup_no_backups))
                }
                else -> {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        backups.forEach { backup ->
                            val subtitle = if (backup.createdAtMillis > 0L) {
                                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                                    .format(Date(backup.createdAtMillis))
                            } else {
                                backup.name
                            }
                            ListItem(
                                headlineContent = { Text(backup.name) },
                                supportingContent = { Text(subtitle) },
                                leadingContent = {
                                    Icon(
                                        imageVector = Icons.Default.Restore,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier.clickable { onSelect(backup) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun LanguageOption(
    languageCode: String,
    languageName: String,
    languageNativeName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(languageName) },
        supportingContent = { Text(languageNativeName) },
        leadingContent = {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary 
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

private fun applyLanguage(context: Context, languageCode: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.getSystemService(LocaleManager::class.java)
            .applicationLocales = LocaleList.forLanguageTags(languageCode)
    } else {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(languageCode)
        )
    }
}

private fun restartApp(context: Context) {
    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    context.startActivity(intent)
    Runtime.getRuntime().exit(0)
}
