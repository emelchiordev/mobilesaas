package re.melchior.saviomobile.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import re.melchior.saviomobile.data.local.dao.EquipmentDao
import re.melchior.saviomobile.data.local.dao.InterventionActualTypeDao
import re.melchior.saviomobile.data.local.dao.InterventionDao
import re.melchior.saviomobile.data.local.dao.InvoiceDao
import re.melchior.saviomobile.data.local.dao.InvoiceLineDao
import re.melchior.saviomobile.data.local.dao.InvoicePaymentDao
import re.melchior.saviomobile.data.local.dao.InterventionHistoryDao
import re.melchior.saviomobile.data.local.dao.PendingUpdateDao
import re.melchior.saviomobile.data.local.dao.PhotoDao
import re.melchior.saviomobile.data.local.dao.ReferentielDao
import re.melchior.saviomobile.data.local.dao.SettingsDao
import re.melchior.saviomobile.data.local.database.MIGRATION_12_13
import re.melchior.saviomobile.data.local.database.SavioDatabase
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
        "savio.db"
    )
        .addMigrations(MIGRATION_12_13)
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

}