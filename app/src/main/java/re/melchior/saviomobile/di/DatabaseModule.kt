package re.melchior.saviomobile.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import re.melchior.saviomobile.data.local.dao.EquipmentDao
import re.melchior.saviomobile.data.local.dao.InterventionDao
import re.melchior.saviomobile.data.local.dao.PendingUpdateDao
import re.melchior.saviomobile.data.local.dao.ReferentielDao
import re.melchior.saviomobile.data.local.dao.SettingsDao
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
}