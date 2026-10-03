package com.example.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ProductImageHelper {

    fun copyUriToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val productsDir = File(context.filesDir, "products").apply {
                if (!exists()) mkdirs()
            }
            val destinationFile = File(
                productsDir,
                "prod_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            )

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }
            destinationFile.absolutePath
        } catch (_: Throwable) {
            null
        }
    }
}
