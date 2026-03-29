package com.bodyquest.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File

object ImageUtil {

    fun compressAndResize(context: Context, uri: Uri, maxSize: Int = 512): ByteArray {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open image URI")
        val original = BitmapFactory.decodeStream(inputStream)
            ?: throw IllegalArgumentException("Cannot decode image")
        inputStream.close()

        val size = minOf(original.width, original.height)
        val x = (original.width - size) / 2
        val y = (original.height - size) / 2
        val cropped = Bitmap.createBitmap(original, x, y, size, size)

        val scaled = if (cropped.width > maxSize) {
            Bitmap.createScaledBitmap(cropped, maxSize, maxSize, true)
        } else {
            cropped
        }

        val output = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, output)

        if (scaled !== cropped) scaled.recycle()
        if (cropped !== original) cropped.recycle()
        original.recycle()

        return output.toByteArray()
    }

    fun createTempImageUri(context: Context): Uri {
        val dir = File(context.cacheDir, "images").apply { mkdirs() }
        val file = File(dir, "camera_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
