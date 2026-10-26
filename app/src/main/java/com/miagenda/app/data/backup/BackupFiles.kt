package com.miagenda.app.data.backup

import android.content.Context
import android.net.Uri
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BackupFiles(
    context: Context
) {
    private val contentResolver = context.applicationContext.contentResolver

    suspend fun write(uri: Uri, contents: String) {
        withContext(Dispatchers.IO) {
            val stream = contentResolver.openOutputStream(uri)
                ?: throw IOException("No output stream")
            stream.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write(contents)
            }
        }
    }

    suspend fun read(uri: Uri): String {
        return withContext(Dispatchers.IO) {
            val stream = contentResolver.openInputStream(uri)
                ?: throw IOException("No input stream")
            stream.bufferedReader(Charsets.UTF_8).use { reader ->
                reader.readText()
            }
        }
    }
}
