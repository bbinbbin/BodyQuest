package com.bodyquest.app.data.repository

import com.bodyquest.app.data.local.entity.WorkoutEntity
import com.bodyquest.app.data.local.entity.WorkoutSetEntity
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    suspend fun startWorkout(workout: WorkoutEntity): Long
    suspend fun updateWorkout(workout: WorkoutEntity)
    suspend fun getWorkoutById(workoutId: Long): WorkoutEntity?
    fun getWorkoutHistory(userId: Long): Flow<List<WorkoutEntity>>
    fun getTodaysCompletedWorkouts(userId: Long, startOfDay: Long): Flow<List<WorkoutEntity>>
    fun getWeekWorkouts(userId: Long, weekStart: Long): Flow<List<WorkoutEntity>>
    suspend fun insertWorkoutSet(set: WorkoutSetEntity)
    suspend fun updateWorkoutSet(set: WorkoutSetEntity)
    fun getSetsForWorkout(workoutId: Long): Flow<List<WorkoutSetEntity>>
    suspend fun getSetsForWorkoutOnce(workoutId: Long): List<WorkoutSetEntity>
    fun getCompletedWorkoutCount(userId: Long): Flow<Int>
    fun getTotalXpEarned(userId: Long): Flow<Int>
    fun getTotalElapsedSeconds(userId: Long): Flow<Int>
    fun getRecentCompletedWorkouts(userId: Long, limit: Int): Flow<List<WorkoutEntity>>
    fun getCompletedWorkoutsSince(userId: Long, startTime: Long): Flow<List<WorkoutEntity>>
    suspend fun getLastCompletionTimes(userId: Long): Map<String, Long>
}
