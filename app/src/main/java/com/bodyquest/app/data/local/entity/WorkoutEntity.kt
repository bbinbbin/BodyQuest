package com.bodyquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workouts",
    indices = [
        Index(value = ["userId", "completed", "startTime"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = QuestEntity::class,
            parentColumns = ["id"],
            childColumns = ["questId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
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
    val xpEarned: Int = 0,
    val firestoreId: String? = null
)
