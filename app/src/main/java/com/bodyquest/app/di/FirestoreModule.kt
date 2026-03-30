package com.bodyquest.app.di

import com.bodyquest.app.data.local.dao.BossProgressDao
import com.bodyquest.app.data.local.dao.SkinInventoryDao
import com.bodyquest.app.data.local.dao.UserDao
import com.bodyquest.app.data.local.dao.WorkoutDao
import com.bodyquest.app.data.remote.FirestoreUserService
import com.bodyquest.app.data.remote.SyncManager
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirestoreModule {

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirestoreUserService(firestore: FirebaseFirestore): FirestoreUserService =
        FirestoreUserService(firestore)

    @Provides
    @Singleton
    fun provideSyncManager(
        firestoreService: FirestoreUserService,
        userDao: UserDao,
        workoutDao: WorkoutDao,
        bossProgressDao: BossProgressDao,
        skinInventoryDao: SkinInventoryDao
    ): SyncManager = SyncManager(firestoreService, userDao, workoutDao, bossProgressDao, skinInventoryDao)
}
