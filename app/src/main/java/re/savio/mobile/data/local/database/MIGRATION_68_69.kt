package re.savio.mobile.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_68_69 = object : Migration(68, 69) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS client_financial_summary (
                documentId TEXT NOT NULL PRIMARY KEY,
                clientId TEXT NOT NULL,
                documentType TEXT NOT NULL,
                number TEXT NOT NULL,
                title TEXT,
                emittedAt TEXT NOT NULL,
                statusCode TEXT NOT NULL,
                statusLabel TEXT NOT NULL,
                statusTone TEXT NOT NULL,
                totalTtc REAL NOT NULL,
                syncedAt INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_client_financial_summary_clientId
            ON client_financial_summary(clientId)
            """.trimIndent(),
        )
    }
}
