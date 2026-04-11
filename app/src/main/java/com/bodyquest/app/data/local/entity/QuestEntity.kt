package com.bodyquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quests")
data class QuestEntity(
    @PrimaryKey val id: String,
    val category: String,          // "STRENGTH" | "ENDURANCE" | "BALANCE"
    val bodyPart: String?,         // "chest", "back", "legs", etc.
    val specificArea: String?,     // more detail
    val name: String,
    val description: String,
    val difficulty: Int,           // 1=초급, 2=중급, 3=고급
    val durationMinutes: Int,
    val sets: Int,
    val repsPerSet: Int,
    val xpReward: Int,
    val statType: String,          // "STRENGTH" | "ENDURANCE" | "BALANCE"
    val statReward: Int,
    val inputType: String = "WEIGHT_REPS"  // ExerciseInputType: WEIGHT_REPS | REPS_ONLY | TIME_ONLY | MIXED
)
