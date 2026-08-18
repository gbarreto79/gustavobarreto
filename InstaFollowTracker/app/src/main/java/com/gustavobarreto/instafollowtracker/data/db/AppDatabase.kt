package com.gustavobarreto.instafollowtracker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [SnapshotEntity::class, ProfileRecordEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun snapshotDao(): SnapshotDao
    abstract fun profileRecordDao(): ProfileRecordDao

    companion object {
        private const val DATABASE_NAME = "insta_tracker.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).build().also { instance = it }
            }
        }
    }
}
