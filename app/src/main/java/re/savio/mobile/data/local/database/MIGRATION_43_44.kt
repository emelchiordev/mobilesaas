package re.savio.mobile.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_43_44 = object : Migration(43, 44) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE invoices ADD COLUMN hamonSignatureUrl TEXT",
        )
        database.execSQL(
            "ALTER TABLE invoices ADD COLUMN hamonRequested INTEGER NOT NULL DEFAULT 0",
        )
    }
}
