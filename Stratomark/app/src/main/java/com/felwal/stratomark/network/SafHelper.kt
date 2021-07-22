package com.felwal.stratomark.network

import android.content.Context
import android.content.Intent
import android.content.UriPermission
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import com.felwal.stratomark.data.AppDatabase
import com.felwal.stratomark.data.Note
import com.felwal.stratomark.util.coToastLog
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStreamReader

private const val LOG_TAG = "Saf"
private const val MIME_TEXT = "text/*"
private const val MIME_TEXT_PLAIN = "text/plain"

class SafHelper(private val applicationContext: Context) {

    private val resolver = applicationContext.contentResolver

    private val persistPermissionsFlags: Int =
        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

    private val persistedPermissionCount: Int
        get() = resolver.persistedUriPermissions.size

    private val persistedPermissionCountCap: Int
        get() = if (ApplicationInfo().minSdkVersion >= 30) 512 else 128

    private val canPersistMorePermissions: Boolean
        get() = persistedPermissionCount < persistedPermissionCountCap

    // read

    fun openFile(openDocumentResultLauncher: ActivityResultLauncher<Array<String>>) =
        openDocumentResultLauncher.launch(arrayOf(MIME_TEXT))

    suspend fun readFile(uri: Uri): Note? {
        if (!hasReadPermission(uri.toString())) {
            // this also fires if the file has been moved/deleted
            // and in the case of DropBox, when renamed
            applicationContext
                .coToastLog(LOG_TAG, "Provider not found or permission to read not persisted, unlinking note ...")

            // TODO: get option to relink

            val db = AppDatabase.getInstance(applicationContext)
            db.noteDao().deleteNote(uri.toString())
            return null

            // TODO: update to allow temporary permissions / edit with
        }

        try {
            // read metadata
            var filename = ""
            val cursor = resolver.query(uri, null, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    filename = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }

            // read content
            var content = ""
            try {
                resolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        content = reader.readText()
                    }
                }
            }
            catch (e: FileNotFoundException) {
                // files from google drive fire this after restart. why?
                // they aren't caught with not having permissions.
                applicationContext.coToastLog(LOG_TAG, "Provider not found or permissions not persisted", e)
            }

            return Note(filename, content, uri.toString())
        }
        catch (e: SecurityException) {
            applicationContext.coToastLog(LOG_TAG, "Permissions denied for note", e)
        }

        return null
    }

    // write

    fun createFile(createDocumentResultLauncher: ActivityResultLauncher<String>, filename: String) =
        createDocumentResultLauncher.launch(filename)

    suspend fun writeFile(note: Note) {
        if (!hasWritePermission(note.uri)) {
            applicationContext.coToastLog(LOG_TAG, "Provider not found or permission to write not persisted")

            // TODO: save edits in db and suggest saving copy?

            return
        }

        try {
            resolver.openFileDescriptor(note.uri.toUri(), "w")?.use {
                FileOutputStream(it.fileDescriptor).use {
                    it.write(note.content.toByteArray())
                }
            }
        }
        catch (e: FileNotFoundException) {
            applicationContext.coToastLog(LOG_TAG, "File note found", e)
        }
    }

    suspend fun renameFile(uri: Uri, filename: String) {
        try {
            DocumentsContract.renameDocument(resolver, uri, filename)
        }
        catch (e: UnsupportedOperationException) {
            applicationContext.coToastLog(LOG_TAG, "Provider does not support rename", e)
        }
        catch (e: IllegalStateException) {
            applicationContext
                .coToastLog(LOG_TAG,"Could not rename file; '$filename' already exists at the given location", e)
        }
    }

    suspend fun deleteFile(uri: Uri) {
        try {
            DocumentsContract.deleteDocument(resolver, uri)
        }
        catch (e: UnsupportedOperationException) {
            applicationContext.coToastLog(LOG_TAG, "Provider does not support delete", e)
        }
    }

    // persist permissions

    fun persistPermissions(uri: Uri) {
        return resolver.takePersistableUriPermission(uri, persistPermissionsFlags)
    }

    private fun releasePermissions(uri: Uri) {
        return resolver.releasePersistableUriPermission(uri, persistPermissionsFlags)
    }

    private fun getPersistedPermission(uri: String): UriPermission? {
        val permissions = resolver.persistedUriPermissions

        val index = permissions.map { it.uri.toString() }.indexOf(uri)
        return if (index != -1) permissions[index] else null
    }

    private fun arePermissionsPersisted(uri: String): Boolean {
        return getPersistedPermission(uri) != null
        //return permission?.isReadPermission ?: false || permission?.isWritePermission ?: false

        // TODO: also check if it isn't in a permitted tree?
    }

    private fun hasReadPermission(uri: String): Boolean {
        return getPersistedPermission(uri)?.isReadPermission ?: false
    }

    private fun hasWritePermission(uri: String): Boolean {
        return getPersistedPermission(uri)?.isWritePermission ?: false
    }
}

class CreateTextDocument : ActivityResultContracts.CreateDocument() {

    override fun createIntent(context: Context, input: String): Intent {
        return super.createIntent(context, input).setType(MIME_TEXT)
    }
}