package re.savio.mobile.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_62_63 = object : Migration(62, 63) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE settings ADD COLUMN profile TEXT NOT NULL DEFAULT 'ARTISAN_SOLO'
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE settings ADD COLUMN policyOverridesJson TEXT NOT NULL DEFAULT '{}'
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE settings ADD COLUMN resolvedInvoices TEXT NOT NULL DEFAULT 'AUTO_ISSUE'
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE settings ADD COLUMN resolvedPlanning TEXT NOT NULL DEFAULT 'FREE'
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE settings ADD COLUMN resolvedPlanningEdit TEXT NOT NULL DEFAULT 'FULL_EDIT'
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE settings ADD COLUMN resolvedFieldModifications TEXT NOT NULL DEFAULT 'AUTO_APPLY'
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE settings ADD COLUMN resolvedClosing TEXT NOT NULL DEFAULT 'TECH_CAN_CLOSE'
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE interventions ADD COLUMN policySnapshotJson TEXT
            """.trimIndent(),
        )
    }
}
