package re.savio.mobile.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_67_68 = object : Migration(67, 68) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE invoice_lines ADD COLUMN hasLinePhoto INTEGER NOT NULL DEFAULT 0
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE invoice_lines ADD COLUMN hasArticlePhoto INTEGER NOT NULL DEFAULT 0
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE invoice_lines ADD COLUMN hasEffectivePhoto INTEGER NOT NULL DEFAULT 0
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE invoice_lines ADD COLUMN photoSource TEXT
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE invoice_lines ADD COLUMN photoThumbnailUrl TEXT
            """.trimIndent(),
        )
    }
}
