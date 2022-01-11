package com.felwal.markana.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.felwal.markana.data.Label
import com.felwal.markana.data.Note
import com.felwal.markana.data.Tree

const val DATABASE_NAME = "markana"

@Database(entities = [Note::class, Tree::class, Label::class], version = 1)
abstract class DbDataSource : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    abstract fun treeDao(): TreeDao

    abstract fun labelDao(): LabelDao

    companion object {

        // for Singleton instantiation
        @Volatile private var instance: DbDataSource? = null

        fun getInstance(applicationContext: Context): DbDataSource =
            instance ?: synchronized(this) {
                instance ?: buildDatabase(applicationContext).also { instance = it }
            }

        private fun buildDatabase(applicationContext: Context): DbDataSource =
            Room.databaseBuilder(applicationContext, DbDataSource::class.java, DATABASE_NAME)
                .build()
    }
}