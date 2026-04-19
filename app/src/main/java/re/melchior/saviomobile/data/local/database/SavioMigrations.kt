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

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `catalog_nomenclature` (
                `id` TEXT NOT NULL,
                `domain` TEXT NOT NULL,
                `code` TEXT NOT NULL,
                `label` TEXT NOT NULL,
                `isActive` INTEGER NOT NULL,
                `updatedAt` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `catalog_equipment` (
                `id` TEXT NOT NULL,
                `model` TEXT NOT NULL,
                `brandId` TEXT NOT NULL,
                `energyId` TEXT NOT NULL,
                `equipmentTypeId` TEXT NOT NULL,
                `powerKw` REAL,
                `maintenanceDurationHours` REAL,
                `referenceConstructeur` TEXT,
                `noticeUrl` TEXT,
                `isActive` INTEGER NOT NULL,
                `updatedAt` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE equipments ADD COLUMN equipment_catalog_id TEXT")
    }
}

val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        createPendingOperationsTable(db)
    }
}

/** Si une base est déjà en v16 sans cette table (schéma intermédiaire). */
val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(db: SupportSQLiteDatabase) {
        createPendingOperationsTable(db)
    }
}

val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE equipments ADD COLUMN parent_equipment_id TEXT")
    }
}

val MIGRATION_18_19 = object : Migration(18, 19) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS cold_measures (
                id TEXT PRIMARY KEY NOT NULL,
                interventionId TEXT NOT NULL,
                equipmentId TEXT NOT NULL,
                frigo TEXT NOT NULL DEFAULT '',
                charg TEXT NOT NULL DEFAULT '',
                tonnage TEXT NOT NULL DEFAULT '',
                minter1 TEXT NOT NULL DEFAULT '',
                minter2 TEXT NOT NULL DEFAULT '',
                minter3 TEXT NOT NULL DEFAULT '',
                minter4 TEXT NOT NULL DEFAULT '',
                minter5 TEXT NOT NULL DEFAULT '',
                minter6 TEXT NOT NULL DEFAULT '',
                minter7 TEXT NOT NULL DEFAULT '',
                minter8 TEXT NOT NULL DEFAULT '',
                minteraut TEXT NOT NULL DEFAULT '',
                obsern1 TEXT NOT NULL DEFAULT '',
                obsern2 TEXT NOT NULL DEFAULT '',
                detm1 TEXT NOT NULL DEFAULT '',
                dett1 TEXT NOT NULL DEFAULT '',
                detd1 TEXT NOT NULL DEFAULT '',
                autofuite TEXT NOT NULL DEFAULT '',
                qtefri TEXT NOT NULL DEFAULT '',
                qtefri2 TEXT NOT NULL DEFAULT '',
                freqs1 TEXT NOT NULL DEFAULT '',
                freqs2 TEXT NOT NULL DEFAULT '',
                freqs3 TEXT NOT NULL DEFAULT '',
                freqa1 TEXT NOT NULL DEFAULT '',
                freqa2 TEXT NOT NULL DEFAULT '',
                freqa3 TEXT NOT NULL DEFAULT '',
                pasfuite TEXT NOT NULL DEFAULT '',
                fuiteloc1 TEXT NOT NULL DEFAULT '',
                fuiterep1 TEXT NOT NULL DEFAULT '',
                fuiteloc2 TEXT NOT NULL DEFAULT '',
                fuiterep2 TEXT NOT NULL DEFAULT '',
                fuiteloc3 TEXT NOT NULL DEFAULT '',
                fuiterep3 TEXT NOT NULL DEFAULT '',
                fluidrein TEXT NOT NULL DEFAULT '',
                fluidecv TEXT NOT NULL DEFAULT '',
                fluidecr TEXT NOT NULL DEFAULT '',
                fluidecrg TEXT NOT NULL DEFAULT '',
                fluidrecup TEXT NOT NULL DEFAULT '',
                fluidert TEXT NOT NULL DEFAULT '',
                fluideru TEXT NOT NULL DEFAULT '',
                fluiderc TEXT NOT NULL DEFAULT '',
                un1078a TEXT NOT NULL DEFAULT '',
                un1078b TEXT NOT NULL DEFAULT '',
                nomdec TEXT NOT NULL DEFAULT '',
                adres1dec TEXT NOT NULL DEFAULT '',
                adres2dec TEXT NOT NULL DEFAULT '',
                villedec TEXT NOT NULL DEFAULT '',
                nomtrans TEXT NOT NULL DEFAULT '',
                adres1trans TEXT NOT NULL DEFAULT '',
                adres2trans TEXT NOT NULL DEFAULT '',
                villetrans TEXT NOT NULL DEFAULT '',
                fluidobs TEXT NOT NULL DEFAULT '',
                fluidobs2 TEXT NOT NULL DEFAULT '',
                bordeqte TEXT NOT NULL DEFAULT '',
                bordetrans TEXT NOT NULL DEFAULT '',
                instatrait TEXT NOT NULL DEFAULT '',
                coderd TEXT NOT NULL DEFAULT '',
                qterecep TEXT NOT NULL DEFAULT '',
                frigo2 TEXT NOT NULL DEFAULT '',
                bsff TEXT NOT NULL DEFAULT '',
                un3161a TEXT NOT NULL DEFAULT '',
                un3161b TEXT NOT NULL DEFAULT '',
                updated_at TEXT NOT NULL DEFAULT '',
                is_dirty INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS idx_cold_measures_intervention_equipment
            ON cold_measures(interventionId, equipmentId)
            """.trimIndent()
        )
    }
}

val MIGRATION_19_20 = object : Migration(19, 20) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS equipments_old")
        db.execSQL("ALTER TABLE equipments RENAME TO equipments_old")
        db.execSQL(
            """
            CREATE TABLE equipments (
                id TEXT NOT NULL,
                interventionId TEXT NOT NULL,
                brand TEXT,
                model TEXT,
                typeCode TEXT,
                energyCode TEXT,
                serialNumber TEXT,
                installDate TEXT,
                isPrimary INTEGER NOT NULL DEFAULT 0,
                equipment_catalog_id TEXT,
                parent_equipment_id TEXT,
                PRIMARY KEY (id, interventionId)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO equipments (
                id, interventionId, brand, model, typeCode, energyCode, serialNumber,
                installDate, isPrimary, equipment_catalog_id, parent_equipment_id
            )
            SELECT
                id, interventionId, brand, model, typeCode, energyCode, serialNumber,
                installDate, isPrimary, equipment_catalog_id, parent_equipment_id
            FROM equipments_old
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE equipments_old")
    }
}

private fun createPendingOperationsTable(db: SupportSQLiteDatabase) {
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS pending_operations (
            id TEXT PRIMARY KEY NOT NULL,
            type TEXT NOT NULL,
            payload TEXT NOT NULL,
            occurredAt TEXT NOT NULL,
            interventionId TEXT NOT NULL,
            status TEXT NOT NULL DEFAULT 'pending',
            createdAt TEXT NOT NULL
        )
        """.trimIndent()
    )
}
