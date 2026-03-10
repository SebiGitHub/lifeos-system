package com.sebi.lifeos.lifeosapp.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File

object Downloads {

    private const val CSV_MIME = "text/csv"

    /**
     * Guardar CSV en Descargas.
     * - Android 10+ (Q): MediaStore
     * - < Android 10: File directo en /Download (puede requerir permiso en algunos dispositivos)
     */
    fun saveCsvToDownloads(context: Context, fileName: String, csvText: String): Result<Uri> {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver

                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, CSV_MIME)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val uri = resolver.insert(collection, values)
                    ?: error("No se pudo crear el archivo en Descargas")

                resolver.openOutputStream(uri)?.use { out ->
                    out.write(csvText.toByteArray(Charsets.UTF_8))
                } ?: error("No se pudo abrir OutputStream")

                val done = ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }
                resolver.update(uri, done, null, null)

                uri
            } else {
                @Suppress("DEPRECATION")
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!dir.exists()) dir.mkdirs()

                val file = File(dir, fileName)
                file.writeText(csvText, Charsets.UTF_8)

                // Solo “guardar”: devolvemos Uri de archivo.
                // Para compartir: usa buildShareCsvIntent (cache + FileProvider).
                Uri.fromFile(file)
            }
        }
    }

    /**
     * Compartir CSV (seguro en todas las versiones) usando FileProvider (cache).
     * Requiere FileProvider configurado (manifest + xml/file_paths).
     */
    fun buildShareCsvIntent(context: Context, fileName: String, csvText: String): Result<Intent> {
        return runCatching {
            val cacheDir = File(context.cacheDir, "exports")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            val file = File(cacheDir, fileName)
            file.writeText(csvText, Charsets.UTF_8)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            Intent(Intent.ACTION_SEND).apply {
                type = CSV_MIME
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
    }
}
