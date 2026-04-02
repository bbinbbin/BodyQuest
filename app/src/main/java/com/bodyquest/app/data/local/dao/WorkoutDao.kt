package com.bodyquest.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.bodyquest.app.data.local.entity.WorkoutEntity
import com.bodyquest.app.data.local.entity.WorkoutSetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Insert
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Update
    suspend fun updateWorkout(workout: WorkoutEntity)

    @Query("SELECT * FROM workouts WHERE userId = :userId ORDER BY startTime DESC")
    fun getWorkoutHistory(userId: Long): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE userId = :userId AND completed = 1 AND startTime >= :startOfDay ORDER BY startTime DESC")
    fun getTodaysCompletedWorkouts(userId: Long, startOfDay: Long): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE userId = :userId AND completed = 1 AND startTime >= :weekStart ORDER BY startTime DESC")
    fun getWeekWorkouts(userId: Long, weekStart: Long): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    suspend fun getWorkoutById(workoutId: Long): WorkoutEntity?

    @Query("SELECT * FROM workouts WHERE firestoreId = :firestoreId LIMIT 1")
    suspend fun getWorkoutByFirestoreId(firestoreId: String): WorkoutEntity?

    @Insert
    suspend fun insertWorkoutSet(set: WorkoutSetEntity)

    @Update
    suspend fun updateWorkoutSet(set: WorkoutSetEntity)

    @Query("SELECT * FROM workout_sets WHERE workoutId = :workoutId ORDER BY setNumber")
    fun getSetsForWorkout(workoutId: Long): Flow<List<WorkoutSetEntity>>

    @Query("SELECT * FROM workout_sets WHERE workoutId = :workoutId ORDER BY setNumber")
    suspend fun getSetsForWorkoutOnce(workoutId: Long): List<WorkoutSetEntity>

    @Query("SELECT COUNT(*) FROM workouts WHERE userId = :userId AND completed = 1")
    fun getCompletedWorkoutCount(userId: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(xpEarned), 0) FROM workouts WHERE userId = :userId AND completed = 1")
    fun getTotalXpEarned(userId: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(elapsedSeconds), 0) FROM workouts WHERE userId = :userId AND completed = 1")
    fun getTotalElapsedSeconds(userId: Long): Flow<Int>

    @Query("SELECT * FROM workouts WHERE userId = :userId AND completed = 1 ORDER BY startTime DESC LIMIT :limit")
    fun getRecentCompletedWorkouts(userId: Long, limit: Int): Flow<List<WorkoutEntity>>
}
