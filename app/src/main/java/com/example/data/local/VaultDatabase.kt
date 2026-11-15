package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [VaultEntryEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(VaultConverters::class)
abstract class VaultDatabase : RoomDatabase() {
    abstract fun vaultDao(): VaultDao

    companion object {
        @Volatile
        private var INSTANCE: VaultDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE vault_entries ADD COLUMN watchedAt INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE vault_entries ADD COLUMN releaseDate TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE vault_entries ADD COLUMN language TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE vault_entries ADD COLUMN studioOrNetwork TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getInstance(context: Context): VaultDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    VaultDatabase::class.java,
                    "cinevault_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration(dropAllTables = false)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
