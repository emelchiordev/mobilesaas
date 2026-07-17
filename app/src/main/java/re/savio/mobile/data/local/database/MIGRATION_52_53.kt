package re.savio.mobile.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_52_53 = object : Migration(52, 53) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP INDEX IF EXISTS index_anomaly_draft_interventionId")
        db.execSQL("DROP TABLE IF EXISTS anomaly_draft")
        db.execSQL(
            """
            CREATE TABLE anomaly_draft (
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
            "CREATE INDEX index_anomaly_draft_interventionId ON anomaly_draft(interventionId)",
        )
    }
}
