package com.abhi.jobsapp.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [JobEntity::class], version = 1)
abstract class JobsDatabase : RoomDatabase() {
    abstract fun jobDao(): JobDao

    companion object {
        @Volatile private var INSTANCE: JobsDatabase? = null

        fun getDatabase(context: Context): JobsDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    JobsDatabase::class.java,
                    "jobs_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
