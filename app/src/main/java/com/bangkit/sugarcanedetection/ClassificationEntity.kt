package com.bangkit.sugarcanedetection

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "classification_history")
data class ClassificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val result: String,
    val confidence: Float,
    val imagePath: String
)
