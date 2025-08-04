package de.igor.gun.sleep.analyzer.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.igor.gun.sleep.analyzer.db.dao.CacheDAO
import de.igor.gun.sleep.analyzer.db.dao.CacheHypnogramDAO
import de.igor.gun.sleep.analyzer.db.dao.MeasurementDAO
import de.igor.gun.sleep.analyzer.db.dao.SeriesDAO
import de.igor.gun.sleep.analyzer.db.entities.Cache
import de.igor.gun.sleep.analyzer.db.entities.CacheHypnogram
import de.igor.gun.sleep.analyzer.db.entities.Measurement
import de.igor.gun.sleep.analyzer.db.entities.Series
import de.igor.gun.sleep.analyzer.db.migration.MIGRATION_1_2
import de.igor.gun.sleep.analyzer.db.migration.MIGRATION_2_3
import de.igor.gun.sleep.analyzer.db.migration.MIGRATION_3_4
import de.igor.gun.sleep.analyzer.db.migration.MIGRATION_4_5
import de.igor.gun.sleep.analyzer.db.tools.Converters


@Database(
    entities = [
        Series::class,
        Measurement::class,
        Cache::class,
        CacheHypnogram::class,
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun seriesDAO(): SeriesDAO
    abstract fun measurementDAO(): MeasurementDAO
    abstract fun cacheDAO(): CacheDAO
    abstract fun cacheHypnogramDAO(): CacheHypnogramDAO

    companion object {
        private const val DB_NAME = "vsData.sqlite"
        private const val INITIAL_DB_PATH = "initialDB/vsData.sqlite"

        fun createDB(context: Context): AppDatabase {
            fun <T : RoomDatabase> Builder<T>.useMigrations() =
                this.addMigrations(MIGRATION_1_2)
                    .addMigrations(MIGRATION_2_3)
                    .addMigrations(MIGRATION_3_4)
                    .addMigrations(MIGRATION_4_5)

            fun buildDB() = Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                DB_NAME
            )
                .useMigrations()
                .build()

            fun buildNewDB() = Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                DB_NAME
            )
                .createFromAsset(INITIAL_DB_PATH)
                .useMigrations()
                .build()

            synchronized(context) {
                if (isDatabaseExist(context)) {
                    return buildDB()
                } else {
                    return buildNewDB()
                }
            }
        }

        private fun isDatabaseExist(context: Context): Boolean {
            val dbFile = context.getDatabasePath(DB_NAME)
            return dbFile.exists()
        }
    }
}