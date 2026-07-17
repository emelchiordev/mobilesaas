package re.savio.mobile.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import re.savio.mobile.data.local.dao.CatalogEquipmentDao
import re.savio.mobile.data.local.dao.ClientFinancialSummaryDao
import re.savio.mobile.data.local.dao.CatalogNomenclatureDao
import re.savio.mobile.data.local.dao.EquipmentDao
import re.savio.mobile.data.local.dao.InterventionActualTypeDao
import re.savio.mobile.data.local.dao.InterventionDao
import re.savio.mobile.data.local.dao.InterventionHistoryDao
import re.savio.mobile.data.local.dao.InvoiceDao
import re.savio.mobile.data.local.dao.InvoiceLineDao
import re.savio.mobile.data.local.dao.InvoicePaymentDao
import re.savio.mobile.data.local.dao.PendingOperationDao
import re.savio.mobile.data.local.dao.PendingUpdateDao
import re.savio.mobile.data.local.dao.PhotoDao
import re.savio.mobile.data.local.dao.QuotePhotoDao
import re.savio.mobile.data.local.dao.ReferentielDao
import re.savio.mobile.data.local.dao.SettingsDao
import re.savio.mobile.data.local.dao.AttestationVeDao
import re.savio.mobile.data.local.dao.AttestationVePointControleDao
import re.savio.mobile.data.local.dao.ColdMeasureDao
import re.savio.mobile.data.local.dao.EquipmentSnapshotDao
import re.savio.mobile.data.local.dao.MeasureDao
import re.savio.mobile.data.local.dao.PacMeasureDao
import re.savio.mobile.data.local.dao.PendingClientDao
import re.savio.mobile.data.local.dao.PendingInterventionDao
import re.savio.mobile.data.local.dao.AnomalyDraftDao
import re.savio.mobile.data.local.dao.AnomalyTypeDao
import re.savio.mobile.data.local.dao.InstallationCheckDao
import re.savio.mobile.data.local.dao.ContractProposalDao
import re.savio.mobile.data.local.dao.ContractTariffDao
import re.savio.mobile.data.local.entity.AttestationVeEntity
import re.savio.mobile.data.local.entity.AttestationVePointControleEntity
import re.savio.mobile.data.local.entity.CatalogEquipmentEntity
import re.savio.mobile.data.local.entity.ColdMeasureEntity
import re.savio.mobile.data.local.entity.CatalogNomenclatureEntity
import re.savio.mobile.data.local.entity.EnergyTypeEntity
import re.savio.mobile.data.local.entity.EquipmentEntity
import re.savio.mobile.data.local.entity.EquipmentSnapshotEntity
import re.savio.mobile.data.local.entity.EquipmentTypeEntity
import re.savio.mobile.data.local.entity.InterventionActualTypeEntity
import re.savio.mobile.data.local.entity.InterventionEntity
import re.savio.mobile.data.local.entity.InterventionHistoryEntity
import re.savio.mobile.data.local.entity.InterventionTypeEntity
import re.savio.mobile.data.local.entity.InvoiceEntity
import re.savio.mobile.data.local.entity.InvoiceLineEntity
import re.savio.mobile.data.local.entity.InvoicePaymentEntity
import re.savio.mobile.data.local.entity.MeasureEntity
import re.savio.mobile.data.local.entity.PacMeasureEntity
import re.savio.mobile.data.local.entity.PendingClientEntity
import re.savio.mobile.data.local.entity.PendingInterventionEntity
import re.savio.mobile.data.local.entity.CivilityOptionEntity
import re.savio.mobile.data.local.entity.ClientFinancialSummaryEntity
import re.savio.mobile.data.local.entity.ContractProposalEntity
import re.savio.mobile.data.local.entity.ContractTariffEntity
import re.savio.mobile.data.local.entity.AnomalyDraftEntity
import re.savio.mobile.data.local.entity.AnomalyTypeEntity
import re.savio.mobile.data.local.entity.InstallationCheckEntity
import re.savio.mobile.data.local.entity.PendingOperationEntity
import re.savio.mobile.data.local.entity.PendingUpdateEntity
import re.savio.mobile.data.local.entity.PhotoEntity
import re.savio.mobile.data.local.entity.QuotePhotoEntity
import re.savio.mobile.data.local.entity.SettingsEntity
import re.savio.mobile.data.local.entity.UnitTypeEntity

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
        QuotePhotoEntity::class,
        ClientFinancialSummaryEntity::class,
    ],
    version = 73,
    exportSchema = true
)
abstract class SavioDatabase : RoomDatabase() {
    abstract fun interventionDao(): InterventionDao
    abstract fun equipmentDao(): EquipmentDao
    abstract fun referentielDao(): ReferentielDao
    abstract fun settingsDao(): SettingsDao

    abstract fun photoDao(): PhotoDao

    abstract fun quotePhotoDao(): QuotePhotoDao

    abstract fun clientFinancialSummaryDao(): ClientFinancialSummaryDao

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
