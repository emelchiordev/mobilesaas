package re.melchior.saviomobile.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import re.melchior.saviomobile.data.local.dao.EquipmentDao
import re.melchior.saviomobile.data.local.dao.InterventionActualTypeDao
import re.melchior.saviomobile.data.local.dao.InterventionDao
import re.melchior.saviomobile.data.local.dao.InterventionHistoryDao
import re.melchior.saviomobile.data.local.dao.InvoiceDao
import re.melchior.saviomobile.data.local.dao.InvoiceLineDao
import re.melchior.saviomobile.data.local.dao.InvoicePaymentDao
import re.melchior.saviomobile.data.local.dao.PendingUpdateDao
import re.melchior.saviomobile.data.local.dao.PhotoDao
import re.melchior.saviomobile.data.local.dao.ReferentielDao
import re.melchior.saviomobile.data.local.dao.SettingsDao
import re.melchior.saviomobile.data.local.entity.EnergyTypeEntity
import re.melchior.saviomobile.data.local.entity.EquipmentEntity
import re.melchior.saviomobile.data.local.entity.EquipmentTypeEntity
import re.melchior.saviomobile.data.local.entity.InterventionActualTypeEntity
import re.melchior.saviomobile.data.local.entity.InterventionEntity
import re.melchior.saviomobile.data.local.entity.InterventionHistoryEntity
import re.melchior.saviomobile.data.local.entity.InterventionTypeEntity
import re.melchior.saviomobile.data.local.entity.InvoiceEntity
import re.melchior.saviomobile.data.local.entity.InvoiceLineEntity
import re.melchior.saviomobile.data.local.entity.InvoicePaymentEntity
import re.melchior.saviomobile.data.local.entity.PendingUpdateEntity
import re.melchior.saviomobile.data.local.entity.PhotoEntity
import re.melchior.saviomobile.data.local.entity.SettingsEntity

@Database(
    entities = [
        InterventionEntity::class,
        EquipmentEntity::class,
        InterventionTypeEntity::class,
        EquipmentTypeEntity::class,
        EnergyTypeEntity::class,
        SettingsEntity::class,
        PendingUpdateEntity::class,
        PhotoEntity::class,
        InterventionHistoryEntity::class,
        InterventionActualTypeEntity::class,
        InvoiceEntity::class,
        InvoiceLineEntity::class,
        InvoicePaymentEntity::class
    ],
    version = 13,
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

}
