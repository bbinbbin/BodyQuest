package com.bodyquest.app.data.remote

import com.bodyquest.app.data.local.entity.UserEntity
import com.bodyquest.app.data.local.entity.WorkoutEntity
import com.bodyquest.app.data.local.entity.WorkoutSetEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreUserService @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun pushUser(user: UserEntity) {
        val uid = user.firebaseUid ?: return
        val data = mapOf(
            "nickname" to user.nickname,
            "job" to user.job,
            "goal" to user.goal,
            "avatarIndex" to user.avatarIndex,
            "strengthStat" to user.strengthStat,
            "enduranceStat" to user.enduranceStat,
            "balanceStat" to user.balanceStat,
            "xp" to user.xp,
            "level" to user.level,
            "createdAt" to user.createdAt,
            "updatedAt" to System.currentTimeMillis(),
            "email" to user.email,
            "authProvider" to user.authProvider
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
            balanceStat = (doc.getLong("balanceStat") ?: 0).toInt(),
            xp = (doc.getLong("xp") ?: 0).toInt(),
            level = (doc.getLong("level") ?: 1).toInt(),
            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
            firebaseUid = firebaseUid,
            email = doc.getString("email"),
            authProvider = doc.getString("authProvider"),
            updatedAt = doc.getLong("updatedAt") ?: 0
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
                    completed = s["completed"] as? Boolean ?: false,
                    completedAt = s["completedAt"] as? Long
                )
            }

            workout to sets
        }
    }
}
