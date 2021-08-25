package com.felwal.markana.data

import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.core.net.toUri
import com.felwal.markana.data.db.DbDataSource
import com.felwal.markana.data.network.SafDataSource
import com.felwal.markana.prefs
import com.felwal.markana.util.coToast
import com.felwal.markana.util.withIO

class NoteRepository(
    private val applicationContext: Context,
    private val db: DbDataSource,
    private val saf: SafDataSource
) {

    // read

    suspend fun getNotes(): List<Note> = withIO {
        db.noteDao().getNotes(prefs.sortBy, prefs.ascending)
    }

    suspend fun getNote(uri: String): Note? = withIO {
        db.noteDao().getNote(uri)
    }

    // sync: download from saf to db

    suspend fun syncNotes() {
        extractTrees()
        withIO {
            db.noteDao().getUris().forEach {
                syncNote(it)
            }
        }
    }

    suspend fun syncNote(uri: String) {
        val note = saf.readFile(uri.toUri())
        if (note == null) db.noteDao().deleteNote(uri)
        else withIO {
            db.noteDao().updateNoteContent(uri, note.filename, note.content)
        }
    }

    // extract: save tree notes to db

    private suspend fun extractTrees() = withIO {
        db.treeDao().getTrees().forEach { extractTree(it) }
    }

    private suspend fun extractTree(tree: Tree) {
        // TODO: persist permissions?

        val notes = saf.readTree(tree)
        withIO {
            db.noteDao().addNoteIfNotExists(*notes.toTypedArray())
        }
    }

    // write

    fun linkNote(openDocumentLauncher: ActivityResultLauncher<Array<String>>) {
        /** Saving to db is done in [handleOpenedDocument], which should be called from [openDocumentLauncher]. */
        saf.openFile(openDocumentLauncher)
    }

    fun linkFolder(openTreeLauncher: ActivityResultLauncher<Uri>) {
        /** Saving to db is done in [handleOpenedDocumentTree], which should be called from [openTreeLauncher]. */
        saf.openTree(openTreeLauncher, null)
    }

    fun createNote(createDocumentLauncher: ActivityResultLauncher<String>) {
        saf.createFile(createDocumentLauncher, "")
    }

    suspend fun saveNote(note: Note, rename: Boolean) {
        // rename
        val newUri = if (rename) saf.renameFile(note.uri.toUri(), note.filename) else null
        newUri?.let {
            db.noteDao().deleteNote(note.uri)
            note.uri = it.toString()
        }

        saf.writeFile(note)
        withIO {
            db.noteDao().addOrUpdateNote(note)
        }
    }

    suspend fun updateNoteMetadata(vararg notes: Note) = withIO {
        for (note in notes) db.noteDao().updateNoteMetadata(note.uri, note.isPinned, note.colorIndex)
    }

    suspend fun unlinkNotes(uris: List<String>) =
        uris.forEach { unlinkNote(it) }

    private suspend fun unlinkNote(uri: String) = withIO {
        val note = db.noteDao().getNote(uri)

        note?.let {
            // temp: also unlink whole tree if note from tree
            // TODO: use a sort of archive in that case
            it.treeId?.let { unlinkTree(it) }

            db.noteDao().deleteNote(uri)
        }
    }

    private suspend fun unlinkTree(id: Int) = withIO {
        db.treeDao().deleteTree(id)
        db.noteDao().deleteNotes(id)
    }

    suspend fun deleteNotes(uris: List<String>) {
        for (uri in uris) {
            saf.deleteFile(uri.toUri())
            unlinkNote(uri) // TODO: dont unlink all of same folder
        }
    }

    suspend fun deleteNote(uri: String) {
        saf.deleteFile(uri.toUri())
        unlinkNote(uri) // TODO: dont unlink all of same folder
    }

    // launcher results

    suspend fun handleOpenedDocument(uri: Uri) = withIO {
        saf.persistPermissions(uri)

        // read and save to db
        if (!db.noteDao().doesNoteExist(uri.toString())) {
            val note = saf.readFile(uri)
            note?.let {
                db.noteDao().addNote(it)
            }
        }
        else applicationContext.coToast("Note already linked")
    }

    suspend fun handleOpenedDocumentTree(uri: Uri) {
        saf.persistPermissions(uri)

        // save tree uri and sync
        db.treeDao().addTreeIfNotExists(Tree(uri.toString()))
        val tree = db.treeDao().getTree(uri.toString())
        tree?.let { extractTree(it) }
    }

    // permissions

    fun persistPermissions(uri: Uri) {
        saf.persistPermissions(uri)
    }
}