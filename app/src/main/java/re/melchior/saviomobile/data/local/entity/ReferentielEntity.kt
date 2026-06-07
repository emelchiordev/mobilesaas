package re.melchior.saviomobile.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "intervention_types",
    indices = [Index(value = ["code"], unique = true)],
)
data class InterventionTypeEntity(
    @PrimaryKey
    val id: String,
    val code: String,
    val label: String,
    val color: String?,
    val isVeType: Boolean = false,
    val isRamonageType: Boolean = false,
    val isSystem: Boolean = false,
    val showOnCreate: Boolean = true,
    val showOnClose: Boolean = true,
    val requireClientSignature: Boolean = true,
    val requireReport: Boolean = true,
    val triggerEquipmentSetup: Boolean = false,
)

@Entity(tableName = "equipment_types")
data class EquipmentTypeEntity(
    @PrimaryKey
    val code: String,
    val label: String
)

@Entity(tableName = "energy_types")
data class EnergyTypeEntity(
    @PrimaryKey
    val code: String,
    val label: String
)

@Entity(tableName = "unit_types")
data class UnitTypeEntity(
    @PrimaryKey
    val code: String,
    val label: String,
    val category: String,
)

@Entity(tableName = "civility_options")
data class CivilityOptionEntity(
    @PrimaryKey
    val code: String,
    val label: String,
)