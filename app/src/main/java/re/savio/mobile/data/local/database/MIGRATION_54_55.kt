package re.savio.mobile.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_54_55 = object : Migration(54, 55) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS installation_check (
                interventionId TEXT NOT NULL PRIMARY KEY,
                turbidityTested INTEGER NOT NULL DEFAULT 0,
                turbidityNtu TEXT NOT NULL DEFAULT '',
                turbidityState TEXT,
                gasPipeType TEXT,
                gasPipeValidityDate TEXT NOT NULL DEFAULT '',
                gasPipeReplaced INTEGER NOT NULL DEFAULT 0,
                gasTapCompliant TEXT,
                notes TEXT NOT NULL DEFAULT '',
                updatedAt TEXT NOT NULL DEFAULT '',
                isDirty INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent(),
        )
    }
}
