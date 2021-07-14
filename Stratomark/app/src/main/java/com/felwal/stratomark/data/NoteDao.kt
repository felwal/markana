package com.felwal.stratomark.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface NoteDao {

    // writer

    @Insert
    fun add(vararg notes: Note)

    @Update
    fun update(vararg notes: Note)

    fun addOrUpdate(note: Note) = if (exists(note.id)) update(note) else add(note)

    @Delete
    fun delete(note: Note)

    // reader

    @Query("SELECT * FROM notes WHERE id == :id LIMIT 1")
    fun get(id: Int): Note?

    @Query("SELECT * FROM notes")
    fun getAll(): List<Note>

    @Query("SELECT * FROM notes WHERE title LIKE :title AND body LIKE :body LIMIT 1")
    fun search(title: String, body: String): Note

    fun exists(id: Int) = get(id) != null
}