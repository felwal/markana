package me.felwal.markana.data

import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.core.net.toUri
import me.felwal.android.util.coToast
import me.felwal.android.util.withIO
import me.felwal.markana.R
import me.felwal.markana.data.db.DbDataSource
import me.felwal.markana.data.network.SafDataSource
import me.felwal.markana.prefs
import me.felwal.markana.util.toUriPathString

class Repository(
    private val applicationContext: Context,
    private val db: DbDataSource,
    private val saf: SafDataSource
) {
    // read

    suspend fun getNotes(labelId: Long, searchQuery: String): List<Note> = withIO {
        db.noteDao().getNotes(labelId, searchQuery, prefs.sortBy, prefs.ascending, prefs.showArchived)
    }

    suspend fun getNote(uri: String): Note? = withIO {
        db.noteDao().getNote(uri)
    }

    suspend fun getLabels(): List<Label> = withIO {
        db.labelDao().getLabels().toMutableList().apply { add(0, Label("Backlog")) }
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
        // this is necessary since extractTree extracts trees which should be already deleted
        // TODO: better solution
        db.noteDao().deleteNotesInDeletedTrees()
    }

    private suspend fun extractTree(tree: Tree) {
        // TODO: persist permissions?

        val notes = saf.readTree(tree)
        withIO {
            notes.forEach {
                db.noteDao().addNoteIfNotExists(it)
            }
        }
    }

    // write

    suspend fun addLabel(name: String) = withIO {
        db.labelDao().addLabel(Label(name))
    }

    suspend fun renameLabel(id: Long, newName: String) = withIO {
        db.labelDao().renameLabel(id, newName)
    }

    suspend fun deleteLabel(id: Long) = withIO {
        db.labelDao().deleteLabel(id)
        db.noteDao().removeLabelReferences(id)
    }

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
        notes.forEach { db.noteDao().updateNoteMetadata(it.uri, it.isPinned, it.isArchived, it.colorIndex, it.labelId) }
    }

    suspend fun unlinkNote(uri: String) = withIO {
        db.noteDao().deleteNote(uri)
    }

    suspend fun unlinkTree(id: Long) = withIO {
        db.treeDao().deleteTree(id)
        db.noteDao().deleteNotes(id)
    }

    suspend fun deleteNote(uri: String) {
        saf.deleteFile(uri.toUri())
        unlinkNote(uri)
    }

    // launcher results

    suspend fun handleOpenedDocument(uri: Uri) = withIO {
        // check if note exists (independently or in tree)
        if (db.noteDao().doesNoteExistIncludeInTree(uri.toUriPathString())) {
            applicationContext.coToast(R.string.toast_i_db_note_already_linked)
            return@withIO
        }

        saf.persistPermissions(uri)

        // read and save to db
        val note = saf.readFile(uri)
        note?.let { db.noteDao().addNote(it) }
    }

    suspend fun handleOpenedDocumentTree(uri: Uri) = withIO {
        // check if tree exists (independently or nested)
        if (db.treeDao().doesTreeExistIncludeAsNested(uri.toString())) {
            applicationContext.coToast(R.string.toast_i_db_tree_already_linked)
            return@withIO
        }

        saf.persistPermissions(uri)

        // save tree uri and sync
        addTreeIfNotExists(Tree(uri.toString()))
        val tree = db.treeDao().getTree(uri.toString())
        tree?.let { extractTree(it) }
    }

    private suspend fun addTreeIfNotExists(tree: Tree) {
        // the tree already exists; don't add
        if (db.treeDao().doesTreeExist(tree.uri)) return

        // the new tree may contain already linked trees; unlink them
        val nestedTrees = db.treeDao().getNestedTrees(tree.uri)
        for (nestedTree in nestedTrees) unlinkTree(nestedTree.id)

        db.treeDao().addTree(tree)
    }

    // permissions

    fun persistPermissions(uri: Uri) {
        saf.persistPermissions(uri)
    }
}