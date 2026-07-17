package re.savio.mobile.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_63_64 = object : Migration(63, 64) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE cold_measures ADD COLUMN supplyContainerId TEXT NOT NULL DEFAULT ''
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE cold_measures ADD COLUMN recoveryContainerId TEXT NOT NULL DEFAULT ''
            """.trimIndent(),
        )
    }
}
