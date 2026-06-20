package com.petgrooming.manager.data.backup

/** Metadata describing a backup archive stored on Google Drive. */
data class DriveBackupFile(
    val id: String,
    val name: String,
    val createdAtMillis: Long,
    val sizeBytes: Long
)
