package com.bodyquest.app.di

import android.content.Context
import com.bodyquest.app.data.local.BodyQuestDatabase
import com.bodyquest.app.data.local.dao.QuestDao
import com.bodyquest.app.data.local.dao.UserDao
import com.bodyquest.app.data.local.dao.WorkoutDao
import com.bodyquest.app.data.repository.QuestRepository
import com.bodyquest.app.data.repository.UserRepository
import com.bodyquest.app.data.repository.WorkoutRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BodyQuestDatabase {
        return BodyQuestDatabase.getDatabase(context)
    }

    @Provides
    fun provideUserDao(database: BodyQuestDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideQuestDao(database: BodyQuestDatabase): QuestDao {
        return database.questDao()
    }

    @Provides
    fun provideWorkoutDao(database: BodyQuestDatabase): WorkoutDao {
        return database.workoutDao()
    }

    @Provides
    @Singleton
    fun provideUserRepository(userDao: UserDao): UserRepository {
        return UserRepository(userDao)
    }

    @Provides
    @Singleton
    fun provideQuestRepository(questDao: QuestDao): QuestRepository {
        return QuestRepository(questDao)
    }

    @Provides
    @Singleton
    fun provideWorkoutRepository(workoutDao: WorkoutDao): WorkoutRepository {
        return WorkoutRepository(workoutDao)
    }
}
