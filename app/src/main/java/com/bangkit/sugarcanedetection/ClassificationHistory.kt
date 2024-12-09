package com.bangkit.sugarcanedetection

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class ClassificationHistory(
    val result: String,
    val confidence: Float
) : java.io.Serializable