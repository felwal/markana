package com.felwal.markana.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.felwal.markana.util.DATABASE_NAME

@Database(entities = [Note::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object {

        // for Singleton instantiation
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(applicationContext: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(applicationContext).also { instance = it }
            }
        }

        private fun buildDatabase(applicationContext: Context): AppDatabase =
            Room
                .databaseBuilder(applicationContext, AppDatabase::class.java, DATABASE_NAME)
                .build()
    }
}