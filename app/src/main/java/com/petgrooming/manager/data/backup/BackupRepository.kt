package com.petgrooming.manager.data.backup

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.petgrooming.manager.data.local.PetGroomingDatabase
import com.petgrooming.manager.data.preferences.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Creates and restores local backup archives. A backup is a single zip file
 * containing the SQLite database files and any pet/booking photos stored in the
 * app's `images` folder.
 *
 * Backups are written to the app cache and exposed through a [FileProvider] so the
 * user can share them anywhere (email, Google Drive, Files, messaging apps, …) via
 * the system share sheet. Restoring reads a zip the user picks from any of those
 * apps through the system file picker — no account or network access required.
 */
@Singleton
class BackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: PetGroomingDatabase,
    private val preferences: UserPreferencesRepository
) {
    /**
     * Builds a backup archive and returns a shareable [Uri] pointing at it.
     */
    suspend fun createBackup(): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            checkpointDatabase()
            val dir = File(context.cacheDir, BACKUP_DIR).apply { mkdirs() }
            // Keep only the latest archive around to avoid filling the cache.
            dir.listFiles()?.forEach { it.delete() }
            val archive = File(dir, "${APP_NAME_SLUG}-${timestamp()}.zip")
            createBackupZip(archive)
            preferences.setLastBackup(System.currentTimeMillis())
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archive)
        }
    }

    /**
     * Restores from a backup zip the user picked through the system file picker.
     * The database is closed and overwritten, so the caller must restart the
     * process afterwards.
     */
    suspend fun restoreFromUri(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val tempZip = File.createTempFile("restore", ".zip", context.cacheDir)
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(tempZip).use { output -> input.copyTo(output) }
                } ?: throw IllegalStateException("Cannot open the selected file")
                require(isValidBackup(tempZip)) { "The selected file is not a valid backup" }
                applyRestore(tempZip)
            } finally {
                tempZip.delete()
            }
        }
    }

    private fun checkpointDatabase() {
        runCatching {
            database.openHelper.writableDatabase
                .query("PRAGMA wal_checkpoint(TRUNCATE)")
                .use { it.moveToFirst() }
        }
    }

    private fun createBackupZip(target: File) {
        val dbFile = context.getDatabasePath(PetGroomingDatabase.DATABASE_NAME)
        val imagesDir = File(context.filesDir, IMAGES_DIR)
        ZipOutputStream(BufferedOutputStream(FileOutputStream(target))).use { zos ->
            listOf(dbFile, File(dbFile.path + "-wal"), File(dbFile.path + "-shm")).forEach { file ->
                if (file.exists()) addEntry(zos, file, "$DB_PREFIX${file.name}")
            }
            if (imagesDir.isDirectory) {
                imagesDir.listFiles()?.forEach { file ->
                    if (file.isFile) addEntry(zos, file, "$PHOTO_PREFIX${file.name}")
                }
            }
        }
    }

    private fun addEntry(zos: ZipOutputStream, file: File, entryName: String) {
        zos.putNextEntry(ZipEntry(entryName))
        FileInputStream(file).use { it.copyTo(zos) }
        zos.closeEntry()
    }

    /** A valid backup must contain at least one database entry. */
    private fun isValidBackup(zipFile: File): Boolean {
        ZipInputStream(FileInputStream(zipFile)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (entry.name.startsWith(DB_PREFIX)) return true
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        return false
    }

    /**
     * Closes the database and overwrites the live database and photo files with the
     * contents of [zipFile]. The caller must restart the process afterwards because
     * the singleton [PetGroomingDatabase] instance is no longer usable.
     */
    private fun applyRestore(zipFile: File) {
        val dbFile = context.getDatabasePath(PetGroomingDatabase.DATABASE_NAME)
        val databasesDir = dbFile.parentFile
        val imagesDir = File(context.filesDir, IMAGES_DIR)

        database.close()
        listOf(dbFile, File(dbFile.path + "-wal"), File(dbFile.path + "-shm")).forEach { it.delete() }

        ZipInputStream(FileInputStream(zipFile)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    val target = when {
                        entry.name.startsWith(DB_PREFIX) ->
                            databasesDir?.let { File(it, entry.name.removePrefix(DB_PREFIX)) }
                        entry.name.startsWith(PHOTO_PREFIX) ->
                            File(imagesDir, entry.name.removePrefix(PHOTO_PREFIX))
                        else -> null
                    }
                    if (target != null) {
                        target.parentFile?.mkdirs()
                        FileOutputStream(target).use { zis.copyTo(it) }
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }

    private fun timestamp(): String =
        SimpleDateFormat("yyyy-MM-dd-HHmm", Locale.US).format(Date())

    companion object {
        private const val BACKUP_DIR = "backups"
        private const val APP_NAME_SLUG = "PetGroomingManager-backup"
        private const val IMAGES_DIR = "images"
        private const val DB_PREFIX = "database/"
        private const val PHOTO_PREFIX = "photos/"
    }
}
