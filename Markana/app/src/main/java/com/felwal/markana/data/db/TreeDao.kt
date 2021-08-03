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

    fun addTreeIfNotExists(vararg trees: Tree) = trees.forEach {
        if (!doesTreeExist(it.uri)) addTree(it)
    }

    @Insert
    fun addTree(vararg trees: Tree)

    @Query("DELETE FROM trees WHERE id = :id")
    fun deleteTree(id: Int);

    // read

    @Query("SELECT * FROM trees")
    fun getTrees(): List<Tree>

    fun doesTreeExist(uri: String) = getTree(uri) != null

    @Query("SELECT * FROM trees WHERE uri = :uri LIMIT 1")
    fun getTree(uri: String): Tree?
}