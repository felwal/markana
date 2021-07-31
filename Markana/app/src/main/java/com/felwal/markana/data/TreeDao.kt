package com.felwal.markana.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TreeDao {

    // write

    @Insert
    fun addTree(vararg trees: Tree)

    @Update
    fun updateTree(vararg trees: Tree)

    fun addTreeIfNotExists(tree: Tree) {
        if (!doesTreeExist(tree.uri)) addTree(tree)
    }

    @Delete
    fun deleteTree(vararg trees: Tree)

    @Query("DELETE FROM trees WHERE id = :id")
    fun deleteTree(id: Int);

    // read

    @Query("SELECT * FROM trees")
    fun getAllTrees(): List<Tree>

    @Query("SELECT uri FROM trees")
    fun getAllUris(): List<String>

    @Query("SELECT * FROM trees WHERE uri = :uri LIMIT 1")
    fun getTree(uri: String): Tree?

    fun doesTreeExist(uri: String) = getTree(uri) != null
}