package com.felwal.markana.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.felwal.markana.prefs.SortBy

@Dao
interface NoteDao {

    // write

    @Insert
    fun addNote(vararg notes: Note)

    @Update
    fun updateNote(vararg notes: Note)

    fun addOrUpdateNote(note: Note) = if (doesNoteExist(note.uri)) updateNote(note) else addNote(note)

    fun addOrUpdateNotes(notes: List<Note>) = notes.forEach { addOrUpdateNote(it) }

    @Delete
    fun deleteNote(vararg notes: Note)

    @Delete
    fun deleteNotes(notes: List<Note>)

    @Query("DELETE FROM notes WHERE treeId = :treeId")
    fun deleteNotes(treeId: Int)

    @Query("DELETE FROM notes WHERE id = :id")
    fun deleteNote(id: Int)

    @Query("DELETE FROM notes WHERE uri = :uri")
    fun deleteNote(uri: String)

    // read

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    fun getNote(id: Int): Note?

    @Query("SELECT * FROM notes WHERE uri = :uri LIMIT 1")
    fun getNote(uri: String): Note?

    @Query("SELECT modified FROM notes WHERE uri = :uri LIMIT 1")
    fun getModified(uri: String): Long?

    @Query("SELECT opened FROM notes WHERE uri = :uri LIMIT 1")
    fun getOpened(uri: String): Long?

    @RawQuery
    fun getNotesQuery(sortQuery: SupportSQLiteQuery?): List<Note>

    fun getAllNotes(sortBy: SortBy, asc: Boolean): List<Note> =
        getNotesQuery(SimpleSQLiteQuery("SELECT * FROM notes" + orderBy(sortBy, asc)))

    @RawQuery
    fun getUrisQuery(sortQuery: SupportSQLiteQuery?): List<String>

    fun getAllUris(sortBy: SortBy, asc: Boolean): List<String> =
        getUrisQuery(SimpleSQLiteQuery("SELECT uri FROM notes" + orderBy(sortBy, asc)))

    @Query("SELECT * FROM notes WHERE filename LIKE :filename AND content LIKE :content LIMIT 1")
    fun searchNote(filename: String, content: String): Note

    fun doesNoteExist(id: Int) = getNote(id) != null

    fun doesNoteExist(uri: String) = getNote(uri) != null

    @Query("SELECT count() FROM notes")
    fun noteCount(): Int

    // tool

    fun orderBy(sortBy: SortBy, asc: Boolean) = " ORDER BY " + when (sortBy) {
        SortBy.NAME -> "filename"
        SortBy.MODIFIED -> "modified"
        SortBy.OPENED -> "opened"
    } + if (asc) " ASC" else " DESC"
}