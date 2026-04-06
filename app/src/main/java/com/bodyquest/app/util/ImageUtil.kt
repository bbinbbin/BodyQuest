package com.bodyquest.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File

object ImageUtil {

    fun compressAndResize(context: Context, uri: Uri, maxSize: Int = 512): ByteArray {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open image URI")
        val original = BitmapFactory.decodeStream(inputStream)
            ?: throw IllegalArgumentException("Cannot decode image")
        inputStream.close()

        // EXIF 회전 정보 읽어서 비트맵 바르게 회전
        val rotated = context.contentResolver.openInputStream(uri)?.use { exifStream ->
            val exif = ExifInterface(exifStream)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            val degree = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
            if (degree != 0f) {
                val matrix = Matrix().apply { postRotate(degree) }
                val result = Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
                original.recycle()
                result
            } else {
                original
            }
        } ?: original

        val size = minOf(rotated.width, rotated.height)
        val x = (rotated.width - size) / 2
        val y = (rotated.height - size) / 2
        val cropped = Bitmap.createBitmap(rotated, x, y, size, size)

        val scaled = if (cropped.width > maxSize) {
            Bitmap.createScaledBitmap(cropped, maxSize, maxSize, true)
        } else {
            cropped
        }

        val maxBytes = 500_000 // 500KB — Base64 인코딩 시 ~667KB, Firestore 1MB 문서 제한 내
        var quality = 80
        var output: ByteArrayOutputStream
        do {
            output = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, quality, output)
            quality -= 10
        } while (output.size() > maxBytes && quality > 10)

        if (scaled !== cropped) scaled.recycle()
        if (cropped !== rotated) cropped.recycle()
        rotated.recycle()

        return output.toByteArray()
    }

    fun createTempImageUri(context: Context): Uri {
        val dir = File(context.cacheDir, "images").apply { mkdirs() }
        val file = File(dir, "camera_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
