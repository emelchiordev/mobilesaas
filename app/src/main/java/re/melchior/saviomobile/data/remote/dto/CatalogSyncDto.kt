package re.melchior.saviomobile.data.remote.dto

import com.google.gson.annotations.SerializedName
import re.melchior.saviomobile.data.local.entity.CatalogEquipmentEntity
import re.melchior.saviomobile.data.local.entity.CatalogNomenclatureEntity

data class NoticeUrlResponseDto(
    @SerializedName("downloadUrl") val downloadUrl: String,
)

data class CatalogSyncResponseDto(
    @SerializedName("syncedAt") val syncedAt: String,
    @SerializedName("nomenclature") val nomenclature: List<NomenclatureItemDto>,
    @SerializedName("equipment") val equipment: List<CatalogEquipmentDto>,
)

data class NomenclatureItemDto(
    @SerializedName("id") val id: String,
    @SerializedName("domain") val domain: String,
    @SerializedName("code") val code: String,
    @SerializedName("label") val label: String,
    @SerializedName("isActive") val isActive: Boolean,
    @SerializedName("updatedAt") val updatedAt: String,
)

data class CatalogEquipmentDto(
    @SerializedName("id") val id: String,
    @SerializedName("model") val model: String,
    @SerializedName("brandId") val brandId: String,
    @SerializedName("energyId") val energyId: String,
    @SerializedName("equipmentTypeId") val equipmentTypeId: String,
    @SerializedName("powerKw") val powerKw: Double?,
    @SerializedName("maintenanceDurationHours") val maintenanceDurationHours: Double?,
    @SerializedName("referenceConstructeur") val referenceConstructeur: String?,
    @SerializedName("noticeUrl") val noticeUrl: String?,
    @SerializedName("isActive") val isActive: Boolean,
    @SerializedName("updatedAt") val updatedAt: String,
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
