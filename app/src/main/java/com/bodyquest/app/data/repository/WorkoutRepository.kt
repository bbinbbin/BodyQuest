package com.bodyquest.app.data.repository

import com.bodyquest.app.data.local.dao.WorkoutDao
import com.bodyquest.app.data.local.entity.WorkoutEntity
import com.bodyquest.app.data.local.entity.WorkoutSetEntity
import kotlinx.coroutines.flow.Flow

class WorkoutRepository(private val workoutDao: WorkoutDao) {
    suspend fun startWorkout(workout: WorkoutEntity): Long =
        workoutDao.insertWorkout(workout)

    suspend fun updateWorkout(workout: WorkoutEntity) =
        workoutDao.updateWorkout(workout)

    suspend fun getWorkoutById(workoutId: Long): WorkoutEntity? =
        workoutDao.getWorkoutById(workoutId)

    fun getWorkoutHistory(userId: Long): Flow<List<WorkoutEntity>> =
        workoutDao.getWorkoutHistory(userId)

    fun getTodaysCompletedWorkouts(userId: Long, startOfDay: Long): Flow<List<WorkoutEntity>> =
        workoutDao.getTodaysCompletedWorkouts(userId, startOfDay)

    fun getWeekWorkouts(userId: Long, weekStart: Long): Flow<List<WorkoutEntity>> =
        workoutDao.getWeekWorkouts(userId, weekStart)

    suspend fun insertWorkoutSet(set: WorkoutSetEntity) =
        workoutDao.insertWorkoutSet(set)

    suspend fun updateWorkoutSet(set: WorkoutSetEntity) =
        workoutDao.updateWorkoutSet(set)

    fun getSetsForWorkout(workoutId: Long): Flow<List<WorkoutSetEntity>> =
        workoutDao.getSetsForWorkout(workoutId)
}
