package com.bodyquest.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bodyquest.app.data.local.dao.BossDao
import com.bodyquest.app.data.local.dao.BossProgressDao
import com.bodyquest.app.data.local.dao.QuestDao
import com.bodyquest.app.data.local.dao.UserDao
import com.bodyquest.app.data.local.dao.WorkoutDao
import com.bodyquest.app.data.local.dao.SkinInventoryDao
import com.bodyquest.app.data.local.entity.BossEntity
import com.bodyquest.app.data.local.entity.BossProgressEntity
import com.bodyquest.app.data.local.entity.QuestEntity
import com.bodyquest.app.data.local.entity.SkinInventoryEntity
import com.bodyquest.app.data.local.entity.UserEntity
import com.bodyquest.app.data.local.entity.WorkoutEntity
import com.bodyquest.app.data.local.entity.WorkoutSetEntity

@Database(
    entities = [
        UserEntity::class,
        QuestEntity::class,
        WorkoutEntity::class,
        WorkoutSetEntity::class,
        BossEntity::class,
        BossProgressEntity::class,
        SkinInventoryEntity::class
    ],
    version = 13,
    exportSchema = true
)
abstract class BodyQuestDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun questDao(): QuestDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun bossDao(): BossDao
    abstract fun bossProgressDao(): BossProgressDao
    abstract fun skinInventoryDao(): SkinInventoryDao

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

        // v3→v4: 스키마 변경 없음 (fallbackToDestructiveMigration 대신 안전 경로 제공)
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) { /* no-op */ }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE workouts ADD COLUMN firestoreId TEXT")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // FK 제약조건 임시 해제 (workouts → users FK로 인해 DROP TABLE 불가)
                db.execSQL("PRAGMA foreign_keys = OFF")

                // users 테이블 재생성 (balanceStat 제거, SQLite는 DROP COLUMN 미지원)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `users_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `nickname` TEXT NOT NULL,
                        `job` TEXT NOT NULL,
                        `goal` TEXT NOT NULL,
                        `avatarIndex` INTEGER NOT NULL,
                        `strengthStat` INTEGER NOT NULL,
                        `enduranceStat` INTEGER NOT NULL,
                        `xp` INTEGER NOT NULL,
                        `level` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `firebaseUid` TEXT,
                        `email` TEXT,
                        `authProvider` TEXT,
                        `updatedAt` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO `users_new`
                        (id, nickname, job, goal, avatarIndex, strengthStat, enduranceStat,
                         xp, level, createdAt, firebaseUid, email, authProvider, updatedAt)
                    SELECT
                        id, nickname, job, goal, avatarIndex, strengthStat, enduranceStat,
                        xp, level, createdAt, firebaseUid, email, authProvider, updatedAt
                    FROM `users`
                """.trimIndent())
                db.execSQL("DROP TABLE `users`")
                db.execSQL("ALTER TABLE `users_new` RENAME TO `users`")

                // bosses 테이블 신규 생성 (Room이 기대하는 PRIMARY KEY 제약조건 형식)
                db.execSQL("CREATE TABLE IF NOT EXISTS `bosses` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `requiredStrength` INTEGER NOT NULL, `requiredEndurance` INTEGER NOT NULL, `requiredLevel` INTEGER NOT NULL, `type` TEXT NOT NULL, PRIMARY KEY(`id`))")

                // FK 제약조건 복원
                db.execSQL("PRAGMA foreign_keys = ON")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN profileImageUrl TEXT")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `boss_progress` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `bossId` INTEGER NOT NULL,
                        `userId` TEXT NOT NULL,
                        `lastDefeatedAt` INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_boss_progress_userId_bossId` ON `boss_progress` (`userId`, `bossId`)")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // bosses: 재생성 (bossOrder 컬럼 추가, 기존 데이터 삭제 후 seed로 재삽입)
                db.execSQL("DROP TABLE IF EXISTS `bosses`")
                db.execSQL("CREATE TABLE IF NOT EXISTS `bosses` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `requiredStrength` INTEGER NOT NULL, `requiredEndurance` INTEGER NOT NULL, `requiredLevel` INTEGER NOT NULL, `type` TEXT NOT NULL, `bossOrder` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`id`))")

                // boss_progress: 재생성 (composite PK, isCleared 컬럼으로 교체)
                db.execSQL("DROP TABLE IF EXISTS `boss_progress`")
                db.execSQL("CREATE TABLE IF NOT EXISTS `boss_progress` (`bossId` INTEGER NOT NULL, `userId` TEXT NOT NULL, `isCleared` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`userId`, `bossId`))")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `boss_progress` ADD COLUMN `performance` TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `users` ADD COLUMN `equippedSkinId` TEXT")
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `users` ADD COLUMN `gachaTickets` INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `skin_inventory` (
                        `skinId` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `count` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`skinId`, `userId`)
                    )
                """.trimIndent())
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

        private fun insertSeedBosses(db: SupportSQLiteDatabase) {
            seedBosses.forEach { b ->
                db.execSQL(
                    "INSERT OR IGNORE INTO bosses (id, name, requiredStrength, requiredEndurance, requiredLevel, type, bossOrder) VALUES (?,?,?,?,?,?,?)",
                    arrayOf(b.id, b.name, b.requiredStrength, b.requiredEndurance, b.requiredLevel, b.type, b.order)
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13)
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            insertSeedQuests(db)
                            insertSeedBosses(db)
                        }
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            val questCursor = db.query("SELECT COUNT(*) FROM quests")
                            val questCount = if (questCursor.moveToFirst()) questCursor.getInt(0) else 0
                            questCursor.close()
                            if (questCount < seedQuests.size) insertSeedQuests(db)

                            // BALANCE 카테고리 퀘스트의 statType을 BALANCE로 통일
                            db.execSQL("UPDATE quests SET statType = 'BALANCE' WHERE category = 'BALANCE'")

                            val bossCursor = db.query("SELECT COUNT(*) FROM bosses")
                            val bossCount = if (bossCursor.moveToFirst()) bossCursor.getInt(0) else 0
                            bossCursor.close()
                            if (bossCount == 0) insertSeedBosses(db)
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
