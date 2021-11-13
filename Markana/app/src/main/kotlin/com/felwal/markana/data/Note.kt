package com.felwal.markana.data

import android.content.Context
import androidx.annotation.ColorInt
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.felwal.android.util.getColorAttr
import com.felwal.android.util.getIntegerArray
import com.felwal.android.util.multiplyAlphaComponent
import com.felwal.markana.R

const val ID_AUTO_GENERATE = 0L // must be 0 to enable autoincrement
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
    @ColumnInfo(name = "tree_id") var treeId: Long? = null,
    @PrimaryKey(autoGenerate = true) var id: Long = ID_AUTO_GENERATE
) {
    @Ignore var isSelected: Boolean = false

    @ColorInt
    fun getColor(c: Context) = c.getIntegerArray(R.array.note_palette)[colorIndex]

    @ColorInt
    fun getBackgroundColor(c: Context) =
        if (colorIndex == 0) c.getIntegerArray(R.array.note_palette_bg)[colorIndex]
        else c.getIntegerArray(R.array.note_palette_bg)[colorIndex].multiplyAlphaComponent(0.15f)

    override fun toString(): String = "$filename: $content"
}