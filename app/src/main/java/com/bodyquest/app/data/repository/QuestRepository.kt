package com.bodyquest.app.data.repository

import com.bodyquest.app.data.local.entity.QuestEntity
import kotlinx.coroutines.flow.Flow

interface QuestRepository {
    fun getQuestsByCategory(category: String): Flow<List<QuestEntity>>
    fun getQuestsByBodyPart(category: String, bodyPart: String): Flow<List<QuestEntity>>
    fun getQuestsByFilter(category: String, bodyPart: String, difficulty: Int): Flow<List<QuestEntity>>
    suspend fun getQuestById(questId: String): QuestEntity?
    suspend fun getBodyParts(category: String): List<String>
}
