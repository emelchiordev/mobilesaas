package re.savio.mobile.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_42_43 = object : Migration(42, 43) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE invoices ADD COLUMN acceptedAt TEXT",
        )
        database.execSQL(
            "ALTER TABLE invoices ADD COLUMN invoicedAt TEXT",
        )
        database.execSQL(
            "ALTER TABLE invoices ADD COLUMN paidAt TEXT",
        )
        database.execSQL(
            "ALTER TABLE invoices ADD COLUMN devisSignatureUrl TEXT",
        )
    }
}
