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
        return@withIO db.noteDao().getNotes(prefs.sortBy, prefs.ascending)
    }

    suspend fun getNote(uri: String): Note? = withIO {
        db.noteDao().getNote(uri)
    }

    // sync: download from saf to db

    suspend fun syncNotes() = withIO {
        db.noteDao().getUris().forEach {
            syncNote(it)
        }
    }

    private suspend fun syncNote(uri: String) = withIO {
        saf.readFile(uri.toUri())?.let {
            db.noteDao().updateNote(uri, it.filename, it.content)
        }
    }

    // extract: save tree notes to db

    private suspend fun extractAllTrees() = withIO {
        db.treeDao().getTrees().forEach { extractTree(it) }
    }

    private suspend fun extractTree(tree: Tree) = withIO {
        // TODO: persist permissions?

        val notes = saf.readTree(tree)
        db.noteDao().addNoteIfNotExists(*notes.toTypedArray())
    }

    // write

    suspend fun linkNote(openDocumentLauncher: ActivityResultLauncher<Array<String>>) = withIO {
        saf.openFile(openDocumentLauncher)
        /** Saving to db is done in [handleOpenedDocument], which should be called from [openDocumentLauncher]. */
    }

    suspend fun linkFolder(openTreeLauncher: ActivityResultLauncher<Uri>) = withIO {
        saf.openTree(openTreeLauncher, null)
        /** Saving to db is done in [handleOpenedDocumentTree], which should be called from [openTreeLauncher]. */
    }

    suspend fun createNote(createDocumentLauncher: ActivityResultLauncher<String>) = withIO {
        saf.createFile(createDocumentLauncher, "")
    }

    suspend fun saveNote(note: Note, rename: Boolean) = withIO {
        db.noteDao().addOrUpdateNote(note)
        saf.writeFile(note)
        if (rename) saf.renameFile(note.uri.toUri(), note.filename)
    }

    suspend fun unlinkNotes(uris: List<String>) = withIO {
        for (uri in uris) {
            unlinkNote(uri)
        }
    }

    private suspend fun unlinkNote(uri: String) = withIO {
        val note = db.noteDao().getNote(uri)

        note?.let {
            // temp: also unlink whole tree if note from tree
            // TODO: use a sort of archive in that case
            it.treeId?.let { unlinkTree(it) }

            db.noteDao().deleteNote(uri)
        }
    }

    suspend fun unlinkTree(id: Int) = withIO {
        db.treeDao().deleteTree(id)
        db.noteDao().deleteNotes(id)
    }

    suspend fun deleteNotes(uris: List<String>) = withIO {
        for (uri in uris) {
            saf.deleteFile(uri.toUri())
            unlinkNote(uri) // TODO: dont unlink all of same folder
        }
    }

    suspend fun deleteNote(uri: String) = withIO {
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

    suspend fun handleOpenedDocumentTree(uri: Uri) = withIO {
        saf.persistPermissions(uri)

        // save tree uri and sync
        db.treeDao().addTreeIfNotExists(Tree(uri.toString()))
        val tree = db.treeDao().getTree(uri.toString())
        tree?.let { extractTree(it) }
    }

    // permissions

    suspend fun persistPermissions(uri: Uri) = withIO {
        saf.persistPermissions(uri)
    }
}