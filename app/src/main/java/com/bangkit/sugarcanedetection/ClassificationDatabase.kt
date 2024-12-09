package com.bangkit.sugarcanedetection

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ClassificationEntity::class], version = 1)
abstract class ClassificationDatabase : RoomDatabase() {

    abstract fun classificationDao(): ClassificationDao

    companion object {
        @Volatile
        private var INSTANCE: ClassificationDatabase? = null

        fun getInstance(context: Context): ClassificationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClassificationDatabase::class.java,
                    "classification_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}