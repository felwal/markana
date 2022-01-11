package com.felwal.markana.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.felwal.markana.data.Label

@Dao
interface LabelDao {

    // write

    @Insert
    suspend fun addLabel(label: Label): Long

    @Query("DELETE FROM labels WHERE id = :id")
    suspend fun deleteLabel(id: Long)

    @Query("UPDATE labels SET name = :newName WHERE id = :id")
    suspend fun renameLabel(id: Long, newName: String)

    // read

    @Query("SELECT * FROM labels WHERE id = :id LIMIT 1")
    suspend fun getLabel(id: Long): Label?

    @Query("SELECT * FROM labels")
    suspend fun getLabels(): List<Label>

    @Query("SELECT id FROM labels WHERE rowid = :rowId LIMIT 1")
    suspend fun getId(rowId: Int): Long

    @Query("SELECT id FROM labels")
    suspend fun getIds(): List<Long>

    @Query("SELECT count() FROM labels")
    suspend fun labelCount(): Int
}