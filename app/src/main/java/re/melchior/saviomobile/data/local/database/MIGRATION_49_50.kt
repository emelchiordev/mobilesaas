package re.melchior.saviomobile.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Historique : ajout du champ notes (raison de la demande d'intervention). */
val MIGRATION_49_50 = object : Migration(49, 50) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE intervention_history ADD COLUMN notes TEXT DEFAULT NULL",
        )
    }
}
