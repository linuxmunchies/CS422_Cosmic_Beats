package com.example.musicplayer.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.musicplayer.data.dao.CollectionDao
import com.example.musicplayer.data.entites.Collection

@Database(entities = [Collection::class], version = 1, exportSchema = false)
abstract class CollectionDatabase : RoomDatabase() {

    abstract fun collectionDao(): CollectionDao

    companion object {
        @Volatile
        private var INSTANCE: CollectionDatabase? = null

        fun getDatabase(context: Context): CollectionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CollectionDatabase::class.java,
                    "collection_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
