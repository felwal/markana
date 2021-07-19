package com.felwal.stratomark.data

import androidx.core.net.toUri
import com.felwal.stratomark.network.SafHelper

class NoteRepository(
    private val db: AppDatabase,
    private val saf: SafHelper
) {

    // read

    fun getAllNotes(): List<Note> {
        val uris = db.noteDao().getAllUris()
        return uris.mapNotNull { saf.readFile(it.toUri()) }
    }

    fun getNote(uri: String): Note? {
        return saf.readFile(uri.toUri())
    }

    // write

    fun createNote() {
        saf.createFile("")
    }

    fun saveNote(note: Note, rename: Boolean) {
        db.noteDao().addOrUpdateNote(note)
        saf.writeFile(note)
        if (rename) saf.renameFile(note.uri.toUri(), note.filename)
    }

    fun createAndSaveNote(note: Note) {
        // TODO: unfinished; update uri
        saf.writeFile(note)
        saf.createFile(note.filename)
    }

    fun unlinkNote(uri: String) {
        db.noteDao().deleteNote(uri)
    }

    fun unlinkNotes(uris: List<String>) = uris.forEach { unlinkNote(it) }

    fun deleteNote(uri: String) {
        saf.deleteFile(uri.toUri())
        unlinkNote(uri)
    }

    fun deleteNotes(uris: List<String>) = uris.forEach { deleteNote(it) }
}