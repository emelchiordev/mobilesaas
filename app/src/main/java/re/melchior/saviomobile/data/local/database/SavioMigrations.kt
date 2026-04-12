package re.melchior.saviomobile.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `invoice_payments` (
                `id` TEXT NOT NULL,
                `invoiceId` TEXT NOT NULL,
                `amount` REAL NOT NULL,
                `paymentMethodCode` TEXT NOT NULL,
                `paidAt` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
    }
}
