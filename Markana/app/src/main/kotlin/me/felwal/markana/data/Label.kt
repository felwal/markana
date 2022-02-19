package me.felwal.markana.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "labels")
class Label(
    @ColumnInfo(name = "name") var name: String,
) {
    @PrimaryKey(autoGenerate = true) var id: Long = ID_AUTO_GENERATE
}