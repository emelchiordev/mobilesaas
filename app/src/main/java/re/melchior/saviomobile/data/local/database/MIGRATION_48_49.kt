package re.melchior.saviomobile.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Contrat (status) + historique VE (completedAsVe). */
val MIGRATION_48_49 = object : Migration(48, 49) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE interventions ADD COLUMN contractStatus TEXT",
        )
        db.execSQL(
            "ALTER TABLE intervention_history ADD COLUMN completedAsVe INTEGER NOT NULL DEFAULT 0",
        )
    }
}
