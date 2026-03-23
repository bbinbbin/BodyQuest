package com.bodyquest.app.data.repository

import com.bodyquest.app.data.local.dao.WorkoutDao
import com.bodyquest.app.data.local.entity.WorkoutEntity
import com.bodyquest.app.data.local.entity.WorkoutSetEntity
import kotlinx.coroutines.flow.Flow

class LocalWorkoutRepository(private val workoutDao: WorkoutDao) : WorkoutRepository {
    override suspend fun startWorkout(workout: WorkoutEntity): Long =
        workoutDao.insertWorkout(workout)

    override suspend fun updateWorkout(workout: WorkoutEntity) =
        workoutDao.updateWorkout(workout)

    override suspend fun getWorkoutById(workoutId: Long): WorkoutEntity? =
        workoutDao.getWorkoutById(workoutId)

    override fun getWorkoutHistory(userId: Long): Flow<List<WorkoutEntity>> =
        workoutDao.getWorkoutHistory(userId)

    override fun getTodaysCompletedWorkouts(userId: Long, startOfDay: Long): Flow<List<WorkoutEntity>> =
        workoutDao.getTodaysCompletedWorkouts(userId, startOfDay)

    override fun getWeekWorkouts(userId: Long, weekStart: Long): Flow<List<WorkoutEntity>> =
        workoutDao.getWeekWorkouts(userId, weekStart)

    override suspend fun insertWorkoutSet(set: WorkoutSetEntity) =
        workoutDao.insertWorkoutSet(set)

    override suspend fun updateWorkoutSet(set: WorkoutSetEntity) =
        workoutDao.updateWorkoutSet(set)

    override fun getSetsForWorkout(workoutId: Long): Flow<List<WorkoutSetEntity>> =
        workoutDao.getSetsForWorkout(workoutId)
}
