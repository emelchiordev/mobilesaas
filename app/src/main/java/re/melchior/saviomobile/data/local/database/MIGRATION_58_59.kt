package re.melchior.saviomobile.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_58_59 = object : Migration(58, 59) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE interventions ADD COLUMN timeSlot TEXT NOT NULL DEFAULT 'matin'
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE interventions ADD COLUMN isUrgent INTEGER NOT NULL DEFAULT 0
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE settings ADD COLUMN mobilePlanningPermission TEXT NOT NULL DEFAULT 'LIMITED_EDIT'
            """.trimIndent(),
        )
    }
}
