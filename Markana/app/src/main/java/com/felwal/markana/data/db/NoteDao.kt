package com.felwal.markana.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.felwal.markana.data.Note
import com.felwal.markana.data.Tree
import com.felwal.markana.data.prefs.SortBy

@Dao
interface NoteDao {

    // write

    fun addNoteIfNotExists(vararg notes: Note) = notes.forEach {
        if (!doesNoteExist(it.uri)) addNote(it)
    }

    fun addOrUpdateNote(vararg notes: Note) = notes.forEach {
        if (doesNoteExist(it.uri)) updateNote(it)
        else addNote(it)
    }

    @Insert
    fun addNote(vararg notes: Note)

    @Update
    fun updateNote(vararg notes: Note)

    @Query("UPDATE notes SET filename = :filename, content = :content WHERE uri = :uri")
    fun updateNote(uri: String, filename: String, content: String)

    @Delete
    fun deleteNote(vararg notes: Note)

    @Query("DELETE FROM notes WHERE uri = :uri")
    fun deleteNote(uri: String)

    @Query("DELETE FROM notes WHERE treeId = :treeId")
    fun deleteNotes(treeId: Int)

    // read

    @Query("SELECT * FROM notes WHERE uri = :uri LIMIT 1")
    fun getNote(uri: String): Note?

    fun getNotes(sortBy: SortBy, asc: Boolean): List<Note> =
        getNotesQuery(SimpleSQLiteQuery("SELECT * FROM notes" + orderBy(sortBy, asc)))

    @Query("SELECT uri FROM notes")
    fun getUris(): List<String>

    fun doesNoteExist(uri: String) = getNote(uri) != null

    @RawQuery
    fun getNotesQuery(sortQuery: SupportSQLiteQuery?): List<Note>

    fun getUris(sortBy: SortBy, asc: Boolean): List<String> =
        getUrisQuery(SimpleSQLiteQuery("SELECT uri FROM notes" + orderBy(sortBy, asc)))

    @RawQuery
    fun getUrisQuery(sortQuery: SupportSQLiteQuery?): List<String>

    @Query("SELECT count() FROM notes")
    fun noteCount(): Int

    // tool

    fun orderBy(sortBy: SortBy, asc: Boolean) = " ORDER BY " + when (sortBy) {
        SortBy.NAME -> "filename"
        SortBy.MODIFIED -> "modified"
        SortBy.OPENED -> "opened"
    } + if (asc) " ASC" else " DESC"
}