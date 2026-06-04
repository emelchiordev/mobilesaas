package re.melchior.saviomobile.data.remote.dto

import com.google.gson.annotations.SerializedName
import re.melchior.saviomobile.data.local.entity.CatalogEquipmentEntity
import re.melchior.saviomobile.data.local.entity.CatalogNomenclatureEntity

data class NoticeUrlResponseDto(
    @SerializedName("downloadUrl") val downloadUrl: String,
)

data class CatalogSyncResponseDto(
    @SerializedName(value = "syncedAt", alternate = ["synced_at"]) val syncedAt: String,
    @SerializedName("nomenclature") val nomenclature: List<NomenclatureItemDto>,
    @SerializedName("equipment") val equipment: List<CatalogEquipmentDto>,
)

data class NomenclatureItemDto(
    @SerializedName("id") val id: String,
    @SerializedName("domain") val domain: String,
    @SerializedName("code") val code: String,
    @SerializedName("label") val label: String,
    @SerializedName(value = "isActive", alternate = ["is_active"]) val isActive: Boolean = true,
    @SerializedName(value = "updatedAt", alternate = ["updated_at"]) val updatedAt: String? = null,
)

data class CatalogEquipmentDto(
    @SerializedName("id") val id: String,
    @SerializedName("model") val model: String,
    @SerializedName(value = "brandId", alternate = ["brand_id"]) val brandId: String,
    @SerializedName(value = "energyId", alternate = ["energy_id"]) val energyId: String,
    @SerializedName(value = "equipmentTypeId", alternate = ["equipment_type_id"]) val equipmentTypeId: String,
    @SerializedName(value = "powerKw", alternate = ["power_kw"]) val powerKw: Double?,
    @SerializedName(value = "maintenanceDurationHours", alternate = ["maintenance_duration_hours"])
    val maintenanceDurationHours: Double?,
    @SerializedName(value = "referenceConstructeur", alternate = ["reference_constructeur"])
    val referenceConstructeur: String?,
    @SerializedName(value = "noticeUrl", alternate = ["notice_url"]) val noticeUrl: String?,
    @SerializedName(value = "isActive", alternate = ["is_active"]) val isActive: Boolean = true,
    @SerializedName(value = "updatedAt", alternate = ["updated_at"]) val updatedAt: String? = null,
)

fun NomenclatureItemDto.toEntity() = CatalogNomenclatureEntity(
    id = id,
    domain = domain,
    code = code,
    label = label,
    isActive = isActive,
    updatedAt = updatedAt ?: "",
)

fun CatalogEquipmentDto.toEntity() = CatalogEquipmentEntity(
    id = id,
    model = model,
    brandId = brandId,
    energyId = energyId,
    equipmentTypeId = equipmentTypeId,
    powerKw = powerKw,
    maintenanceDurationHours = maintenanceDurationHours,
    referenceConstructeur = referenceConstructeur,
    noticeUrl = noticeUrl,
    isActive = isActive,
    updatedAt = updatedAt?: "",
)
