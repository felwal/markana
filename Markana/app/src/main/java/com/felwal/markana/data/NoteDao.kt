package com.felwal.markana.data

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

    fun addOrUpdateNote(note: Note) = if (doesNoteExist(note.uri)) updateNote(note) else addNote(note)

    @Delete
    fun deleteNote(vararg notes: Note)

    @Delete
    fun deleteNotes(notes: List<Note>)

    @Query("DELETE FROM notes WHERE id = :id")
    fun deleteNote(id: Int);

    @Query("DELETE FROM notes WHERE uri = :uri")
    fun deleteNote(uri: String);

    // read

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    fun getNote(id: Int): Note?

    @Query("SELECT * FROM notes WHERE uri = :uri LIMIT 1")
    fun getNote(uri: String): Note?

    @Query("SELECT * FROM notes")
    fun getAllNotes(): List<Note>

    @Query("SELECT uri FROM notes")
    fun getAllUris(): List<String>

    @Query("SELECT * FROM notes WHERE filename LIKE :filename AND content LIKE :content LIMIT 1")
    fun searchNote(filename: String, content: String): Note

    fun doesNoteExist(id: Int) = getNote(id) != null

    fun doesNoteExist(uri: String) = getNote(uri) != null

    @Query("SELECT count() FROM notes")
    fun noteCount(): Int
}