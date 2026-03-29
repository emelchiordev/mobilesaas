package re.melchior.saviomobile.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import re.melchior.saviomobile.data.local.dao.EquipmentDao
import re.melchior.saviomobile.data.local.dao.InterventionDao
import re.melchior.saviomobile.data.local.dao.PendingUpdateDao
import re.melchior.saviomobile.data.local.dao.ReferentielDao
import re.melchior.saviomobile.data.local.dao.SettingsDao
import re.melchior.saviomobile.data.local.entity.EnergyTypeEntity
import re.melchior.saviomobile.data.local.entity.EquipmentEntity
import re.melchior.saviomobile.data.local.entity.EquipmentTypeEntity
import re.melchior.saviomobile.data.local.entity.InterventionEntity
import re.melchior.saviomobile.data.local.entity.InterventionTypeEntity
import re.melchior.saviomobile.data.local.entity.PendingUpdateEntity
import re.melchior.saviomobile.data.local.entity.SettingsEntity

@Database(
    entities = [
        InterventionEntity::class,
        EquipmentEntity::class,
        InterventionTypeEntity::class,
        EquipmentTypeEntity::class,
        EnergyTypeEntity::class,
        SettingsEntity::class,
        PendingUpdateEntity::class
    ],
    version = 4,
    exportSchema = true
)
abstract class SavioDatabase : RoomDatabase() {
    abstract fun interventionDao(): InterventionDao
    abstract fun equipmentDao(): EquipmentDao
    abstract fun referentielDao(): ReferentielDao
    abstract fun settingsDao(): SettingsDao

    abstract fun pendingUpdateDao(): PendingUpdateDao

}