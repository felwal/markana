package com.felwal.markana.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

const val NO_ID = 0 // must be 0 to enable autoincrement
const val URI_DEFAULT = ""

@Entity(tableName = "notes")
data class Note(
    @ColumnInfo(name = "filename") var filename: String = "",
    @ColumnInfo(name = "content") var content: String = "",
    @ColumnInfo(name = "modified") var modified: Long? = null,
    @ColumnInfo(name = "opened") var opened: Long? = modified,
    @ColumnInfo(name = "pinned") var isPinned: Boolean = false,
    @ColumnInfo(name = "uri") var uri: String = URI_DEFAULT,
    @ColumnInfo(name = "tree_id") var treeId: Int? = null,
    @PrimaryKey(autoGenerate = true) var id: Int = NO_ID
) {
    @Ignore var isSelected: Boolean = false

    override fun toString(): String = "$filename: $content"
}