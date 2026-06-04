package re.melchior.saviomobile.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_51_52 = object : Migration(51, 52) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS anomaly_types (
                id TEXT NOT NULL PRIMARY KEY,
                code TEXT NOT NULL,
                designation TEXT NOT NULL,
                type TEXT NOT NULL,
                level TEXT NOT NULL,
                nomenclature TEXT NOT NULL,
                energyType TEXT NOT NULL,
                isActive INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS anomaly_draft (
                localId TEXT NOT NULL PRIMARY KEY,
                interventionId TEXT NOT NULL,
                unitId TEXT NOT NULL,
                equipmentId TEXT,
                scope TEXT NOT NULL,
                anomalyTypeCode TEXT,
                customDescription TEXT,
                reportedAt TEXT NOT NULL,
                action TEXT,
                syncStatus TEXT NOT NULL DEFAULT 'pending'
            )
            """.trimIndent(),
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_anomaly_draft_interventionId ON anomaly_draft(interventionId)",
        )
    }
}
