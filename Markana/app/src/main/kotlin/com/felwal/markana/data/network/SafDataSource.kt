package com.felwal.markana.data.network

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.UriPermission
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.felwal.markana.R
import com.felwal.markana.data.Note
import com.felwal.markana.data.Tree
import com.felwal.markana.util.coToastLog
import com.felwal.markana.util.isMime
import com.felwal.markana.util.toastLog
import com.felwal.markana.util.tryToastLog
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStreamReader

private const val LOG_TAG = "Saf"
private const val MIME_TEXT_TYPE = "text"
private const val MIME_TEXT = "text/*"
private const val MIME_TEXT_PLAIN = "text/plain"

class SafDataSource(private val applicationContext: Context) {

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

    fun openTree(openDocuementTreeLauncher: ActivityResultLauncher<Uri>, initialUri: Uri?) =
        openDocuementTreeLauncher.launch(initialUri)

    suspend fun readFile(uri: Uri): Note? {
        if (false && !hasReadPermission(uri.toString())) {
            // this also fires if the file has been moved/deleted
            // and in the case of Dropbox, when renamed
            applicationContext
                .coToastLog(LOG_TAG, R.string.toast_e_saf_file_not_found_or_perm_read)

            // TODO: get option to relink

            // TODO: update to allow temporary permissions / edit with

            return null // unlink note
        }

        try {
            // read metadata
            val filename = resolver.getDisplayName(uri)

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
                applicationContext
                    .coToastLog(LOG_TAG, R.string.toast_e_saf_file_not_found_or_perm, e)

                return null // unlink note
            }

            return Note(filename, content, uri = uri.toString())
        }
        catch (e: SecurityException) {
            applicationContext.coToastLog(LOG_TAG, R.string.toast_e_saf_perm_read, e)
        }
        catch (e: IllegalArgumentException) {
            // "Failed to determine if home:Markana/notes/h.txt is child of home:Markana/notes"
            // file was moved, removed or otherwise made unavailable
            applicationContext.coToastLog(LOG_TAG, R.string.toast_e_saf_file_not_found_read, e)
        }

        return null // unlink note
    }

    suspend fun readTree(tree: Tree): List<Note> {
        val notes = mutableListOf<Note>()

        val docDir = DocumentFile.fromTreeUri(applicationContext, tree.uri.toUri())
        val docFiles = readDocumentFile(docDir)

        var wrongMimeCount = 0

        for (file in docFiles) {
            file.uri
            // check mime TODO: markdown isn't recognized as text
            if (true || file.type?.isMime(MIME_TEXT_TYPE) == true) {
                readFile(file.uri)?.let { note ->
                    //persistPermissions(note.uri.toUri()) // TODO: not working / not neccessary?
                    note.treeId = tree.id
                    notes.add(note)
                }
            }
            else wrongMimeCount++
        }

        if (wrongMimeCount > 0) {
            //applicationContext.coToast("$wrongMimeCount files were not linked due to wrong format")
            Log.i(LOG_TAG, "$wrongMimeCount files were not linked due to wrong format")
        }

        return notes
    }

    /**
     * Recursively gets all files in a documentFile directory
     */
    private fun readDocumentFile(document: DocumentFile?): List<DocumentFile> {
        if (document == null || isFileHidden(document.uri)) return listOf()
        if (!document.isDirectory) return listOf(document)

        val docs = mutableListOf<List<DocumentFile>>()

        for (doc in document.listFiles()) {
            // go one dir deeper
            docs.add(readDocumentFile(doc))
        }

        return docs.flatten()
    }

    // write

    fun createFile(createDocumentResultLauncher: ActivityResultLauncher<String>, filename: String) =
        createDocumentResultLauncher.launch(filename)

    fun writeFile(note: Note) {
        if (false && !hasWritePermission(note.uri)) {
            applicationContext.toastLog(LOG_TAG, R.string.toast_e_saf_file_not_found_or_perm_write)

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
        catch (e: SecurityException) {
            applicationContext.toastLog(LOG_TAG, R.string.toast_e_saf_perm_write, e)
        }
        catch (e: FileNotFoundException) {
            applicationContext.toastLog(LOG_TAG, R.string.toast_e_saf_file_not_found_write, e)
        }
        catch (e: IllegalArgumentException) {
            applicationContext.toastLog(LOG_TAG, R.string.toast_e_saf_file_not_found_write, e)
        }
    }

    fun renameFile(uri: Uri, filename: String): Uri? {
        try {
            DocumentsContract.renameDocument(resolver, uri, filename)?.let {
                // it is now technically a new file
                persistPermissions(it)
                return it
            }
        }
        catch (e: UnsupportedOperationException) {
            applicationContext.toastLog(LOG_TAG, R.string.toast_e_saf_provider_support_rename, e)
        }
        catch (e: IllegalStateException) {
            applicationContext
                .toastLog(LOG_TAG, "Could not rename file; '$filename' already exists at the given location", e)
        }
        catch (e: FileNotFoundException) {
            applicationContext.toastLog(LOG_TAG, R.string.toast_e_saf_file_not_found_rename, e)
        }
        catch (e: IllegalArgumentException) {
            applicationContext.toastLog(LOG_TAG, R.string.toast_e_saf_file_not_found_rename, e)
        }

        return null
    }

    fun deleteFile(uri: Uri) {
        try {
            DocumentsContract.deleteDocument(resolver, uri)
        }
        catch (e: UnsupportedOperationException) {
            applicationContext.toastLog(LOG_TAG, R.string.toast_e_saf_provider_support_delete, e)
        }
        catch (e: FileNotFoundException) {
            applicationContext.toastLog(LOG_TAG, R.string.toast_e_saf_file_not_found_delete, e)
        }
        catch (e: IllegalArgumentException) {
            applicationContext.toastLog(LOG_TAG, R.string.toast_e_saf_file_not_found_delete, e)
        }
    }

    // persist permissions

    fun persistPermissions(uri: Uri) {
        try {
            return resolver.takePersistableUriPermission(uri, persistPermissionsFlags)
        }
        catch (e: SecurityException) {
            applicationContext.tryToastLog(LOG_TAG, R.string.toast_e_saf_perm_persist, e)
        }
    }

    private fun releasePermissions(uri: Uri) =
        resolver.releasePersistableUriPermission(uri, persistPermissionsFlags)

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

    private fun hasReadPermission(uri: String): Boolean =
        getPersistedPermission(uri)?.isReadPermission ?: false

    private fun hasWritePermission(uri: String): Boolean =
        getPersistedPermission(uri)?.isWritePermission ?: false

    // tool

    private fun isFileHidden(uri: Uri): Boolean =
        resolver.getDisplayName(uri).substring(0, 1) == "."
}

private fun ContentResolver.getDisplayName(uri: Uri): String {
    val cursor = query(uri, null, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            return it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        }
    }
    return ""
}

class CreateTextDocument : ActivityResultContracts.CreateDocument() {

    override fun createIntent(context: Context, input: String): Intent =
        super.createIntent(context, input).setType(MIME_TEXT)
}