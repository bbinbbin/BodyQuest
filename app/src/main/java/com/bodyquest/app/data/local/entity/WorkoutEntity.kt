package com.bodyquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questId: String,
    val userId: Long,
    val startTime: Long,
    val endTime: Long? = null,
    val elapsedSeconds: Int = 0,
    val caloriesBurned: Int = 0,
    val heartRateAvg: Int = 0,
    val completed: Boolean = false,
    val xpEarned: Int = 0
)
