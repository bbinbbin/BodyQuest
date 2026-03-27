package com.bodyquest.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bodyquest.app.data.local.dao.QuestDao
import com.bodyquest.app.data.local.dao.UserDao
import com.bodyquest.app.data.local.dao.WorkoutDao
import com.bodyquest.app.data.local.entity.QuestEntity
import com.bodyquest.app.data.local.entity.UserEntity
import com.bodyquest.app.data.local.entity.WorkoutEntity
import com.bodyquest.app.data.local.entity.WorkoutSetEntity

@Database(
    entities = [
        UserEntity::class,
        QuestEntity::class,
        WorkoutEntity::class,
        WorkoutSetEntity::class
    ],
    version = 5,
    exportSchema = true
)
abstract class BodyQuestDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun questDao(): QuestDao
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: BodyQuestDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Recreate workouts table with FK constraints and indices
                // SQLite doesn't support ALTER TABLE ADD FOREIGN KEY
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `workouts_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `questId` TEXT NOT NULL,
                        `userId` INTEGER NOT NULL,
                        `startTime` INTEGER NOT NULL,
                        `endTime` INTEGER,
                        `elapsedSeconds` INTEGER NOT NULL DEFAULT 0,
                        `caloriesBurned` INTEGER NOT NULL DEFAULT 0,
                        `heartRateAvg` INTEGER NOT NULL DEFAULT 0,
                        `completed` INTEGER NOT NULL DEFAULT 0,
                        `xpEarned` INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`questId`) REFERENCES `quests`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("INSERT INTO `workouts_new` SELECT * FROM `workouts`")
                db.execSQL("DROP TABLE `workouts`")
                db.execSQL("ALTER TABLE `workouts_new` RENAME TO `workouts`")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_workouts_userId_completed_startTime` ON `workouts` (`userId`, `completed`, `startTime`)"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN firebaseUid TEXT")
                db.execSQL("ALTER TABLE users ADD COLUMN email TEXT")
                db.execSQL("ALTER TABLE users ADD COLUMN authProvider TEXT")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_users_firebaseUid ON users (firebaseUid)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE workouts ADD COLUMN firestoreId TEXT")
            }
        }

        private fun insertSeedQuests(db: SupportSQLiteDatabase) {
            seedQuests.forEach { q ->
                db.execSQL(
                    "INSERT OR IGNORE INTO quests (id, category, bodyPart, specificArea, name, description, difficulty, durationMinutes, sets, repsPerSet, xpReward, statType, statReward) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    arrayOf(q.id, q.category, q.bodyPart, q.specificArea, q.name, q.description, q.difficulty, q.durationMinutes, q.sets, q.repsPerSet, q.xpReward, q.statType, q.statReward)
                )
            }
        }

        fun getDatabase(context: Context): BodyQuestDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BodyQuestDatabase::class.java,
                    "bodyquest_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_4_5)
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            insertSeedQuests(db)
                        }
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            // 기존 DB에 퀘스트가 없을 경우 보장
                            val cursor = db.query("SELECT COUNT(*) FROM quests")
                            val count = if (cursor.moveToFirst()) cursor.getInt(0) else 0
                            cursor.close()
                            if (count == 0) insertSeedQuests(db)
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
