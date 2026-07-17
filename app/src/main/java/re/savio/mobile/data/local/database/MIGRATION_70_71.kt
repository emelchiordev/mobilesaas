package re.savio.mobile.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_70_71 = object : Migration(70, 71) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE interventions ADD COLUMN isDemo INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE settings ADD COLUMN demoOnboardingState TEXT",
        )
    }
}
