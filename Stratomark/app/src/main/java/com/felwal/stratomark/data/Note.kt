package com.felwal.stratomark.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "title") var title: String = "",
    @ColumnInfo(name = "body") var body: String = "",
    @ColumnInfo(name = "extension") var extension: String = "txt"
) {

    val titleWithExt: String get() = "$title.$extension"

    init {
        if (extension == "") extension = "txt"
    }

    override fun toString(): String = "$title.$extension: $body"

    fun isEmpty(): Boolean = title == "" && body == ""
}