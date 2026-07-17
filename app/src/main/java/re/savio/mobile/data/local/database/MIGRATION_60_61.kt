package re.savio.mobile.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_60_61 = object : Migration(60, 61) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE settings
            ADD COLUMN blockMobileFollowUpResolve INTEGER NOT NULL DEFAULT 0
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE interventions
            ADD COLUMN followUpStatus TEXT NOT NULL DEFAULT 'none'
            """.trimIndent(),
        )
        db.execSQL(
            """
            UPDATE interventions
            SET followUpStatus = 'pending'
            WHERE followUpRequired = 1 AND followUpStatus = 'none'
            """.trimIndent(),
        )
    }
}
