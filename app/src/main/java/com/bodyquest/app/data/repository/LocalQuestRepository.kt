package com.bodyquest.app.data.repository

import com.bodyquest.app.data.local.dao.QuestDao
import com.bodyquest.app.data.local.entity.QuestEntity
import kotlinx.coroutines.flow.Flow

class LocalQuestRepository(private val questDao: QuestDao) : QuestRepository {
    override fun getQuestsByCategory(category: String): Flow<List<QuestEntity>> =
        questDao.getQuestsByCategory(category)

    override fun getQuestsByBodyPart(category: String, bodyPart: String): Flow<List<QuestEntity>> =
        questDao.getQuestsByBodyPart(category, bodyPart)

    override fun getQuestsByFilter(category: String, bodyPart: String, difficulty: Int): Flow<List<QuestEntity>> =
        questDao.getQuestsByFilter(category, bodyPart, difficulty)

    override suspend fun getQuestById(questId: String): QuestEntity? =
        questDao.getQuestById(questId)

    override suspend fun getQuestsByIds(questIds: List<String>): List<QuestEntity> =
        questDao.getQuestsByIds(questIds)

    override suspend fun getBodyParts(category: String): List<String> =
        questDao.getBodyParts(category)
}
