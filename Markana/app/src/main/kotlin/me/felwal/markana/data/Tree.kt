package me.felwal.markana.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trees")
class Tree(
    @ColumnInfo(name = "uri") var uri: String,
) {
    @PrimaryKey(autoGenerate = true) var id: Long = ID_AUTO_GENERATE
}