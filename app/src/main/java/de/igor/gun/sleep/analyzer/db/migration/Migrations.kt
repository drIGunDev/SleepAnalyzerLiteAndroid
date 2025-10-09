package de.igor.gun.sleep.analyzer.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE 'Cache' ADD COLUMN 'duration' REAL NOT NULL DEFAULT 0.0;")
    }
}

val MIGRATION_2_3: Migration = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS 'CacheHypnogram' (
                'id' INTEGER NOT NULL, 
                'sleep_state' INTEGER NOT NULL, 
                'time' TEXT NOT NULL, 
                'series_id' INTEGER NOT NULL,
                PRIMARY KEY('id'),
                FOREIGN KEY('series_id') REFERENCES 'Series'('id') ON UPDATE CASCADE ON DELETE CASCADE);
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS 'index_CacheHypnogram_series_id' ON 'CacheHypnogram' ('series_id');")
    }
}

val MIGRATION_3_4: Migration = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE 'Cache' ADD COLUMN 'max_hr_scaled' REAL NOT NULL DEFAULT 0.0;")
        db.execSQL("ALTER TABLE 'Cache' ADD COLUMN 'min_hr_scaled' REAL NOT NULL DEFAULT 0.0;")
    }
}


val MIGRATION_4_5: Migration = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE 'Cache' ADD COLUMN 'awake' REAL NOT NULL DEFAULT 0.0;")
        db.execSQL("ALTER TABLE 'Cache' ADD COLUMN 'rem' REAL NOT NULL DEFAULT 0.0;")
        db.execSQL("ALTER TABLE 'Cache' ADD COLUMN 'l_seep' REAL NOT NULL DEFAULT 0.0;")
        db.execSQL("ALTER TABLE 'Cache' ADD COLUMN 'd_sleep' REAL NOT NULL DEFAULT 0.0;")
    }
}

val MIGRATION_5_6: Migration = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE 'Cache' RENAME COLUMN 'l_seep' TO 'l_sleep';")
    }
}
