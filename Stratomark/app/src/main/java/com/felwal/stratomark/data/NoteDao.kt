package com.felwal.stratomark.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface NoteDao {

    // write

    @Insert
    fun addNote(vararg notes: Note)

    @Update
    fun updateNote(vararg notes: Note)

    fun addOrUpdateNote(note: Note) = if (doesNoteExist(note.noteId)) updateNote(note) else addNote(note)

    @Delete
    fun deleteNote(note: Note)

    @Query("DELETE FROM notes WHERE noteId = :noteId")
    fun deleteNote(noteId: Int);

    // read

    @Query("SELECT * FROM notes WHERE noteId = :noteId LIMIT 1")
    fun getNote(noteId: Int): Note?

    @Query("SELECT * FROM notes")
    fun getAllNotes(): List<Note>

    @Query("SELECT * FROM notes WHERE title LIKE :title AND body LIKE :body LIMIT 1")
    fun searchNote(title: String, body: String): Note

    fun doesNoteExist(noteId: Int) = getNote(noteId) != null
}