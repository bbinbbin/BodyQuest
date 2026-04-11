package com.bodyquest.app.data.remote

import com.bodyquest.app.data.local.entity.BossProgressEntity
import com.bodyquest.app.data.local.entity.SkinInventoryEntity
import com.bodyquest.app.data.local.entity.UserEntity
import com.bodyquest.app.data.local.entity.WorkoutEntity
import com.bodyquest.app.data.local.entity.WorkoutSetEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreUserService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    suspend fun isNicknameTaken(nickname: String): Boolean {
        val snapshot = firestore.collection("users")
            .whereEqualTo("nickname", nickname)
            .limit(1)
            .get()
            .await()
        return !snapshot.isEmpty
    }

    suspend fun deleteUser(firebaseUid: String) {
        require(auth.currentUser?.uid == firebaseUid) { "본인 계정만 삭제할 수 있습니다." }
        val userRef = firestore.collection("users").document(firebaseUid)
        val subcollections = listOf("workouts", "bossProgress", "inventory")
        for (name in subcollections) {
            val docs = userRef.collection(name).get().await().documents
            docs.chunked(500).forEach { chunk ->
                val batch = firestore.batch()
                chunk.forEach { doc -> batch.delete(doc.reference) }
                batch.commit().await()
            }
        }
        userRef.delete().await()
    }

    suspend fun pushUser(user: UserEntity) {
        val uid = user.firebaseUid ?: return
        val data = mapOf(
            "nickname" to user.nickname,
            "job" to user.job,
            "goal" to user.goal,
            "avatarIndex" to user.avatarIndex,
            "strengthStat" to user.strengthStat,
            "enduranceStat" to user.enduranceStat,
            "xp" to user.xp,
            "level" to user.level,
            "createdAt" to user.createdAt,
            "updatedAt" to System.currentTimeMillis(),
            "email" to user.email,
            "authProvider" to user.authProvider,
            "profileImageUrl" to user.profileImageUrl,
            "equippedSkinId" to user.equippedSkinId,
            "equippedBottomId" to user.equippedBottomId,
            "equippedHatId" to user.equippedHatId,
            "gachaTickets" to user.gachaTickets
        )
        firestore.collection("users").document(uid).set(data).await()
    }

    suspend fun pullUser(firebaseUid: String): UserEntity? {
        val doc = firestore.collection("users").document(firebaseUid).get().await()
        if (!doc.exists()) return null
        return UserEntity(
            nickname = doc.getString("nickname") ?: "",
            job = doc.getString("job") ?: "",
            goal = doc.getString("goal") ?: "",
            avatarIndex = (doc.getLong("avatarIndex") ?: 0).toInt(),
            strengthStat = (doc.getLong("strengthStat") ?: 0).toInt(),
            enduranceStat = (doc.getLong("enduranceStat") ?: 0).toInt(),
            xp = (doc.getLong("xp") ?: 0).toInt(),
            level = (doc.getLong("level") ?: 1).toInt(),
            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
            firebaseUid = firebaseUid,
            email = doc.getString("email"),
            authProvider = doc.getString("authProvider"),
            profileImageUrl = doc.getString("profileImageUrl"),
            updatedAt = doc.getLong("updatedAt") ?: 0,
            equippedSkinId = doc.getString("equippedSkinId"),
            equippedBottomId = doc.getString("equippedBottomId"),
            equippedHatId = doc.getString("equippedHatId"),
            gachaTickets = (doc.getLong("gachaTickets") ?: 0).toInt()
        )
    }

    suspend fun pushWorkout(
        firebaseUid: String,
        workout: WorkoutEntity,
        sets: List<WorkoutSetEntity>
    ): String {
        val setsData = sets.map { set ->
            mapOf(
                "setNumber" to set.setNumber,
                "reps" to set.reps,
                "weight" to set.weight,
                "durationSeconds" to set.durationSeconds,
                "completed" to set.completed,
                "completedAt" to set.completedAt
            )
        }
        val data = mapOf(
            "questId" to workout.questId,
            "startTime" to workout.startTime,
            "endTime" to workout.endTime,
            "elapsedSeconds" to workout.elapsedSeconds,
            "caloriesBurned" to workout.caloriesBurned,
            "heartRateAvg" to workout.heartRateAvg,
            "completed" to workout.completed,
            "xpEarned" to workout.xpEarned,
            "sets" to setsData
        )
        val docRef = firestore.collection("users").document(firebaseUid)
            .collection("workouts").add(data).await()
        return docRef.id
    }

    suspend fun pushBossProgress(firebaseUid: String, progress: BossProgressEntity) {
        val data = mapOf(
            "bossId" to progress.bossId,
            "isCleared" to progress.isCleared,
            "performance" to progress.performance
        )
        firestore.collection("users").document(firebaseUid)
            .collection("bossProgress").document(progress.bossId.toString())
            .set(data).await()
    }

    suspend fun pullAllBossProgress(firebaseUid: String): List<BossProgressEntity> {
        val snapshot = firestore.collection("users").document(firebaseUid)
            .collection("bossProgress").get().await()

        return snapshot.documents.mapNotNull { doc ->
            val bossId = (doc.getLong("bossId") ?: return@mapNotNull null).toInt()
            val isCleared = doc.getBoolean("isCleared") ?: false
            val performance = doc.getString("performance") ?: ""
            BossProgressEntity(
                bossId = bossId,
                userId = firebaseUid,
                isCleared = isCleared,
                performance = performance
            )
        }
    }

    suspend fun pushSkinInventory(firebaseUid: String, item: SkinInventoryEntity) {
        val data = mapOf(
            "skinId" to item.skinId,
            "count" to item.count
        )
        firestore.collection("users").document(firebaseUid)
            .collection("inventory").document(item.skinId)
            .set(data).await()
    }

    suspend fun pullAllSkinInventory(firebaseUid: String): List<SkinInventoryEntity> {
        val snapshot = firestore.collection("users").document(firebaseUid)
            .collection("inventory").get().await()

        return snapshot.documents.mapNotNull { doc ->
            val skinId = doc.getString("skinId") ?: return@mapNotNull null
            val count = (doc.getLong("count") ?: 0).toInt()
            SkinInventoryEntity(skinId = skinId, userId = firebaseUid, count = count)
        }
    }

    suspend fun pullAllWorkouts(firebaseUid: String): List<Pair<WorkoutEntity, List<WorkoutSetEntity>>> {
        val snapshot = firestore.collection("users").document(firebaseUid)
            .collection("workouts").get().await()

        return snapshot.documents.mapNotNull { doc ->
            val workout = WorkoutEntity(
                questId = doc.getString("questId") ?: return@mapNotNull null,
                userId = 0, // will be set by SyncManager
                startTime = doc.getLong("startTime") ?: 0,
                endTime = doc.getLong("endTime"),
                elapsedSeconds = (doc.getLong("elapsedSeconds") ?: 0).toInt(),
                caloriesBurned = (doc.getLong("caloriesBurned") ?: 0).toInt(),
                heartRateAvg = (doc.getLong("heartRateAvg") ?: 0).toInt(),
                completed = doc.getBoolean("completed") ?: false,
                xpEarned = (doc.getLong("xpEarned") ?: 0).toInt(),
                firestoreId = doc.id
            )

            @Suppress("UNCHECKED_CAST")
            val setsRaw = doc.get("sets") as? List<Map<String, Any?>> ?: emptyList()
            val sets = setsRaw.map { s ->
                WorkoutSetEntity(
                    workoutId = 0, // will be set by SyncManager
                    setNumber = (s["setNumber"] as? Long ?: 0).toInt(),
                    reps = (s["reps"] as? Long ?: 0).toInt(),
                    weight = (s["weight"] as? Number)?.toDouble() ?: 0.0,
                    durationSeconds = (s["durationSeconds"] as? Long ?: 0).toInt(),
                    completed = s["completed"] as? Boolean ?: false,
                    completedAt = s["completedAt"] as? Long
                )
            }

            workout to sets
        }
    }
}
