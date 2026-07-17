package re.savio.mobile.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_50_51 = object : Migration(50, 51) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS contract_tariffs (
                id TEXT NOT NULL PRIMARY KEY,
                contractTypeId TEXT NOT NULL,
                appliesToType TEXT NOT NULL,
                priceHt REAL NOT NULL,
                vatCategory TEXT NOT NULL,
                isActive INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS contract_proposals (
                id TEXT NOT NULL PRIMARY KEY,
                customerId TEXT NOT NULL,
                unitId TEXT NOT NULL,
                status TEXT NOT NULL,
                payloadJson TEXT NOT NULL,
                signatureKey TEXT,
                hamonWaivedExecution INTEGER NOT NULL DEFAULT 0,
                synced INTEGER NOT NULL DEFAULT 0,
                createdAt TEXT NOT NULL
            )
            """.trimIndent(),
        )
    }
}
