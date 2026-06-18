package re.melchior.saviomobile.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import re.melchior.saviomobile.data.local.dao.CatalogEquipmentDao
import re.melchior.saviomobile.data.local.dao.CatalogNomenclatureDao
import re.melchior.saviomobile.data.local.dao.EquipmentDao
import re.melchior.saviomobile.data.local.dao.InterventionActualTypeDao
import re.melchior.saviomobile.data.local.dao.InterventionDao
import re.melchior.saviomobile.data.local.dao.InterventionHistoryDao
import re.melchior.saviomobile.data.local.dao.InvoiceDao
import re.melchior.saviomobile.data.local.dao.InvoiceLineDao
import re.melchior.saviomobile.data.local.dao.InvoicePaymentDao
import re.melchior.saviomobile.data.local.dao.PendingOperationDao
import re.melchior.saviomobile.data.local.dao.PendingUpdateDao
import re.melchior.saviomobile.data.local.dao.PhotoDao
import re.melchior.saviomobile.data.local.dao.ReferentielDao
import re.melchior.saviomobile.data.local.dao.SettingsDao
import re.melchior.saviomobile.data.local.dao.AttestationVeDao
import re.melchior.saviomobile.data.local.dao.AttestationVePointControleDao
import re.melchior.saviomobile.data.local.dao.ColdMeasureDao
import re.melchior.saviomobile.data.local.dao.EquipmentSnapshotDao
import re.melchior.saviomobile.data.local.dao.MeasureDao
import re.melchior.saviomobile.data.local.dao.PacMeasureDao
import re.melchior.saviomobile.data.local.dao.PendingClientDao
import re.melchior.saviomobile.data.local.dao.PendingInterventionDao
import re.melchior.saviomobile.data.local.dao.AnomalyDraftDao
import re.melchior.saviomobile.data.local.dao.AnomalyTypeDao
import re.melchior.saviomobile.data.local.dao.InstallationCheckDao
import re.melchior.saviomobile.data.local.dao.ContractProposalDao
import re.melchior.saviomobile.data.local.dao.ContractTariffDao
import re.melchior.saviomobile.data.local.entity.AttestationVeEntity
import re.melchior.saviomobile.data.local.entity.AttestationVePointControleEntity
import re.melchior.saviomobile.data.local.entity.CatalogEquipmentEntity
import re.melchior.saviomobile.data.local.entity.ColdMeasureEntity
import re.melchior.saviomobile.data.local.entity.CatalogNomenclatureEntity
import re.melchior.saviomobile.data.local.entity.EnergyTypeEntity
import re.melchior.saviomobile.data.local.entity.EquipmentEntity
import re.melchior.saviomobile.data.local.entity.EquipmentSnapshotEntity
import re.melchior.saviomobile.data.local.entity.EquipmentTypeEntity
import re.melchior.saviomobile.data.local.entity.InterventionActualTypeEntity
import re.melchior.saviomobile.data.local.entity.InterventionEntity
import re.melchior.saviomobile.data.local.entity.InterventionHistoryEntity
import re.melchior.saviomobile.data.local.entity.InterventionTypeEntity
import re.melchior.saviomobile.data.local.entity.InvoiceEntity
import re.melchior.saviomobile.data.local.entity.InvoiceLineEntity
import re.melchior.saviomobile.data.local.entity.InvoicePaymentEntity
import re.melchior.saviomobile.data.local.entity.MeasureEntity
import re.melchior.saviomobile.data.local.entity.PacMeasureEntity
import re.melchior.saviomobile.data.local.entity.PendingClientEntity
import re.melchior.saviomobile.data.local.entity.PendingInterventionEntity
import re.melchior.saviomobile.data.local.entity.CivilityOptionEntity
import re.melchior.saviomobile.data.local.entity.ContractProposalEntity
import re.melchior.saviomobile.data.local.entity.ContractTariffEntity
import re.melchior.saviomobile.data.local.entity.AnomalyDraftEntity
import re.melchior.saviomobile.data.local.entity.AnomalyTypeEntity
import re.melchior.saviomobile.data.local.entity.InstallationCheckEntity
import re.melchior.saviomobile.data.local.entity.PendingOperationEntity
import re.melchior.saviomobile.data.local.entity.PendingUpdateEntity
import re.melchior.saviomobile.data.local.entity.PhotoEntity
import re.melchior.saviomobile.data.local.entity.SettingsEntity
import re.melchior.saviomobile.data.local.entity.UnitTypeEntity

@Database(
    entities = [
        InterventionEntity::class,
        EquipmentEntity::class,
        InterventionTypeEntity::class,
        EquipmentTypeEntity::class,
        EnergyTypeEntity::class,
        UnitTypeEntity::class,
        CivilityOptionEntity::class,
        SettingsEntity::class,
        PendingUpdateEntity::class,
        PhotoEntity::class,
        InterventionHistoryEntity::class,
        InterventionActualTypeEntity::class,
        InvoiceEntity::class,
        InvoiceLineEntity::class,
        InvoicePaymentEntity::class,
        CatalogNomenclatureEntity::class,
        CatalogEquipmentEntity::class,
        PendingOperationEntity::class,
        ColdMeasureEntity::class,
        EquipmentSnapshotEntity::class,
        MeasureEntity::class,
        PacMeasureEntity::class,
        AttestationVeEntity::class,
        AttestationVePointControleEntity::class,
        PendingInterventionEntity::class,
        PendingClientEntity::class,
        ContractTariffEntity::class,
        ContractProposalEntity::class,
        AnomalyTypeEntity::class,
        AnomalyDraftEntity::class,
        InstallationCheckEntity::class,
    ],
    version = 63,
    exportSchema = true
)
abstract class SavioDatabase : RoomDatabase() {
    abstract fun interventionDao(): InterventionDao
    abstract fun equipmentDao(): EquipmentDao
    abstract fun referentielDao(): ReferentielDao
    abstract fun settingsDao(): SettingsDao

    abstract fun photoDao(): PhotoDao

    abstract fun pendingUpdateDao(): PendingUpdateDao

    abstract fun interventionHistoryDao(): InterventionHistoryDao

    abstract fun interventionActualTypeDao(): InterventionActualTypeDao

    abstract fun invoiceDao(): InvoiceDao

    abstract fun invoiceLineDao(): InvoiceLineDao

    abstract fun invoicePaymentDao(): InvoicePaymentDao

    abstract fun catalogNomenclatureDao(): CatalogNomenclatureDao

    abstract fun catalogEquipmentDao(): CatalogEquipmentDao

    abstract fun pendingOperationDao(): PendingOperationDao

    abstract fun coldMeasureDao(): ColdMeasureDao

    abstract fun equipmentSnapshotDao(): EquipmentSnapshotDao

    abstract fun measureDao(): MeasureDao

    abstract fun pacMeasureDao(): PacMeasureDao

    abstract fun attestationVeDao(): AttestationVeDao

    abstract fun attestationVePointControleDao(): AttestationVePointControleDao

    abstract fun pendingInterventionDao(): PendingInterventionDao

    abstract fun pendingClientDao(): PendingClientDao

    abstract fun contractTariffDao(): ContractTariffDao

    abstract fun contractProposalDao(): ContractProposalDao

    abstract fun anomalyTypeDao(): AnomalyTypeDao

    abstract fun anomalyDraftDao(): AnomalyDraftDao

    abstract fun installationCheckDao(): InstallationCheckDao
}
