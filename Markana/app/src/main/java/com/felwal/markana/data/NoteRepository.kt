package com.felwal.markana.data

import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.core.net.toUri
import com.felwal.markana.network.SafHelper
import com.felwal.markana.util.coToast
import com.felwal.markana.util.withIO

class NoteRepository(
    private val applicationContext: Context,
    private val db: AppDatabase,
    private val saf: SafHelper
) {

    // read

    suspend fun getAllNotes(): List<Note> = withIO {
        // TODO: extract somewhere else
        extractAllTreesUris()

        val uris = db.noteDao().getAllUris()
        return@withIO uris.mapNotNull { saf.readFile(it.toUri()) }
    }

    suspend fun getNote(uri: String): Note? = withIO {
        return@withIO saf.readFile(uri.toUri())
    }

    // write

    suspend fun linkNote(resultLauncher: ActivityResultLauncher<Array<String>>) = withIO {
        saf.openFile(resultLauncher)
    }

    suspend fun linkFolder(resultLauncher: ActivityResultLauncher<Uri>) = withIO {
        saf.openTree(resultLauncher, null)
    }

    suspend fun createNote(resultLauncher: ActivityResultLauncher<String>) = withIO {
        saf.createFile(resultLauncher, "")
    }

    suspend fun saveNote(note: Note, rename: Boolean) = withIO {
        db.noteDao().addOrUpdateNote(note)
        saf.writeFile(note)
        if (rename) saf.renameFile(note.uri.toUri(), note.filename)
    }

    suspend fun unlinkNote(uri: String) = withIO {
        val note = db.noteDao().getNote(uri)

        note?.let {
            // temp: also unlink whole tree if note from tree
            // TODO: use a sort of archive in that case
            it.treeId?.let { unlinkTree(it) }

            db.noteDao().deleteNote(uri)
        }
    }

    suspend fun unlinkNotes(uris: List<String>) = withIO {
        for (uri in uris) {
            unlinkNote(uri)
        }
    }

    suspend fun unlinkTree(id: Int) = withIO {
        db.treeDao().deleteTree(id)
        db.noteDao().deleteNotes(id)
    }

    suspend fun deleteNote(uri: String) = withIO {
        saf.deleteFile(uri.toUri())
        unlinkNote(uri)
    }

    suspend fun deleteNotes(uris: List<String>) = withIO {
        for (uri in uris) {
            saf.deleteFile(uri.toUri())
            unlinkNote(uri)
        }
    }

    // permissions

    suspend fun persistPermissions(uri: Uri) = withIO {
        saf.persistPermissions(uri)
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
        tree?.let { extractTreeUris(it) }
    }

    // sync daos

    private suspend fun extractTreeUris(tree: Tree) = withIO {
        // TODO: persist permissions?

        val notes = saf.readTree(tree)
        db.noteDao().addOrUpdateNotes(notes)
    }

    /**
     * Syncs files in saved trees to notes
     */
    private suspend fun extractAllTreesUris() =
        db.treeDao().getAllTrees().forEach { extractTreeUris(it) }
}