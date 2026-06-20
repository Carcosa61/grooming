package com.petgrooming.manager.data.backup

import android.accounts.Account
import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
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
import com.google.api.services.drive.model.File as DriveFile

/**
 * Creates, lists and restores backup archives stored in Google Drive under
 * `PetGroomingManager/Backups/`. Each archive is a zip containing the SQLite
 * database files and any pet/booking photos stored in the app's `images` folder.
 *
 * All Drive operations run on [Dispatchers.IO] and return a [Result].
 */
@Singleton
class DriveBackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: PetGroomingDatabase,
    private val preferences: UserPreferencesRepository
) {
    private fun driveService(account: Account): Drive {
        val credential = GoogleAccountCredential
            .usingOAuth2(context, listOf(DriveScopes.DRIVE_FILE))
            .also { it.selectedAccount = account }
        return Drive.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance(), credential)
            .setApplicationName(APP_NAME)
            .build()
    }

    suspend fun backup(account: Account): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val service = driveService(account)
            checkpointDatabase()
            val archive = createBackupZip()
            try {
                val appFolderId = ensureFolder(service, APP_FOLDER, null)
                val backupsFolderId = ensureFolder(service, BACKUPS_FOLDER, appFolderId)
                val name = "${timestamp()}-backup.zip"
                upload(service, archive, name, backupsFolderId)
                preferences.setLastBackup(System.currentTimeMillis())
                name
            } finally {
                archive.delete()
            }
        }
    }

    suspend fun listBackups(account: Account): Result<List<DriveBackupFile>> = withContext(Dispatchers.IO) {
        runCatching {
            val service = driveService(account)
            val appFolderId = findFolder(service, APP_FOLDER, null)
                ?: return@runCatching emptyList()
            val backupsFolderId = findFolder(service, BACKUPS_FOLDER, appFolderId)
                ?: return@runCatching emptyList()
            service.files().list()
                .setQ("'$backupsFolderId' in parents and trashed=false and mimeType='$MIME_ZIP'")
                .setOrderBy("createdTime desc")
                .setSpaces("drive")
                .setFields("files(id,name,createdTime,size)")
                .execute()
                .files
                .orEmpty()
                .map { file ->
                    DriveBackupFile(
                        id = file.id,
                        name = file.name,
                        createdAtMillis = file.createdTime?.value ?: 0L,
                        sizeBytes = file.getSize() ?: 0L
                    )
                }
        }
    }

    suspend fun restore(account: Account, fileId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val service = driveService(account)
            val tempZip = File.createTempFile("restore", ".zip", context.cacheDir)
            try {
                FileOutputStream(tempZip).use { output ->
                    service.files().get(fileId).executeMediaAndDownloadTo(output)
                }
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

    private fun createBackupZip(): File {
        val dbFile = context.getDatabasePath(PetGroomingDatabase.DATABASE_NAME)
        val imagesDir = File(context.filesDir, IMAGES_DIR)
        val zipFile = File.createTempFile("backup", ".zip", context.cacheDir)
        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
            listOf(dbFile, File(dbFile.path + "-wal"), File(dbFile.path + "-shm")).forEach { file ->
                if (file.exists()) addEntry(zos, file, "$DB_PREFIX${file.name}")
            }
            if (imagesDir.isDirectory) {
                imagesDir.listFiles()?.forEach { file ->
                    if (file.isFile) addEntry(zos, file, "$PHOTO_PREFIX${file.name}")
                }
            }
        }
        return zipFile
    }

    private fun addEntry(zos: ZipOutputStream, file: File, entryName: String) {
        zos.putNextEntry(ZipEntry(entryName))
        FileInputStream(file).use { it.copyTo(zos) }
        zos.closeEntry()
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

    private fun ensureFolder(service: Drive, name: String, parentId: String?): String {
        findFolder(service, name, parentId)?.let { return it }
        val metadata = DriveFile().apply {
            this.name = name
            mimeType = MIME_FOLDER
            if (parentId != null) parents = listOf(parentId)
        }
        return service.files().create(metadata).setFields("id").execute().id
    }

    private fun findFolder(service: Drive, name: String, parentId: String?): String? {
        val query = buildString {
            append("mimeType='$MIME_FOLDER' and trashed=false and name='$name'")
            if (parentId != null) append(" and '$parentId' in parents")
        }
        return service.files().list()
            .setQ(query)
            .setSpaces("drive")
            .setFields("files(id)")
            .execute()
            .files
            ?.firstOrNull()
            ?.id
    }

    private fun upload(service: Drive, file: File, name: String, parentId: String) {
        val metadata = DriveFile().apply {
            this.name = name
            parents = listOf(parentId)
        }
        val content = FileContent(MIME_ZIP, file)
        service.files().create(metadata, content).setFields("id").execute()
    }

    private fun timestamp(): String =
        SimpleDateFormat("yyyy-MM-dd-HHmm", Locale.US).format(Date())

    companion object {
        private const val APP_FOLDER = "PetGroomingManager"
        private const val BACKUPS_FOLDER = "Backups"
        private const val MIME_FOLDER = "application/vnd.google-apps.folder"
        private const val MIME_ZIP = "application/zip"
        private const val APP_NAME = "Pet Grooming Manager"
        private const val IMAGES_DIR = "images"
        private const val DB_PREFIX = "database/"
        private const val PHOTO_PREFIX = "photos/"
    }
}
