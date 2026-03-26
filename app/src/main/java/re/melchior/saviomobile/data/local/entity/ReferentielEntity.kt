package re.melchior.saviomobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "intervention_types")
data class InterventionTypeEntity(
    @PrimaryKey
    val code: String,
    val label: String,
    val color: String?
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