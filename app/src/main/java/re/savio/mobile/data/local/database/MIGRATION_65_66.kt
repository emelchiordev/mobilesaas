package re.savio.mobile.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_65_66 = object : Migration(65, 66) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE settings ADD COLUMN vatRegime TEXT NOT NULL DEFAULT 'REAL_NORMAL'
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE settings ADD COLUMN canEditVatRate INTEGER NOT NULL DEFAULT 1
            """.trimIndent(),
        )
    }
}
