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
import com.felwal.markana.util.toUriPathString

@Dao
interface NoteDao {

    // write

    suspend fun addNoteIfNotExists(note: Note) {
        // the note already exists; dont add
        if (doesNoteExist(note.uri)) return

        // the note already exists independently; copy metadata
        val noteLinkedIndependently = getNoteLinkedIndependently(note.uri.toUriPathString())
        if (noteLinkedIndependently != null) {
            note.apply {
                modified = noteLinkedIndependently.modified
                opened = noteLinkedIndependently.opened
                isPinned = noteLinkedIndependently.isPinned
                colorIndex = noteLinkedIndependently.colorIndex
            }

            // delete original independent to avoid duplicates
            deleteNote(noteLinkedIndependently.uri)
        }

        // the note does not exist
        addNote(note)
    }

    suspend fun addOrUpdateNote(vararg notes: Note) = notes.forEach {
        if (doesNoteExist(it.id)) updateNote(it)
        else addNote(it)
    }

    @Insert
    suspend fun addNote(vararg notes: Note)

    @Update
    suspend fun updateNote(vararg notes: Note)

    @Query("UPDATE notes SET filename = :filename, content = :content WHERE uri = :uri")
    suspend fun updateNoteContent(uri: String, filename: String, content: String)

    @Query("UPDATE notes SET pinned = :isPinned, color_index = :colorIndex WHERE uri = :uri")
    suspend fun updateNoteMetadata(uri: String, isPinned: Boolean, colorIndex: Int)

    @Delete
    suspend fun deleteNote(vararg notes: Note)

    @Query("DELETE FROM notes WHERE uri = :uri")
    suspend fun deleteNote(uri: String)

    @Query("DELETE FROM notes WHERE tree_id = :treeId")
    suspend fun deleteNotes(treeId: Long)

    @Query("DELETE FROM notes WHERE tree_id IS NOT NULL AND tree_id NOT IN (SELECT id from trees)")
    suspend fun deleteNotesInDeletedTrees()

    // read

    suspend fun doesNoteExist(id: Long) = getNote(id) != null

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNote(id: Long): Note?

    suspend fun doesNoteExist(uri: String) = getNote(uri) != null

    @Query("SELECT * FROM notes WHERE uri = :uri LIMIT 1")
    suspend fun getNote(uri: String): Note?

    suspend fun doesNoteExistIncludeInTree(uriPathString: String) = getNoteIncludeInTree(uriPathString) != null

    @Query("SELECT * FROM notes WHERE uri LIKE '%' || :uriPathString LIMIT 1")
    suspend fun getNoteIncludeInTree(uriPathString: String): Note?

    @Query("SELECT * FROM notes WHERE tree_id ISNULL AND uri LIKE '%' || :uriPathString")
    suspend fun getNoteLinkedIndependently(uriPathString: String): Note?

    suspend fun getNotes(sortBy: SortBy, asc: Boolean): List<Note> =
        getNotesQuery(SimpleSQLiteQuery("SELECT * FROM notes" + orderBy(sortBy, asc)))

    suspend fun getNotes(searchQuery: String, sortBy: SortBy, asc: Boolean): List<Note> =
        getNotesQuery(
            SimpleSQLiteQuery(
                "SELECT * FROM notes WHERE " +
                    like(searchQuery, "filename", "content") +
                    orderBy(sortBy, asc)
            )
        )

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

    private fun like(query: String, vararg columns: String) =
        columns.foldIndexed("") { i, acc, col ->
            acc +
                (if (i == 0) "(" else " OR ") +
                "$col LIKE '%$query%'" +
                if (i == columns.size - 1) ")" else ""
        }

    private fun orderBy(sortBy: SortBy, asc: Boolean) =
        " ORDER BY pinned DESC, " +
            when (sortBy) {
                SortBy.NAME -> "lower(filename) ${order(asc)}, filename "
                SortBy.MODIFIED -> "modified "
                SortBy.OPENED -> "opened "
            } + order(asc)

    private fun order(asc: Boolean) = if (asc) "ASC" else "DESC"
}