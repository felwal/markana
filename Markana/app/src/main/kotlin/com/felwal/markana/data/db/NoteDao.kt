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
import com.felwal.markana.data.prefs.SortBy

@Dao
interface NoteDao {

    // write

    suspend fun addNoteIfNotExists(vararg notes: Note) = notes.forEach {
        if (!doesNoteExist(it.uri)) addNote(it)
    }

    suspend fun addOrUpdateNote(vararg notes: Note) = notes.forEach {
        if (doesNoteExist(it.uri)) updateNote(it)
        else addNote(it)
    }

    @Insert
    suspend fun addNote(vararg notes: Note)

    @Update
    suspend fun updateNote(vararg notes: Note)

    @Query("UPDATE notes SET filename = :filename, content = :content WHERE uri = :uri")
    suspend fun updateNoteContent(uri: String, filename: String, content: String)

    @Query("UPDATE notes SET pinned = :isPinned WHERE uri = :uri")
    suspend fun updateNoteMetadata(uri: String, isPinned: Boolean)

    @Delete
    suspend fun deleteNote(vararg notes: Note)

    @Query("DELETE FROM notes WHERE uri = :uri")
    suspend fun deleteNote(uri: String)

    @Query("DELETE FROM notes WHERE tree_id = :treeId")
    suspend fun deleteNotes(treeId: Int)

    // read

    suspend fun doesNoteExist(uri: String) = getNote(uri) != null

    @Query("SELECT * FROM notes WHERE uri = :uri LIMIT 1")
    suspend fun getNote(uri: String): Note?

    suspend fun getNotes(sortBy: SortBy, asc: Boolean): List<Note> =
        getNotesQuery(SimpleSQLiteQuery("SELECT * FROM notes" + orderBy(sortBy, asc)))

    @RawQuery
    suspend fun getNotesQuery(sortQuery: SupportSQLiteQuery?): List<Note>

    @Query("SELECT uri FROM notes")
    suspend fun getUris(): List<String>

    suspend fun getUris(sortBy: SortBy, asc: Boolean): List<String> =
        getUrisQuery(SimpleSQLiteQuery("SELECT uri FROM notes" + orderBy(sortBy, asc)))

    @RawQuery
    suspend fun getUrisQuery(sortQuery: SupportSQLiteQuery?): List<String>

    @Query("SELECT count() FROM notes")
    suspend fun noteCount(): Int

    // tool

    private fun orderBy(sortBy: SortBy, asc: Boolean) =
        " ORDER BY pinned DESC, " +
            when (sortBy) {
                SortBy.NAME -> "lower(filename), filename"
                SortBy.MODIFIED -> "modified"
                SortBy.OPENED -> "opened"
            } + order(asc)

    private fun order(asc: Boolean) = if (asc) " ASC" else " DESC"
}