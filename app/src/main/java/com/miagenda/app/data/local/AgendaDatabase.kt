package com.miagenda.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.miagenda.app.data.local.dao.ActivityDao
import com.miagenda.app.data.local.dao.CategoryDao
import com.miagenda.app.data.local.dao.HabitDao
import com.miagenda.app.data.local.dao.RecurrenceDao
import com.miagenda.app.data.local.dao.ReflectionDao
import com.miagenda.app.data.local.dao.TaskDao
import com.miagenda.app.data.local.entity.ActivityOverrideEntity
import com.miagenda.app.data.local.entity.AgendaActivityEntity
import com.miagenda.app.data.local.entity.AgendaTaskEntity
import com.miagenda.app.data.local.entity.CategoryEntity
import com.miagenda.app.data.local.entity.DailyReflectionEntity
import com.miagenda.app.data.local.entity.HabitEntity
import com.miagenda.app.data.local.entity.HabitLogEntity
import com.miagenda.app.data.local.entity.RecurrenceRuleEntity
import kotlinx.coroutines.Dispatchers

@Database(
    entities = [
        CategoryEntity::class,
        RecurrenceRuleEntity::class,
        AgendaActivityEntity::class,
        ActivityOverrideEntity::class,
        AgendaTaskEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        DailyReflectionEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(AgendaConverters::class)
abstract class AgendaDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun recurrenceDao(): RecurrenceDao
    abstract fun activityDao(): ActivityDao
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun reflectionDao(): ReflectionDao

    companion object {
        const val DATABASE_NAME = "mi_agenda.db"

        fun create(context: Context): AgendaDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AgendaDatabase::class.java,
                DATABASE_NAME
            ).open()
        }

        internal fun createInMemory(context: Context): AgendaDatabase {
            return Room.inMemoryDatabaseBuilder(
                context,
                AgendaDatabase::class.java
            ).open(unitTest = true)
        }

        internal fun createForFile(context: Context, name: String): AgendaDatabase {
            return Room.databaseBuilder(
                context,
                AgendaDatabase::class.java,
                name
            ).open(unitTest = true)
        }

        private fun RoomDatabase.Builder<AgendaDatabase>.open(
            unitTest: Boolean = false
        ): AgendaDatabase {
            if (unitTest) {
                // Estas pruebas no tienen un Looper de Android, y Room lo consulta
                // al abrir una transacción. En el teléfono ese control sigue activo.
                allowMainThreadQueries()
            }
            return setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .addCallback(CategorySeedCallback)
                .build()
        }
    }
}
