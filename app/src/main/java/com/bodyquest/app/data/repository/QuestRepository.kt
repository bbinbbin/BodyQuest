package com.bodyquest.app.data.repository

import com.bodyquest.app.data.local.dao.QuestDao
import com.bodyquest.app.data.local.entity.QuestEntity
import kotlinx.coroutines.flow.Flow

class QuestRepository(private val questDao: QuestDao) {
    fun getQuestsByCategory(category: String): Flow<List<QuestEntity>> =
        questDao.getQuestsByCategory(category)

    fun getQuestsByBodyPart(category: String, bodyPart: String): Flow<List<QuestEntity>> =
        questDao.getQuestsByBodyPart(category, bodyPart)

    fun getQuestsByFilter(category: String, bodyPart: String, difficulty: Int): Flow<List<QuestEntity>> =
        questDao.getQuestsByFilter(category, bodyPart, difficulty)

    suspend fun getQuestById(questId: String): QuestEntity? =
        questDao.getQuestById(questId)

    suspend fun getBodyParts(category: String): List<String> =
        questDao.getBodyParts(category)
}
