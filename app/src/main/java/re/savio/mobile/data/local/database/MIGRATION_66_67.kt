package re.savio.mobile.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_66_67 = object : Migration(66, 67) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE invoice_lines ADD COLUMN tenantArticleId TEXT
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE invoice_lines ADD COLUMN photoIncluded INTEGER NOT NULL DEFAULT 0
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS quote_photos (
                id TEXT NOT NULL PRIMARY KEY,
                invoiceId TEXT NOT NULL,
                localPath TEXT NOT NULL,
                remoteId TEXT,
                remoteThumbnailUrl TEXT,
                syncStatus TEXT NOT NULL DEFAULT 'PENDING',
                errorMessage TEXT,
                takenAt INTEGER NOT NULL,
                deletedLocally INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_quote_photos_invoiceId ON quote_photos(invoiceId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_quote_photos_syncStatus ON quote_photos(syncStatus)")
    }
}
