package re.melchior.saviomobile.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Réaligne intervention_types sur le schéma Room (sans DEFAULT SQL, index code unique). */
val MIGRATION_47_48 = object : Migration(47, 48) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS intervention_types")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS intervention_types (
                id TEXT NOT NULL PRIMARY KEY,
                code TEXT NOT NULL,
                label TEXT NOT NULL,
                color TEXT,
                isVeType INTEGER NOT NULL,
                isRamonageType INTEGER NOT NULL,
                isSystem INTEGER NOT NULL,
                showOnCreate INTEGER NOT NULL,
                showOnClose INTEGER NOT NULL,
                requireClientSignature INTEGER NOT NULL,
                requireReport INTEGER NOT NULL,
                triggerEquipmentSetup INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_intervention_types_code ON intervention_types(code)",
        )
    }
}
