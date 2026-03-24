package com.bodyquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nickname: String,
    val job: String,           // "STRENGTH" | "ENDURANCE" | "BALANCE"
    val goal: String,          // "DIET" | "BULK_UP" | "MAINTAIN"
    val avatarIndex: Int,
    val strengthStat: Int,
    val enduranceStat: Int,
    val balanceStat: Int,
    val xp: Int = 0,
    val level: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val firebaseUid: String? = null,
    val email: String? = null,
    val authProvider: String? = null  // "EMAIL" | "GOOGLE"
)
