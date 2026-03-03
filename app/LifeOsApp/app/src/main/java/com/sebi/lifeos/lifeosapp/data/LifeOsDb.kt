package com.sebi.lifeos.lifeosapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CategoryEntity::class, AppEntity::class, UsageDayEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LifeOsDb : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun appDao(): AppDao
    abstract fun usageDao(): UsageDao

    companion object {
        @Volatile private var INSTANCE: LifeOsDb? = null

        fun get(context: Context): LifeOsDb =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    LifeOsDb::class.java,
                    "lifeos_detox.db"
                ).build().also { INSTANCE = it }
            }
    }
}
