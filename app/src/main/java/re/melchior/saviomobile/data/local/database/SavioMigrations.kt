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

val MIGRATION_20_21 = object : Migration(20, 21) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS equipment_snapshots (
                equipmentId TEXT NOT NULL,
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
                created_at TEXT NOT NULL DEFAULT '',
                PRIMARY KEY (equipmentId, interventionId)
            )
            """.trimIndent(),
        )
    }
}

val MIGRATION_21_22 = object : Migration(21, 22) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE equipments ADD COLUMN catalog_brand_id TEXT")
        db.execSQL("ALTER TABLE equipment_snapshots ADD COLUMN catalog_brand_id TEXT")
    }
}

val MIGRATION_22_23 = object : Migration(22, 23) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE equipments_new (
                id TEXT NOT NULL PRIMARY KEY,
                interventionId TEXT NOT NULL DEFAULT '',
                unitId TEXT,
                `order` INTEGER,
                brand TEXT,
                model TEXT,
                typeCode TEXT,
                energyCode TEXT,
                serialNumber TEXT,
                installDate TEXT,
                isPrimary INTEGER NOT NULL DEFAULT 0,
                equipment_catalog_id TEXT,
                catalog_brand_id TEXT,
                parent_equipment_id TEXT
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO equipments_new (
                id, interventionId, unitId, `order`, brand, model, typeCode, energyCode,
                serialNumber, installDate, isPrimary, equipment_catalog_id, catalog_brand_id,
                parent_equipment_id
            )
            SELECT
                id, interventionId, NULL, NULL, brand, model, typeCode, energyCode,
                serialNumber, installDate, isPrimary, equipment_catalog_id, catalog_brand_id,
                parent_equipment_id
            FROM equipments
            GROUP BY id
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE equipments")
        db.execSQL("ALTER TABLE equipments_new RENAME TO equipments")

        db.execSQL("ALTER TABLE equipment_snapshots ADD COLUMN `order` INTEGER")
        db.execSQL("ALTER TABLE equipment_snapshots ADD COLUMN unitId TEXT")
    }
}

val MIGRATION_24_25 = object : Migration(24, 25) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS attestation_ve (
                id TEXT NOT NULL PRIMARY KEY,
                interventionId TEXT NOT NULL,
                equipmentOrder INTEGER NOT NULL,
                type TEXT NOT NULL,
                appareilMesure TEXT NOT NULL DEFAULT '',
                defautsCorriges TEXT NOT NULL DEFAULT '',
                recommandationUsage TEXT NOT NULL DEFAULT '',
                recommandationAmeliorations TEXT NOT NULL DEFAULT '',
                recommandationRemplacement TEXT NOT NULL DEFAULT '',
                commentaire TEXT NOT NULL DEFAULT '',
                nomPersonnePresente TEXT NOT NULL DEFAULT '',
                remarquesHydraulique TEXT NOT NULL DEFAULT '',
                remarquesRegulation TEXT NOT NULL DEFAULT '',
                remarquesGenerateur TEXT NOT NULL DEFAULT '',
                co TEXT NOT NULL DEFAULT '',
                tempFumees TEXT NOT NULL DEFAULT '',
                tempAmbiante TEXT NOT NULL DEFAULT '',
                co2Fumees TEXT NOT NULL DEFAULT '',
                o2Fumees TEXT NOT NULL DEFAULT '',
                rendementEvalue TEXT NOT NULL DEFAULT '',
                noxEmissions TEXT NOT NULL DEFAULT '',
                classeEnergetique TEXT NOT NULL DEFAULT '',
                indiceNoircissement TEXT NOT NULL DEFAULT '',
                pressionGicleur TEXT NOT NULL DEFAULT '',
                emissionsPoussieres TEXT NOT NULL DEFAULT '',
                emissionsCov TEXT NOT NULL DEFAULT '',
                tExterieurChauf TEXT NOT NULL DEFAULT '',
                tExterieurRefroid TEXT NOT NULL DEFAULT '',
                tInterieurChauf TEXT NOT NULL DEFAULT '',
                tInterieurRefroid TEXT NOT NULL DEFAULT '',
                tensionStatique TEXT NOT NULL DEFAULT '',
                tensionDynamique TEXT NOT NULL DEFAULT '',
                fluideRef TEXT NOT NULL DEFAULT '',
                chargeTotale TEXT NOT NULL DEFAULT '',
                pressionBp TEXT NOT NULL DEFAULT '',
                pressionHp TEXT NOT NULL DEFAULT '',
                isDirty INTEGER NOT NULL DEFAULT 1,
                updatedAt TEXT NOT NULL DEFAULT ''
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS attestation_ve_point_controle (
                id TEXT NOT NULL PRIMARY KEY,
                attestationId TEXT NOT NULL,
                interventionId TEXT NOT NULL,
                equipmentOrder INTEGER NOT NULL,
                type TEXT NOT NULL,
                cle TEXT NOT NULL,
                resultat TEXT NOT NULL DEFAULT ''
            )
            """.trimIndent(),
        )
    }
}

val MIGRATION_23_24 = object : Migration(23, 24) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS measures (
                interventionId TEXT NOT NULL,
                equipmentOrder INTEGER NOT NULL,
                co REAL,
                coamb REAL,
                co2 REAL,
                o2 REAL,
                tair INTEGER,
                temfu REAL,
                rend REAL,
                nox REAL,
                eta REAL,
                thpa REAL,
                no REAL,
                no2 REAL,
                o2ven REAL,
                condilu REAL,
                tgaz INTEGER,
                ta REAL,
                pregas REAL,
                prega REAL,
                pregn REAL,
                pregm REAL,
                puisgaz REAL,
                debga REAL,
                temec REAL,
                temef REAL,
                delta REAL,
                debio REAL,
                debfuel REAL,
                prefp REAL,
                puisfuel REAL,
                pulve REAL,
                spot INTEGER,
                testdsc TEXT,
                remplacond TEXT,
                templagigleur TEXT,
                remplapoly TEXT,
                etaventil TEXT,
                ctranode TEXT,
                ctrextvmc TEXT,
                suie1 INTEGER,
                suie2 INTEGER,
                suie3 INTEGER,
                residhuil INTEGER,
                opaci REAL,
                ionis REAL,
                pgevg REAL,
                pgepg REAL,
                depre REAL,
                depr2 REAL,
                gican REAL,
                gicle REAL,
                pabs REAL,
                perte REAL,
                ppm INTEGER,
                obser TEXT,
                updated_at TEXT NOT NULL DEFAULT '',
                is_dirty INTEGER NOT NULL DEFAULT 1,
                PRIMARY KEY (interventionId, equipmentOrder)
            )
            """.trimIndent(),
        )
    }
}

val MIGRATION_25_26 = object : Migration(25, 26) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE interventions ADD COLUMN isChantier INTEGER NOT NULL DEFAULT 0",
        )
    }
}

val MIGRATION_26_27 = object : Migration(26, 27) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE equipments ADD COLUMN evacuationMode TEXT")
        db.execSQL("ALTER TABLE attestation_ve ADD COLUMN coConduitPpm REAL")
    }
}

val MIGRATION_27_28 = object : Migration(27, 28) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS equipments_new (
                interventionId TEXT NOT NULL,
                `order` INTEGER NOT NULL,
                id TEXT NOT NULL,
                unitId TEXT NOT NULL,
                brand TEXT,
                model TEXT,
                typeCode TEXT,
                energyCode TEXT,
                serialNumber TEXT,
                installDate TEXT,
                isPrimary INTEGER NOT NULL DEFAULT 0,
                equipment_catalog_id TEXT,
                catalog_brand_id TEXT,
                parent_equipment_id TEXT,
                evacuationMode TEXT,
                syncStatus TEXT NOT NULL DEFAULT 'SYNCED',
                isChantier INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY (interventionId, `order`)
            )
            """.trimIndent(),
        )
        database.execSQL(
            """
            INSERT OR IGNORE INTO equipments_new (
                interventionId, `order`, id, unitId, brand, model, typeCode, energyCode,
                serialNumber, installDate, isPrimary, equipment_catalog_id, catalog_brand_id,
                parent_equipment_id, evacuationMode, syncStatus, isChantier
            )
            SELECT
                interventionId,
                COALESCE(`order`, 0),
                id,
                COALESCE(unitId, ''),
                brand, model, typeCode, energyCode,
                serialNumber, installDate, isPrimary,
                equipment_catalog_id, catalog_brand_id, parent_equipment_id,
                evacuationMode,
                'SYNCED',
                0
            FROM equipments
            """.trimIndent(),
        )
        database.execSQL("DROP TABLE equipments")
        database.execSQL("ALTER TABLE equipments_new RENAME TO equipments")
    }
}

val MIGRATION_28_29 = object : Migration(28, 29) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE attestation_ve_new (
                interventionId TEXT NOT NULL,
                equipmentOrder INTEGER NOT NULL,
                id TEXT NOT NULL,
                type TEXT NOT NULL,
                appareilMesure TEXT NOT NULL DEFAULT '',
                defautsCorriges TEXT NOT NULL DEFAULT '',
                recommandationUsage TEXT NOT NULL DEFAULT '',
                recommandationAmeliorations TEXT NOT NULL DEFAULT '',
                recommandationRemplacement TEXT NOT NULL DEFAULT '',
                commentaire TEXT NOT NULL DEFAULT '',
                nomPersonnePresente TEXT NOT NULL DEFAULT '',
                remarquesHydraulique TEXT NOT NULL DEFAULT '',
                remarquesRegulation TEXT NOT NULL DEFAULT '',
                remarquesGenerateur TEXT NOT NULL DEFAULT '',
                co TEXT NOT NULL DEFAULT '',
                coConduitPpm REAL,
                tempFumees TEXT NOT NULL DEFAULT '',
                tempAmbiante TEXT NOT NULL DEFAULT '',
                co2Fumees TEXT NOT NULL DEFAULT '',
                o2Fumees TEXT NOT NULL DEFAULT '',
                rendementEvalue TEXT NOT NULL DEFAULT '',
                noxEmissions TEXT NOT NULL DEFAULT '',
                classeEnergetique TEXT NOT NULL DEFAULT '',
                indiceNoircissement TEXT NOT NULL DEFAULT '',
                pressionGicleur TEXT NOT NULL DEFAULT '',
                emissionsPoussieres TEXT NOT NULL DEFAULT '',
                emissionsCov TEXT NOT NULL DEFAULT '',
                tExterieurChauf TEXT NOT NULL DEFAULT '',
                tExterieurRefroid TEXT NOT NULL DEFAULT '',
                tInterieurChauf TEXT NOT NULL DEFAULT '',
                tInterieurRefroid TEXT NOT NULL DEFAULT '',
                tensionStatique TEXT NOT NULL DEFAULT '',
                tensionDynamique TEXT NOT NULL DEFAULT '',
                fluideRef TEXT NOT NULL DEFAULT '',
                chargeTotale TEXT NOT NULL DEFAULT '',
                pressionBp TEXT NOT NULL DEFAULT '',
                pressionHp TEXT NOT NULL DEFAULT '',
                isDirty INTEGER NOT NULL DEFAULT 1,
                updatedAt TEXT NOT NULL DEFAULT '',
                PRIMARY KEY(interventionId, equipmentOrder)
            )
            """.trimIndent(),
        )
        database.execSQL(
            """
            INSERT OR IGNORE INTO attestation_ve_new
            SELECT interventionId, equipmentOrder, id, type,
                   appareilMesure, defautsCorriges, recommandationUsage,
                   recommandationAmeliorations, recommandationRemplacement,
                   commentaire, nomPersonnePresente, remarquesHydraulique,
                   remarquesRegulation, remarquesGenerateur,
                   co, coConduitPpm, tempFumees, tempAmbiante,
                   co2Fumees, o2Fumees, rendementEvalue, noxEmissions,
                   classeEnergetique, indiceNoircissement, pressionGicleur,
                   emissionsPoussieres, emissionsCov,
                   tExterieurChauf, tExterieurRefroid,
                   tInterieurChauf, tInterieurRefroid,
                   tensionStatique, tensionDynamique,
                   fluideRef, chargeTotale, pressionBp, pressionHp,
                   isDirty, updatedAt
            FROM attestation_ve
            ORDER BY updatedAt DESC
            """.trimIndent(),
        )
        database.execSQL("DROP TABLE attestation_ve")
        database.execSQL("ALTER TABLE attestation_ve_new RENAME TO attestation_ve")
    }
}

val MIGRATION_29_30 = object : Migration(29, 30) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE attestation_ve ADD COLUMN bruleurMarque TEXT",
        )
        database.execSQL(
            "ALTER TABLE attestation_ve ADD COLUMN bruleurModele TEXT",
        )
        database.execSQL(
            "ALTER TABLE attestation_ve ADD COLUMN bruleurSerialNumber TEXT",
        )
        database.execSQL(
            "ALTER TABLE attestation_ve ADD COLUMN bruleurCommissioningDate TEXT",
        )
        database.execSQL(
            "ALTER TABLE attestation_ve ADD COLUMN bruleurPuissanceKw REAL",
        )
    }
}

val MIGRATION_30_31 = object : Migration(30, 31) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            ALTER TABLE attestation_ve
            ADD COLUMN bruleurEquipmentOrder INTEGER
            """.trimIndent(),
        )
    }
}

val MIGRATION_31_32 = object : Migration(31, 32) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE equipments ADD COLUMN power_kw TEXT")
    }
}

val MIGRATION_32_33 = object : Migration(32, 33) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE equipments ADD COLUMN hybride_pac_equipment_id TEXT")
    }
}

/** JSON (tableau d’ordres) — aligné sur l’entité [AttestationVeEntity.linkedEquipmentOrders]. */
val MIGRATION_33_34 = object : Migration(33, 34) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            ALTER TABLE attestation_ve
            ADD COLUMN linkedEquipmentOrders TEXT NOT NULL DEFAULT ''
            """.trimIndent(),
        )
    }
}

val MIGRATION_34_35 = object : Migration(34, 35) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            ALTER TABLE attestation_ve
            ADD COLUMN appareilMesureTension TEXT NOT NULL DEFAULT ''
            """.trimIndent(),
        )
    }
}

val MIGRATION_35_36 = object : Migration(35, 36) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            ALTER TABLE attestation_ve
            ADD COLUMN appareilMesureGenerateur TEXT NOT NULL DEFAULT ''
            """.trimIndent(),
        )
        database.execSQL(
            """
            UPDATE attestation_ve
            SET appareilMesureGenerateur = appareilMesure
            WHERE type IN ('PAC_HYBRIDE_GAZ', 'PAC_HYBRIDE_FIOUL')
              AND trim(appareilMesure) <> ''
            """.trimIndent(),
        )
    }
}

val MIGRATION_36_37 = object : Migration(36, 37) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            ALTER TABLE equipments
            ADD COLUMN attrsJson TEXT
            """.trimIndent(),
        )
    }
}

val MIGRATION_37_38 = object : Migration(37, 38) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS pac_measures (
                interventionId TEXT NOT NULL,
                equipmentOrder INTEGER NOT NULL,
                pacVentilation TEXT NOT NULL DEFAULT '',
                pacNetail TEXT NOT NULL DEFAULT '',
                pacVerail TEXT NOT NULL DEFAULT '',
                pacFiltre TEXT NOT NULL DEFAULT '',
                pacFuite TEXT NOT NULL DEFAULT '',
                pacEvac TEXT NOT NULL DEFAULT '',
                pacPression1 TEXT NOT NULL DEFAULT '',
                pacPression2 TEXT NOT NULL DEFAULT '',
                pacGlycol1 TEXT NOT NULL DEFAULT '',
                pacGlycol2 TEXT NOT NULL DEFAULT '',
                pacTenStat TEXT NOT NULL DEFAULT '',
                pacTenDyna TEXT NOT NULL DEFAULT '',
                pacIntensite TEXT NOT NULL DEFAULT '',
                pacResserage1 TEXT NOT NULL DEFAULT '',
                pacResserage2 TEXT NOT NULL DEFAULT '',
                pacInterieure TEXT NOT NULL DEFAULT '',
                pacExterieure TEXT NOT NULL DEFAULT '',
                pacDepart TEXT NOT NULL DEFAULT '',
                pacRetour TEXT NOT NULL DEFAULT '',
                pacDeltaT TEXT NOT NULL DEFAULT '',
                pacHiver TEXT NOT NULL DEFAULT '',
                pacAppoint TEXT NOT NULL DEFAULT '',
                pacConfort TEXT NOT NULL DEFAULT '',
                pacNonChauf TEXT NOT NULL DEFAULT '',
                pacEcsConsigne TEXT NOT NULL DEFAULT '',
                pacEcs TEXT NOT NULL DEFAULT '',
                pacManometreBp TEXT NOT NULL DEFAULT '',
                pacManometreHp TEXT NOT NULL DEFAULT '',
                pacDegivrage TEXT NOT NULL DEFAULT '',
                pacInversion TEXT NOT NULL DEFAULT '',
                pacHFonct TEXT NOT NULL DEFAULT '',
                pacHComp1 TEXT NOT NULL DEFAULT '',
                pacHVenti TEXT NOT NULL DEFAULT '',
                pacNbDemarr TEXT NOT NULL DEFAULT '',
                pacHAppoint1 TEXT NOT NULL DEFAULT '',
                pacHAppoint2 TEXT NOT NULL DEFAULT '',
                pacAlarme1 TEXT NOT NULL DEFAULT '',
                pacAlarme2 TEXT NOT NULL DEFAULT '',
                pacBlocage1 TEXT NOT NULL DEFAULT '',
                pacBlocage2 TEXT NOT NULL DEFAULT '',
                pacReleve TEXT NOT NULL DEFAULT '',
                pacRem1 TEXT NOT NULL DEFAULT '',
                updated_at TEXT NOT NULL DEFAULT '',
                is_dirty INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY (interventionId, equipmentOrder)
            )
            """.trimIndent(),
        )
    }
}

val MIGRATION_39_40 = object : Migration(39, 40) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE interventions ADD COLUMN version INTEGER NOT NULL DEFAULT 1",
        )
    }
}

val MIGRATION_41_42 = object : Migration(41, 42) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE interventions ADD COLUMN hasLocalChanges INTEGER NOT NULL DEFAULT 0",
        )
        database.execSQL(
            "ALTER TABLE interventions ADD COLUMN conflictResolveAttempts INTEGER NOT NULL DEFAULT 0",
        )
    }
}

val MIGRATION_40_41 = object : Migration(40, 41) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS pending_clients (
                localId TEXT NOT NULL PRIMARY KEY,
                firstName TEXT NOT NULL,
                lastName TEXT NOT NULL,
                civility TEXT,
                phone TEXT,
                email TEXT,
                address TEXT NOT NULL,
                addressComplement TEXT,
                city TEXT NOT NULL,
                zipCode TEXT NOT NULL,
                lat REAL,
                lng REAL,
                unitType TEXT NOT NULL,
                unitCategory TEXT,
                floor TEXT,
                syncStatus TEXT NOT NULL DEFAULT 'PENDING',
                remoteId TEXT,
                remoteUnitId TEXT,
                createdAt INTEGER NOT NULL
            )
            """.trimIndent(),
        )
    }
}

val MIGRATION_38_39 = object : Migration(38, 39) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS pending_interventions (
                localId TEXT NOT NULL PRIMARY KEY,
                clientNameFree TEXT NOT NULL,
                addressFree TEXT NOT NULL,
                city TEXT,
                zipCode TEXT,
                phone TEXT,
                interventionType TEXT NOT NULL,
                scheduledAt INTEGER NOT NULL,
                notes TEXT,
                syncStatus TEXT NOT NULL DEFAULT 'PENDING',
                remoteId TEXT,
                createdAt INTEGER NOT NULL
            )
            """.trimIndent(),
        )
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
