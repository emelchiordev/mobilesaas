package re.melchior.saviomobile.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_57_58 = object : Migration(57, 58) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE anomaly_draft
            ADD COLUMN corrected INTEGER NOT NULL DEFAULT 0
            """.trimIndent(),
        )
    }
}
