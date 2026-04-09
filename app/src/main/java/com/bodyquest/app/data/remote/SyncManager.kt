package com.bodyquest.app.data.remote

import com.bodyquest.app.data.local.dao.BossProgressDao
import com.bodyquest.app.data.local.dao.SkinInventoryDao
import com.bodyquest.app.data.local.dao.UserDao
import com.bodyquest.app.data.local.dao.WorkoutDao
import com.bodyquest.app.data.local.entity.BossProgressEntity
import com.bodyquest.app.data.local.entity.SkinInventoryEntity
import com.bodyquest.app.data.local.entity.UserEntity
import com.bodyquest.app.data.local.entity.WorkoutEntity
import com.bodyquest.app.data.local.entity.WorkoutSetEntity
import com.bodyquest.app.util.AppLogger
import javax.inject.Inject

class SyncManager @Inject constructor(
    private val firestoreService: FirestoreUserService,
    private val userDao: UserDao,
    private val workoutDao: WorkoutDao,
    private val bossProgressDao: BossProgressDao,
    private val skinInventoryDao: SkinInventoryDao
) {

    suspend fun syncOnLogin(firebaseUid: String) {
        try {
            val localUser = userDao.getUserOnce(firebaseUid)
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
                    profileImageUrl = cloudUser.profileImageUrl,
                    updatedAt = cloudUser.updatedAt,
                    gachaTickets = cloudUser.gachaTickets
                )
                userDao.updateUser(updated)
                pullWorkoutsFromCloud(firebaseUid, localUser.id)
            }

            // Boss progress는 항상 pull (updatedAt과 무관하게 동기화)
            pullBossProgressFromCloud(firebaseUid)
            // 인벤토리도 항상 pull
            pullSkinInventoryFromCloud(firebaseUid)
        } catch (e: Exception) {
            AppLogger.w("SyncManager", "로그인 동기화 실패", e)
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
                workoutDao.insertWorkoutWithSets(localWorkout, sets)
            }
        } catch (e: Exception) {
            AppLogger.w("SyncManager", "운동 기록 pull 실패", e)
        }
    }

    private suspend fun pullBossProgressFromCloud(firebaseUid: String) {
        try {
            val cloudProgress = firestoreService.pullAllBossProgress(firebaseUid)
            for (progress in cloudProgress) {
                bossProgressDao.upsert(progress)
            }
        } catch (e: Exception) {
            AppLogger.w("SyncManager", "보스 진행 pull 실패", e)
        }
    }

    private suspend fun pullSkinInventoryFromCloud(firebaseUid: String) {
        try {
            val items = firestoreService.pullAllSkinInventory(firebaseUid)
            for (item in items) {
                skinInventoryDao.upsert(item)
            }
        } catch (e: Exception) {
            AppLogger.w("SyncManager", "인벤토리 pull 실패", e)
        }
    }

    /** @return true if push succeeded */
    suspend fun pushSkinInventoryToCloud(firebaseUid: String, item: SkinInventoryEntity): Boolean {
        return try {
            firestoreService.pushSkinInventory(firebaseUid, item)
            true
        } catch (e: Exception) {
            AppLogger.w("SyncManager", "인벤토리 push 실패", e)
            false
        }
    }

    /** @return true if push succeeded */
    suspend fun pushBossProgressToCloud(firebaseUid: String, progress: BossProgressEntity): Boolean {
        return try {
            firestoreService.pushBossProgress(firebaseUid, progress)
            true
        } catch (e: Exception) {
            AppLogger.w("SyncManager", "보스 진행 push 실패", e)
            false
        }
    }

    /** @return true if push succeeded */
    suspend fun pushUserToCloud(user: UserEntity): Boolean {
        return try {
            firestoreService.pushUser(user)
            true
        } catch (e: Exception) {
            AppLogger.w("SyncManager", "유저 push 실패", e)
            false
        }
    }

    /** @return true if push succeeded */
    suspend fun pushCompletedWorkout(
        firebaseUid: String,
        workout: WorkoutEntity,
        sets: List<WorkoutSetEntity>
    ): Boolean {
        return try {
            if (workout.firestoreId != null) return true // already pushed
            val firestoreId = firestoreService.pushWorkout(firebaseUid, workout, sets)
            // Save firestoreId back to local DB
            workoutDao.updateWorkout(workout.copy(firestoreId = firestoreId))
            true
        } catch (e: Exception) {
            AppLogger.w("SyncManager", "운동 기록 push 실패", e)
            false
        }
    }
}
