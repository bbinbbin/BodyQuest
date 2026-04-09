package com.bodyquest.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.bodyquest.app.data.local.entity.WorkoutEntity
import com.bodyquest.app.data.local.entity.WorkoutSetEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class WorkoutDao {
    @Insert
    abstract suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Update
    abstract suspend fun updateWorkout(workout: WorkoutEntity)

    @Query("SELECT * FROM workouts WHERE userId = :userId ORDER BY startTime DESC")
    abstract fun getWorkoutHistory(userId: Long): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE userId = :userId AND completed = 1 AND startTime >= :startOfDay ORDER BY startTime DESC")
    abstract fun getTodaysCompletedWorkouts(userId: Long, startOfDay: Long): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE userId = :userId AND completed = 1 AND startTime >= :weekStart ORDER BY startTime DESC")
    abstract fun getWeekWorkouts(userId: Long, weekStart: Long): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    abstract suspend fun getWorkoutById(workoutId: Long): WorkoutEntity?

    @Query("SELECT * FROM workouts WHERE firestoreId = :firestoreId LIMIT 1")
    abstract suspend fun getWorkoutByFirestoreId(firestoreId: String): WorkoutEntity?

    @Insert
    abstract suspend fun insertWorkoutSet(set: WorkoutSetEntity)

    /** 운동 1건 + 세트 전체를 원자적으로 삽입 */
    @Transaction
    open suspend fun insertWorkoutWithSets(workout: WorkoutEntity, sets: List<WorkoutSetEntity>): Long {
        val workoutId = insertWorkout(workout)
        for (set in sets) {
            insertWorkoutSet(set.copy(workoutId = workoutId))
        }
        return workoutId
    }

    @Update
    abstract suspend fun updateWorkoutSet(set: WorkoutSetEntity)

    @Query("SELECT * FROM workout_sets WHERE workoutId = :workoutId ORDER BY setNumber")
    abstract fun getSetsForWorkout(workoutId: Long): Flow<List<WorkoutSetEntity>>

    @Query("SELECT * FROM workout_sets WHERE workoutId = :workoutId ORDER BY setNumber")
    abstract suspend fun getSetsForWorkoutOnce(workoutId: Long): List<WorkoutSetEntity>

    @Query("SELECT COUNT(*) FROM workouts WHERE userId = :userId AND completed = 1")
    abstract fun getCompletedWorkoutCount(userId: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(xpEarned), 0) FROM workouts WHERE userId = :userId AND completed = 1")
    abstract fun getTotalXpEarned(userId: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(elapsedSeconds), 0) FROM workouts WHERE userId = :userId AND completed = 1")
    abstract fun getTotalElapsedSeconds(userId: Long): Flow<Int>

    @Query("SELECT * FROM workouts WHERE userId = :userId AND completed = 1 ORDER BY startTime DESC LIMIT :limit")
    abstract fun getRecentCompletedWorkouts(userId: Long, limit: Int): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE userId = :userId AND completed = 1 AND startTime >= :startTime ORDER BY startTime DESC")
    abstract fun getCompletedWorkoutsSince(userId: Long, startTime: Long): Flow<List<WorkoutEntity>>

    @Query("SELECT questId, MAX(startTime) as startTime FROM workouts WHERE userId = :userId AND completed = 1 GROUP BY questId")
    abstract suspend fun getLastCompletionTimes(userId: Long): List<QuestLastDone>
}

data class QuestLastDone(
    val questId: String,
    val startTime: Long
)
