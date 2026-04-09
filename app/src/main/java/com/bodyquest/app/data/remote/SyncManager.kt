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
            } else if (localUser != null && cloudUser != null) {
                // 양쪽 데이터 존재: 설정은 최신 updatedAt 기준, 스탯은 max merge
                val cloudNewer = cloudUser.updatedAt > localUser.updatedAt
                val updated = localUser.copy(
                    // 설정 데이터: 최신 updatedAt 쪽 사용
                    nickname = if (cloudNewer) cloudUser.nickname else localUser.nickname,
                    job = if (cloudNewer) cloudUser.job else localUser.job,
                    goal = if (cloudNewer) cloudUser.goal else localUser.goal,
                    avatarIndex = if (cloudNewer) cloudUser.avatarIndex else localUser.avatarIndex,
                    profileImageUrl = if (cloudNewer) cloudUser.profileImageUrl else localUser.profileImageUrl,
                    equippedSkinId = if (cloudNewer) cloudUser.equippedSkinId else localUser.equippedSkinId,
                    equippedBottomId = if (cloudNewer) cloudUser.equippedBottomId else localUser.equippedBottomId,
                    equippedHatId = if (cloudNewer) cloudUser.equippedHatId else localUser.equippedHatId,
                    // 누적 스탯: 큰 값 유지 (오프라인 운동 데이터 보존)
                    strengthStat = maxOf(localUser.strengthStat, cloudUser.strengthStat),
                    enduranceStat = maxOf(localUser.enduranceStat, cloudUser.enduranceStat),
                    xp = maxOf(localUser.xp, cloudUser.xp),
                    level = maxOf(localUser.level, cloudUser.level),
                    gachaTickets = maxOf(localUser.gachaTickets, cloudUser.gachaTickets),
                    updatedAt = maxOf(localUser.updatedAt, cloudUser.updatedAt)
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
