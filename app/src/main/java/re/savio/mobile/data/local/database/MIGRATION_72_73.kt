package re.savio.mobile.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_72_73 = object : Migration(72, 73) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE cold_measures ADD COLUMN qtefri3 TEXT NOT NULL DEFAULT ''",
        )
    }
}
