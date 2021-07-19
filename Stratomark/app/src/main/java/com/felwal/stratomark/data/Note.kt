package com.felwal.stratomark.data

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// must be 0 to enable autoincrement
const val NO_ID = 0
const val URI_DEFAULT = ""

@Entity(tableName = "notes")
data class Note(
    @ColumnInfo(name = "filename") var filename: String = "", // TODO: remove from db?
    @ColumnInfo(name = "content") var content: String = "", // TODO: remove from db?
    @ColumnInfo(name = "uri") var uri: String = URI_DEFAULT,
    @PrimaryKey(autoGenerate = true) var id: Int = NO_ID
) {

    //@ColumnInfo var type: String = "txt"

    var selected: Boolean = false

    init {
        //val splits = filename.split(".")
        //type = if (splits.size >= 2) splits.last() else ""
        //title = splits.first()
    }

    override fun toString(): String = "$filename: $content"

    fun isEmpty(): Boolean = filename == "" && content == ""
}