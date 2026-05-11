package re.melchior.saviomobile.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import re.melchior.saviomobile.data.local.dao.CatalogEquipmentDao
import re.melchior.saviomobile.data.local.dao.ColdMeasureDao
import re.melchior.saviomobile.data.local.dao.CatalogNomenclatureDao
import re.melchior.saviomobile.data.local.dao.EquipmentDao
import re.melchior.saviomobile.data.local.dao.InterventionActualTypeDao
import re.melchior.saviomobile.data.local.dao.InterventionDao
import re.melchior.saviomobile.data.local.dao.InvoiceDao
import re.melchior.saviomobile.data.local.dao.InvoiceLineDao
import re.melchior.saviomobile.data.local.dao.InvoicePaymentDao
import re.melchior.saviomobile.data.local.dao.InterventionHistoryDao
import re.melchior.saviomobile.data.local.dao.PendingOperationDao
import re.melchior.saviomobile.data.local.dao.PendingUpdateDao
import re.melchior.saviomobile.data.local.dao.PhotoDao
import re.melchior.saviomobile.data.local.dao.ReferentielDao
import re.melchior.saviomobile.data.local.dao.SettingsDao
import re.melchior.saviomobile.data.local.database.MIGRATION_12_13
import re.melchior.saviomobile.data.local.database.MIGRATION_13_14
import re.melchior.saviomobile.data.local.database.MIGRATION_14_15
import re.melchior.saviomobile.data.local.database.MIGRATION_15_16
import re.melchior.saviomobile.data.local.database.MIGRATION_16_17
import re.melchior.saviomobile.data.local.database.MIGRATION_17_18
import re.melchior.saviomobile.data.local.database.MIGRATION_18_19
import re.melchior.saviomobile.data.local.database.MIGRATION_19_20
import re.melchior.saviomobile.data.local.database.MIGRATION_20_21
import re.melchior.saviomobile.data.local.database.MIGRATION_21_22
import re.melchior.saviomobile.data.local.database.MIGRATION_22_23
import re.melchior.saviomobile.data.local.database.MIGRATION_23_24
import re.melchior.saviomobile.data.local.database.MIGRATION_24_25
import re.melchior.saviomobile.data.local.database.MIGRATION_25_26
import re.melchior.saviomobile.data.local.database.MIGRATION_26_27
import re.melchior.saviomobile.data.local.database.MIGRATION_27_28
import re.melchior.saviomobile.data.local.database.MIGRATION_28_29
import re.melchior.saviomobile.data.local.database.MIGRATION_29_30
import re.melchior.saviomobile.data.local.database.MIGRATION_30_31
import re.melchior.saviomobile.data.local.database.MIGRATION_31_32
import re.melchior.saviomobile.data.local.database.MIGRATION_32_33
import re.melchior.saviomobile.data.local.database.MIGRATION_33_34
import re.melchior.saviomobile.data.local.database.MIGRATION_34_35
import re.melchior.saviomobile.data.local.database.MIGRATION_35_36
import re.melchior.saviomobile.data.local.database.MIGRATION_36_37
import re.melchior.saviomobile.data.local.database.MIGRATION_37_38
import re.melchior.saviomobile.data.local.database.SavioDatabase
import re.melchior.saviomobile.data.local.dao.AttestationVeDao
import re.melchior.saviomobile.data.local.dao.AttestationVePointControleDao
import re.melchior.saviomobile.data.local.dao.EquipmentSnapshotDao
import re.melchior.saviomobile.data.local.dao.MeasureDao
import re.melchior.saviomobile.data.local.dao.PacMeasureDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): SavioDatabase = Room.databaseBuilder(
        context,
        SavioDatabase::class.java,
        "savio.db",
    )
        .addMigrations(
            MIGRATION_12_13,
            MIGRATION_13_14,
            MIGRATION_14_15,
            MIGRATION_15_16,
            MIGRATION_16_17,
            MIGRATION_17_18,
            MIGRATION_18_19,
            MIGRATION_19_20,
            MIGRATION_20_21,
            MIGRATION_21_22,
            MIGRATION_22_23,
            MIGRATION_23_24,
            MIGRATION_24_25,
            MIGRATION_25_26,
            MIGRATION_26_27,
            MIGRATION_27_28,
            MIGRATION_28_29,
            MIGRATION_29_30,
            MIGRATION_30_31,
            MIGRATION_31_32,
            MIGRATION_32_33,
            MIGRATION_33_34,
            MIGRATION_34_35,
            MIGRATION_35_36,
            MIGRATION_36_37,
            MIGRATION_37_38,
        )
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    fun provideInterventionDao(db: SavioDatabase): InterventionDao =
        db.interventionDao()

    @Provides
    fun provideEquipmentDao(db: SavioDatabase): EquipmentDao =
        db.equipmentDao()

    @Provides
    fun provideReferentielDao(db: SavioDatabase): ReferentielDao =
        db.referentielDao()

    @Provides
    fun provideSettingsDao(db: SavioDatabase): SettingsDao =
        db.settingsDao()

    @Provides
    fun providePendingUpdateDao(db: SavioDatabase): PendingUpdateDao =
        db.pendingUpdateDao()

    @Provides
    fun providePhotoDao(db: SavioDatabase): PhotoDao =
        db.photoDao()

    @Provides
    fun provideInterventionHistoryDao(db: SavioDatabase): InterventionHistoryDao =
        db.interventionHistoryDao()

    @Provides
    fun provideInterventionActualTypeDao(db: SavioDatabase): InterventionActualTypeDao =
        db.interventionActualTypeDao()

    @Provides
    fun provideInvoiceDao(db: SavioDatabase): InvoiceDao =
        db.invoiceDao()

    @Provides
    fun provideInvoiceLineDao(db: SavioDatabase): InvoiceLineDao =
        db.invoiceLineDao()

    @Provides
    fun provideInvoicePaymentDao(db: SavioDatabase): InvoicePaymentDao =
        db.invoicePaymentDao()

    @Provides
    fun provideCatalogNomenclatureDao(db: SavioDatabase): CatalogNomenclatureDao =
        db.catalogNomenclatureDao()

    @Provides
    fun provideCatalogEquipmentDao(db: SavioDatabase): CatalogEquipmentDao =
        db.catalogEquipmentDao()

    @Provides
    fun providePendingOperationDao(db: SavioDatabase): PendingOperationDao =
        db.pendingOperationDao()

    @Provides
    fun provideColdMeasureDao(db: SavioDatabase): ColdMeasureDao =
        db.coldMeasureDao()

    @Provides
    fun provideEquipmentSnapshotDao(db: SavioDatabase): EquipmentSnapshotDao =
        db.equipmentSnapshotDao()

    @Provides
    fun provideMeasureDao(db: SavioDatabase): MeasureDao =
        db.measureDao()

    @Provides
    fun providePacMeasureDao(db: SavioDatabase): PacMeasureDao =
        db.pacMeasureDao()

    @Provides
    fun provideAttestationVeDao(db: SavioDatabase): AttestationVeDao =
        db.attestationVeDao()

    @Provides
    fun provideAttestationVePointControleDao(
        db: SavioDatabase,
    ): AttestationVePointControleDao =
        db.attestationVePointControleDao()

}