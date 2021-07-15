package com.felwal.stratomark.data

import android.app.Activity
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.felwal.stratomark.util.DATABASE_NAME

@Database(entities = [Note::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    var onWriteListener: (() -> Unit)? = null

    fun invokeWriteListener(a: Activity) = onWriteListener?.let { a.runOnUiThread(it) }

    abstract fun noteDao(): NoteDao

    companion object {

        // For Singleton instantiation
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