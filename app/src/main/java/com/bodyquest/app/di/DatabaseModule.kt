package com.bodyquest.app.di

import android.content.Context
import com.bodyquest.app.data.local.BodyQuestDatabase
import com.bodyquest.app.data.local.dao.BossDao
import com.bodyquest.app.data.local.dao.BossProgressDao
import com.bodyquest.app.data.local.dao.QuestDao
import com.bodyquest.app.data.local.dao.SkinInventoryDao
import com.bodyquest.app.data.local.dao.UserDao
import com.bodyquest.app.data.local.dao.WorkoutDao
import com.bodyquest.app.data.repository.BossRepository
import com.bodyquest.app.data.repository.LocalBossRepository
import com.bodyquest.app.data.repository.LocalQuestRepository
import com.bodyquest.app.data.repository.LocalSkinInventoryRepository
import com.bodyquest.app.data.repository.LocalUserRepository
import com.bodyquest.app.data.repository.LocalWorkoutRepository
import com.bodyquest.app.data.repository.QuestRepository
import com.bodyquest.app.data.repository.SkinInventoryRepository
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
        return LocalUserRepository(userDao)
    }

    @Provides
    @Singleton
    fun provideQuestRepository(questDao: QuestDao): QuestRepository {
        return LocalQuestRepository(questDao)
    }

    @Provides
    @Singleton
    fun provideWorkoutRepository(workoutDao: WorkoutDao): WorkoutRepository {
        return LocalWorkoutRepository(workoutDao)
    }

    @Provides
    fun provideBossDao(database: BodyQuestDatabase): BossDao {
        return database.bossDao()
    }

    @Provides
    fun provideBossProgressDao(database: BodyQuestDatabase): BossProgressDao {
        return database.bossProgressDao()
    }

    @Provides
    @Singleton
    fun provideBossRepository(bossDao: BossDao, bossProgressDao: BossProgressDao): BossRepository {
        return LocalBossRepository(bossDao, bossProgressDao)
    }

    @Provides
    fun provideSkinInventoryDao(database: BodyQuestDatabase): SkinInventoryDao {
        return database.skinInventoryDao()
    }

    @Provides
    @Singleton
    fun provideSkinInventoryRepository(skinInventoryDao: SkinInventoryDao): SkinInventoryRepository {
        return LocalSkinInventoryRepository(skinInventoryDao)
    }
}
