package re.savio.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import re.savio.mobile.data.remote.dto.AnomalyTypeDto

@Entity(
    tableName = "anomaly_types",
)
data class AnomalyTypeEntity(
    @PrimaryKey val id: String,
    val code: String,
    val designation: String,
    val type: String,
    val level: String,
    val nomenclature: String,
    val energyType: String,
    val isActive: Boolean = true,
)

fun AnomalyTypeDto.toEntity() = AnomalyTypeEntity(
    id = id,
    code = code,
    designation = designation,
    type = type,
    level = level,
    nomenclature = nomenclature,
    energyType = energyType,
    isActive = isActive,
)
