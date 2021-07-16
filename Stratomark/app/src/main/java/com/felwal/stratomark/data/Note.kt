package com.felwal.stratomark.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// must be 0 to enable autoincrement
const val NO_ID = 0

@Entity(tableName = "notes")
data class Note(
    @ColumnInfo(name = "title") var title: String = "",
    @ColumnInfo(name = "body") var body: String = "",
    @ColumnInfo(name = "extension") var extension: String = "txt",
    @PrimaryKey(autoGenerate = true) var noteId: Int = NO_ID
) {

    var selected: Boolean = false

    val titleWithExt: String get() = "$title.$extension"

    init {
        if (extension == "") extension = "txt"
    }

    override fun toString(): String = "$title.$extension: $body"

    fun isEmpty(): Boolean = title == "" && body == ""
}