package re.savio.mobile.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Ajoute [documentType] explicite (`quote` | `invoice`) sur `invoices`.
 *
 * Backfill = ancienne heuristique CLOSE (signature / acceptedAt). Avant merge, vérifier sur un dump :
 * ```sql
 * SELECT status,
 *   CASE WHEN (devisSignatureUrl IS NOT NULL AND trim(devisSignatureUrl) != '')
 *              OR (acceptedAt IS NOT NULL AND trim(acceptedAt) != '')
 *         THEN 1 ELSE 0 END AS has_devis_evidence,
 *   COUNT(*) AS n
 * FROM invoices
 * GROUP BY 1, 2;
 *
 * -- Ambigu : invoiced/paid avec ou sans preuve devis
 * SELECT id, status, devisSignatureUrl, acceptedAt
 * FROM invoices
 * WHERE status IN ('invoiced', 'paid');
 * ```
 */
val MIGRATION_69_70 = object : Migration(69, 70) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE invoices ADD COLUMN documentType TEXT NOT NULL DEFAULT 'invoice'",
        )
        db.execSQL(
            """
            UPDATE invoices
            SET documentType = 'quote'
            WHERE status NOT IN ('invoiced', 'paid')
              AND (
                (devisSignatureUrl IS NOT NULL AND trim(devisSignatureUrl) != '')
                OR (acceptedAt IS NOT NULL AND trim(acceptedAt) != '')
              )
            """.trimIndent(),
        )
    }
}
