package re.melchior.saviomobile.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_61_62 = object : Migration(61, 62) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE interventions ADD COLUMN unitVeCoverageUnavailable INTEGER")
        db.execSQL("ALTER TABLE interventions ADD COLUMN unitVeCoverageAttested INTEGER")
        db.execSQL("ALTER TABLE interventions ADD COLUMN unitVeCoverageExpected INTEGER")
        db.execSQL("ALTER TABLE interventions ADD COLUMN unitVeCoverageComplete INTEGER")
    }
}
