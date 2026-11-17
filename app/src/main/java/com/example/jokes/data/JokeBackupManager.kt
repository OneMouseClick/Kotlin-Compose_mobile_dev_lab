package com.example.jokes.data

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupFileInfo(
    val name: String,
    val location: String,
    val sizeBytes: Long,
    val modifiedAtSeconds: Long
)

object JokeBackupManager {

    private const val FILE_NAME = "jokes_18.txt"
    private const val INTERNAL_BACKUP_DIR = "jokes_backups"

    private fun downloadsCollection(): Uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI

    suspend fun getPublicBackup(context: Context): BackupFileInfo? = withContext(Dispatchers.IO) {
        runCatching { findPublicBackup(context)?.info }.getOrNull()
    }

    suspend fun getInternalBackup(context: Context): BackupFileInfo? = withContext(Dispatchers.IO) {
        val file = internalBackupFile(context)
        if (!file.exists()) null else file.toBackupInfo()
    }

    suspend fun createBackup(context: Context, jokes: List<Joke>): Result<BackupFileInfo> =
        withContext(Dispatchers.IO) {
            runCatching {
                val content = buildBackupContent(jokes)
                deletePublicBackups(context)
                insertPublicFile(context, content)
                internalBackupFile(context).delete()
                findPublicBackup(context)?.info
                    ?: BackupFileInfo(
                        FILE_NAME,
                        "Download/$FILE_NAME",
                        content.toByteArray(Charsets.UTF_8).size.toLong(),
                        nowSeconds()
                    )
            }
        }

    suspend fun deletePublicAndKeepInternal(context: Context): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val backup = findPublicBackup(context) ?: error("Файл резервной копии не найден")
                val content = context.contentResolver.openInputStream(backup.uri)?.use { it.readBytes() }
                    ?: error("Не удалось прочитать файл")

                val internalFile = internalBackupFile(context)
                internalFile.parentFile?.mkdirs()
                internalFile.writeBytes(content)
                deletePublicBackups(context)
            }
        }

    suspend fun restoreBackup(context: Context): Result<BackupFileInfo> = withContext(Dispatchers.IO) {
        runCatching {
            val internalFile = internalBackupFile(context)
            check(internalFile.exists()) { "Внутренняя резервная копия не найдена" }

            val content = internalFile.readText(Charsets.UTF_8)
            deletePublicBackups(context)
            insertPublicFile(context, content)
            internalFile.delete()

            findPublicBackup(context)?.info
                ?: BackupFileInfo(
                    FILE_NAME,
                    "Download/$FILE_NAME",
                    content.toByteArray(Charsets.UTF_8).size.toLong(),
                    nowSeconds()
                )
        }
    }

    private fun findPublicBackup(context: Context): PublicBackup? {
        val projection = arrayOf(
            MediaStore.Downloads._ID,
            MediaStore.Downloads.DISPLAY_NAME,
            MediaStore.Downloads.SIZE,
            MediaStore.Downloads.DATE_ADDED,
            MediaStore.Downloads.DATE_MODIFIED,
            MediaStore.Downloads.RELATIVE_PATH
        )

        return context.contentResolver.query(
            downloadsCollection(),
            projection,
            "${MediaStore.Downloads.DISPLAY_NAME} = ?",
            arrayOf(FILE_NAME),
            "${MediaStore.Downloads.DATE_MODIFIED} DESC"
        )?.use { cursor ->
            if (!cursor.moveToFirst()) {
                null
            } else {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Downloads.DISPLAY_NAME))
                    ?: FILE_NAME
                val size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads.SIZE))
                val added = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads.DATE_ADDED))
                val modified = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads.DATE_MODIFIED))
                    .takeIf { it > 0 } ?: added
                val relativePath = cursor.getString(
                    cursor.getColumnIndexOrThrow(MediaStore.Downloads.RELATIVE_PATH)
                ) ?: Environment.DIRECTORY_DOWNLOADS
                val directory = relativePath.trimEnd('/')

                PublicBackup(
                    uri = ContentUris.withAppendedId(downloadsCollection(), id),
                    info = BackupFileInfo(
                        name = name,
                        location = if (directory.isEmpty()) name else "$directory/$name",
                        sizeBytes = size,
                        modifiedAtSeconds = modified
                    )
                )
            }
        }
    }

    private fun insertPublicFile(context: Context, content: String): Uri {
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, FILE_NAME)
            put(MediaStore.Downloads.MIME_TYPE, "text/plain")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(downloadsCollection(), values) ?: error("Не удалось создать файл")

        try {
            resolver.openOutputStream(uri)?.use { stream ->
                stream.write(content.toByteArray(Charsets.UTF_8))
            } ?: error("Не удалось открыть файл для записи")

            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            return uri
        } catch (exception: Exception) {
            resolver.delete(uri, null, null)
            throw exception
        }
    }

    private fun deletePublicBackups(context: Context) {
        val projection = arrayOf(MediaStore.Downloads._ID)
        context.contentResolver.query(
            downloadsCollection(),
            projection,
            "${MediaStore.Downloads.DISPLAY_NAME} = ?",
            arrayOf(FILE_NAME),
            null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID)
            while (cursor.moveToNext()) {
                val uri = ContentUris.withAppendedId(downloadsCollection(), cursor.getLong(idColumn))
                context.contentResolver.delete(uri, null, null)
            }
        }
    }

    private fun buildBackupContent(jokes: List<Joke>): String = buildString {
        appendLine("Резервная копия шуток")
        appendLine("Файл: $FILE_NAME")
        appendLine("Создано: ${formatDate(nowSeconds())}")
        appendLine("Количество шуток: ${jokes.size}")
        appendLine()
        jokes.forEachIndexed { index, joke ->
            appendLine("${index + 1}. [${joke.category}] ${joke.text}")
            appendLine("Рейтинг: ${joke.rating}")
            appendLine()
        }
    }

    private fun internalBackupFile(context: Context): File =
        File(File(context.filesDir, INTERNAL_BACKUP_DIR), FILE_NAME)

    private fun File.toBackupInfo(): BackupFileInfo = BackupFileInfo(
        name = name,
        location = "files/$INTERNAL_BACKUP_DIR/$name",
        sizeBytes = length(),
        modifiedAtSeconds = lastModified() / 1000
    )

    private fun nowSeconds(): Long = System.currentTimeMillis() / 1000

    private fun formatDate(seconds: Long): String =
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(seconds * 1000))

    private data class PublicBackup(val uri: Uri, val info: BackupFileInfo)
}
