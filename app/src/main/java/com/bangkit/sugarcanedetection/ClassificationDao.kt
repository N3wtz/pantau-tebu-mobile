package com.bangkit.sugarcanedetection

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ClassificationDao {

    @Insert
    suspend fun insertHistory(classification: ClassificationEntity)

    @Query("SELECT * FROM classification_history ORDER BY id DESC")
    suspend fun getAllHistory(): List<ClassificationEntity>
}