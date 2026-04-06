package com.bodyquest.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bodyquest.app.data.local.entity.QuestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestDao {
    @Query("SELECT * FROM quests WHERE category = :category")
    fun getQuestsByCategory(category: String): Flow<List<QuestEntity>>

    @Query("SELECT * FROM quests WHERE category = :category AND bodyPart = :bodyPart")
    fun getQuestsByBodyPart(category: String, bodyPart: String): Flow<List<QuestEntity>>

    @Query("SELECT * FROM quests WHERE category = :category AND bodyPart = :bodyPart AND difficulty = :difficulty")
    fun getQuestsByFilter(category: String, bodyPart: String, difficulty: Int): Flow<List<QuestEntity>>

    @Query("SELECT * FROM quests WHERE id = :questId")
    suspend fun getQuestById(questId: String): QuestEntity?

    @Query("SELECT * FROM quests WHERE id IN (:questIds)")
    suspend fun getQuestsByIds(questIds: List<String>): List<QuestEntity>

    @Query("SELECT DISTINCT bodyPart FROM quests WHERE category = :category AND bodyPart IS NOT NULL")
    suspend fun getBodyParts(category: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuests(quests: List<QuestEntity>)
}
