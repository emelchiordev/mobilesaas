package re.savio.mobile.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_71_72 = object : Migration(71, 72) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE settings ADD COLUMN refrigerantCapacityAttestationNumber TEXT",
        )
        db.execSQL(
            "ALTER TABLE settings ADD COLUMN refrigerantCapacityAttestationIsDemo INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE settings ADD COLUMN refrigerantCapacityAttestationPendingSync INTEGER NOT NULL DEFAULT 0",
        )
    }
}
