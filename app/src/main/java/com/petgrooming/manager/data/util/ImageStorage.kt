package com.petgrooming.manager.data.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Saves picked images into the app's private [IMAGES_DIR], automatically
 * downscaling and JPEG-compressing them to keep the file size small.
 *
 * Images stored here are also included in the backup zip (see BackupRepository).
 */
object ImageStorage {

    const val IMAGES_DIR = "images"
    private const val MAX_DIMENSION = 1024
    private const val JPEG_QUALITY = 80

    /**
     * Reads the image at [source], downscales it so its longest edge is at most
     * [MAX_DIMENSION] px, compresses it to JPEG and writes it to the app's
     * private images directory. Returns the absolute file path, or null on failure.
     */
    suspend fun saveImage(context: Context, source: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val dir = File(context.filesDir, IMAGES_DIR).apply { mkdirs() }
            val target = File(dir, "${UUID.randomUUID()}.jpg")

            // 1. Read bounds only to compute a memory-efficient sample size.
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(source)?.use {
                BitmapFactory.decodeStream(it, null, bounds)
            }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null

            // 2. Decode at a reduced resolution.
            val options = BitmapFactory.Options().apply {
                inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight)
            }
            val decoded = context.contentResolver.openInputStream(source)?.use {
                BitmapFactory.decodeStream(it, null, options)
            } ?: return@runCatching null

            // 3. Correct orientation using EXIF metadata.
            val orientation = context.contentResolver.openInputStream(source)?.use { stream ->
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL

            val oriented = applyOrientation(decoded, orientation)

            // 4. Final precise downscale and JPEG compression.
            val scaled = scaleDown(oriented)
            FileOutputStream(target).use { out ->
                scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            }

            if (scaled != oriented) scaled.recycle()
            if (oriented != decoded) oriented.recycle()
            decoded.recycle()

            target.absolutePath
        }.getOrNull()
    }

    /** Deletes a previously saved image file. Only touches files inside [IMAGES_DIR]. */
    fun deleteImage(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching {
            File(path).takeIf { it.exists() && it.path.contains("/$IMAGES_DIR/") }?.delete()
        }
    }

    private fun calculateInSampleSize(width: Int, height: Int): Int {
        var sample = 1
        var w = width
        var h = height
        while ((w / 2) >= MAX_DIMENSION && (h / 2) >= MAX_DIMENSION) {
            w /= 2
            h /= 2
            sample *= 2
        }
        return sample
    }

    private fun scaleDown(bitmap: Bitmap): Bitmap {
        val longest = maxOf(bitmap.width, bitmap.height)
        if (longest <= MAX_DIMENSION) return bitmap
        val ratio = MAX_DIMENSION.toFloat() / longest
        val w = (bitmap.width * ratio).toInt().coerceAtLeast(1)
        val h = (bitmap.height * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, w, h, true)
    }

    private fun applyOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
