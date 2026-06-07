package re.melchior.saviomobile.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_59_60 = object : Migration(59, 60) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS unit_types (
                code TEXT NOT NULL PRIMARY KEY,
                label TEXT NOT NULL,
                category TEXT NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS civility_options (
                code TEXT NOT NULL PRIMARY KEY,
                label TEXT NOT NULL
            )
            """.trimIndent(),
        )
    }
}
