package com.bodyquest.app

import android.app.Application
import com.bodyquest.app.data.local.BodyQuestDatabase
import com.bodyquest.app.data.repository.QuestRepository
import com.bodyquest.app.data.repository.UserRepository
import com.bodyquest.app.data.repository.WorkoutRepository

class BodyQuestApp : Application() {
    val database by lazy { BodyQuestDatabase.getDatabase(this) }
    val userRepository by lazy { UserRepository(database.userDao()) }
    val questRepository by lazy { QuestRepository(database.questDao()) }
    val workoutRepository by lazy { WorkoutRepository(database.workoutDao()) }
}
