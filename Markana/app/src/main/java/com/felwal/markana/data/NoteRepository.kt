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

    suspend fun createNote(resultLauncher: ActivityResultLauncher<String>) = withIO {
        saf.createFile(resultLauncher, "")
    }

    suspend fun saveNote(note: Note, rename: Boolean) = withIO {
        db.noteDao().addOrUpdateNote(note)
        saf.writeFile(note)
        if (rename) saf.renameFile(note.uri.toUri(), note.filename)
    }

    suspend fun unlinkNote(uri: String) = withIO {
        db.noteDao().deleteNote(uri)
    }

    suspend fun unlinkNotes(uris: List<String>) = withIO {
        for (uri in uris) {
            unlinkNote(uri)
        }
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

    //

    suspend fun persistPermissions(uri: Uri) = withIO {
        saf.persistPermissions(uri)
    }

    //

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
}