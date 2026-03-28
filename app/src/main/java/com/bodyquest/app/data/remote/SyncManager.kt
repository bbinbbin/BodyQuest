package com.bodyquest.app.data.remote

import com.bodyquest.app.data.local.dao.UserDao
import com.bodyquest.app.data.local.dao.WorkoutDao
import com.bodyquest.app.data.local.entity.UserEntity
import com.bodyquest.app.data.local.entity.WorkoutEntity
import com.bodyquest.app.data.local.entity.WorkoutSetEntity
import javax.inject.Inject

class SyncManager @Inject constructor(
    private val firestoreService: FirestoreUserService,
    private val userDao: UserDao,
    private val workoutDao: WorkoutDao
) {

    suspend fun syncOnLogin(firebaseUid: String) {
        try {
            val localUser = userDao.getUserByFirebaseUid(firebaseUid)
            val cloudUser = firestoreService.pullUser(firebaseUid)

            if (localUser == null && cloudUser != null) {
                // New device: pull everything from cloud
                val localId = userDao.insertUser(cloudUser)
                pullWorkoutsFromCloud(firebaseUid, localId)
            } else if (localUser != null && cloudUser != null && cloudUser.updatedAt > localUser.updatedAt) {
                // Cloud is newer: update local user stats
                val updated = localUser.copy(
                    nickname = cloudUser.nickname,
                    job = cloudUser.job,
                    goal = cloudUser.goal,
                    avatarIndex = cloudUser.avatarIndex,
                    strengthStat = cloudUser.strengthStat,
                    enduranceStat = cloudUser.enduranceStat,
                    xp = cloudUser.xp,
                    level = cloudUser.level,
                    updatedAt = cloudUser.updatedAt
                )
                userDao.updateUser(updated)
                pullWorkoutsFromCloud(firebaseUid, localUser.id)
            }
        } catch (_: Exception) {
            // Cloud sync failure should not block login
        }
    }

    private suspend fun pullWorkoutsFromCloud(firebaseUid: String, localUserId: Long) {
        try {
            val cloudWorkouts = firestoreService.pullAllWorkouts(firebaseUid)
            for ((workout, sets) in cloudWorkouts) {
                // Skip if already synced
                val firestoreId = workout.firestoreId ?: continue
                if (workoutDao.getWorkoutByFirestoreId(firestoreId) != null) continue

                val localWorkout = workout.copy(userId = localUserId)
                val workoutId = workoutDao.insertWorkout(localWorkout)

                for (set in sets) {
                    workoutDao.insertWorkoutSet(set.copy(workoutId = workoutId))
                }
            }
        } catch (_: Exception) {
            // Partial sync failure is acceptable
        }
    }

    suspend fun pushUserToCloud(user: UserEntity) {
        try {
            firestoreService.pushUser(user)
        } catch (_: Exception) {
            // Cloud push failure should not affect local operation
        }
    }

    suspend fun pushCompletedWorkout(
        firebaseUid: String,
        workout: WorkoutEntity,
        sets: List<WorkoutSetEntity>
    ) {
        try {
            if (workout.firestoreId != null) return // already pushed
            val firestoreId = firestoreService.pushWorkout(firebaseUid, workout, sets)
            // Save firestoreId back to local DB
            workoutDao.updateWorkout(workout.copy(firestoreId = firestoreId))
        } catch (_: Exception) {
            // Cloud push failure should not affect local operation
        }
    }
}
