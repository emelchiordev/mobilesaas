package re.savio.mobile.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_55_56 = object : Migration(55, 56) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE equipment_snapshots
            ADD COLUMN evacuation_mode TEXT
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE equipment_snapshots
            ADD COLUMN hybride_pac_equipment_id TEXT
            """.trimIndent(),
        )
    }
}
