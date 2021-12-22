package com.felwal.markana.data

import android.content.Context
import androidx.annotation.ColorInt
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.felwal.android.util.getIntegerArray
import com.felwal.android.util.multiplyAlphaComponent
import com.felwal.markana.R
import com.felwal.markana.util.split

const val ID_AUTO_GENERATE = 0L // must be 0 to enable autoincrement
const val URI_DEFAULT = ""

@Entity(tableName = "notes")
data class Note(
    @ColumnInfo(name = "filename") var filename: String = "",
    @ColumnInfo(name = "content") var content: String = "",
    @ColumnInfo(name = "modified") var modified: Long? = null,
    @ColumnInfo(name = "opened") var opened: Long? = modified,
    @ColumnInfo(name = "pinned") var isPinned: Boolean = false,
    @ColumnInfo(name = "archived") var isArchived: Boolean = false,
    @ColumnInfo(name = "color_index") var colorIndex: Int = 0,
    @ColumnInfo(name = "uri") var uri: String = URI_DEFAULT,
    @ColumnInfo(name = "tree_id") var treeId: Long? = null,
    @PrimaryKey(autoGenerate = true) var id: Long = ID_AUTO_GENERATE
) {
    @Ignore var isSelected: Boolean = false

    val filenameWithoutExtension get() = filename.split(".", limit = 2)[0]

    val extension get() = filename.split(".", lowerLimit = 2, upperLimit = 2)[1]

    @ColorInt
    fun getColor(c: Context): Int {
        val color = c.getIntegerArray(R.array.note_palette)[colorIndex]

        return if (isArchived) color.multiplyAlphaComponent(0.35f)
        else color
    }

    @ColorInt
    fun getBackgroundColor(c: Context): Int {
        val color =
            if (colorIndex == 0) c.getIntegerArray(R.array.note_palette_bg)[colorIndex]
            else c.getIntegerArray(R.array.note_palette_bg)[colorIndex].multiplyAlphaComponent(0.15f)

        return if (isArchived) color.multiplyAlphaComponent(0.35f)
        else color
    }

    override fun toString(): String = "$filename: $content"
}