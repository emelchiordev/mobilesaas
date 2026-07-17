package re.savio.mobile.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Purge des anciens SUBMIT_INVOICE_FULL (remplacés par CLOSE_INTERVENTION). */
val MIGRATION_45_46 = object : Migration(45, 46) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            UPDATE invoices
            SET syncStatus = 'PENDING'
            WHERE id IN (
                SELECT targetId FROM pending_updates
                WHERE type = 'SUBMIT_INVOICE_FULL' AND syncStatus = 'PENDING'
            )
            """.trimIndent(),
        )
        db.execSQL("DELETE FROM pending_updates WHERE type = 'SUBMIT_INVOICE_FULL'")
    }
}
