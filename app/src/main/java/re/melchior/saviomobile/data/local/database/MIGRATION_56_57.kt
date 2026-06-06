package re.melchior.saviomobile.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_56_57 = object : Migration(56, 57) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE installation_check
            ADD COLUMN gasPipeAnomalyDeclinedDate TEXT NOT NULL DEFAULT ''
            """.trimIndent(),
        )
    }
}
