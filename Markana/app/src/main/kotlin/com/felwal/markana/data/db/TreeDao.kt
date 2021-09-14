package com.felwal.markana.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.felwal.markana.data.Tree

@Dao
interface TreeDao {

    // write

    @Insert
    suspend fun addTree(tree: Tree): Long

    @Query("DELETE FROM trees WHERE id = :id")
    suspend fun deleteTree(id: Long);

    // read

    suspend fun doesTreeExist(uri: String) = getTree(uri) != null

    @Query("SELECT * FROM trees WHERE uri = :uri LIMIT 1")
    suspend fun getTree(uri: String): Tree?

    suspend fun doesTreeExistIncludeAsNested(uri: String) = getTreeIncludeAsNested(uri) != null

    /**
     * `"content://com.android.externalstorage.documents/tree/home%3ARepositories%2Ftest%2Ft"`
     *
     * is a subtree to
     *
     * `"content://com.android.externalstorage.documents/tree/home%3ARepositories%2Ftest"`
     */
    @Query("SELECT * FROM trees WHERE :uri LIKE uri || '%' LIMIT 1")
    suspend fun getTreeIncludeAsNested(uri: String): Tree?

    /**
     * `"content://com.android.externalstorage.documents/tree/home%3ARepositories%2Ftest%2Ft/
     * document/home%3ARepositories%2Ftest%2Ft%2Fhej.txt"`
     *
     * `"content://com.android.externalstorage.documents/tree/home%3ARepositories%2Ftest/
     * document/home%3ARepositories%2Ftest%2Ft%2Fhej.txt"`
     */
    @Query("SELECT * FROM trees WHERE uri != :uri AND uri LIKE :uri || '%'")
    suspend fun getNestedTrees(uri: String): List<Tree>

    @Query("SELECT * FROM trees")
    suspend fun getTrees(): List<Tree>
}