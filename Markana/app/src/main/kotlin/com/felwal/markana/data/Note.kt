package com.felwal.markana.data

import android.content.Context
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.felwal.markana.R
import com.felwal.markana.util.getIntegerArray
import com.felwal.markana.util.multiplyAlphaComponent

const val NO_ID = 0 // must be 0 to enable autoincrement
const val URI_DEFAULT = ""

@Entity(tableName = "notes")
data class Note(
    @ColumnInfo(name = "filename") var filename: String = "",
    @ColumnInfo(name = "content") var content: String = "",
    @ColumnInfo(name = "modified") var modified: Long? = null,
    @ColumnInfo(name = "opened") var opened: Long? = modified,
    @ColumnInfo(name = "pinned") var isPinned: Boolean = false,
    @ColumnInfo(name = "color_index") var colorIndex: Int = 0,
    @ColumnInfo(name = "uri") var uri: String = URI_DEFAULT,
    @ColumnInfo(name = "tree_id") var treeId: Int? = null,
    @PrimaryKey(autoGenerate = true) var id: Int = NO_ID
) {
    @Ignore var isSelected: Boolean = false

    @ColorInt
    fun getColor(c: Context) = c.getIntegerArray(R.array.note_palette)[colorIndex]

    @ColorInt
    fun getBackgroundColor(c: Context) =
        c.getIntegerArray(R.array.note_palette_bg)[colorIndex].multiplyAlphaComponent(0.15f)

    override fun toString(): String = "$filename: $content"
}