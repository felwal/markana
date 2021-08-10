package com.felwal.markana.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.felwal.markana.data.Tree

@Dao
interface TreeDao {

    // write

    suspend fun addTreeIfNotExists(vararg trees: Tree) = trees.forEach {
        if (!doesTreeExist(it.uri)) addTree(it)
    }

    @Insert
    suspend fun addTree(vararg trees: Tree)

    @Query("DELETE FROM trees WHERE id = :id")
    suspend fun deleteTree(id: Int);

    // read

    @Query("SELECT * FROM trees")
    suspend fun getTrees(): List<Tree>

    suspend fun doesTreeExist(uri: String) = getTree(uri) != null

    @Query("SELECT * FROM trees WHERE uri = :uri LIMIT 1")
    suspend fun getTree(uri: String): Tree?
}