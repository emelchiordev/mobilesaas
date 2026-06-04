package re.melchior.saviomobile.data.remote.dto

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class PullResponseDto(
    @SerializedName("pulledAt")
    val pulledAt: String,
    @SerializedName("technician")
    val technician: TechnicianDto,
    @SerializedName("interventions")
    val interventions: List<InterventionDto>,
    @SerializedName("interventionTypes")
    val interventionTypes: List<InterventionTypeDto>? = null,
    @SerializedName("anomalyTypes")
    val anomalyTypes: List<AnomalyTypeDto>? = null,
    @SerializedName("referentiels")
    val referentiels: ReferentielsDto?,
    @SerializedName("settings")
    val settings: SettingsDto? // ← nullable
)

data class TechnicianDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("requireInvoiceValidation")
    val requireInvoiceValidation: Boolean? = null
)

data class InterventionDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("scheduledAt")
    val scheduledAt: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("version")
    val version: Int = 1,
    @SerializedName("number")
    val number: String? = null,
    @SerializedName("type")
    val type: InterventionTypeDto,
    @SerializedName("unit")
    val unit: UnitDto,
    @SerializedName("customer")
    val customer: CustomerDto?,
    @SerializedName("equipment")
    val equipment: List<EquipmentDto>,
    @SerializedName("contract")
    val contract: ContractDto?,
    @SerializedName("report")
    val report: String? = null,
    @SerializedName("completedAt")
    val completedAt: String? = null,
    @SerializedName("startedAt")
    val startedAt: String? = null,
    @SerializedName("isChantier")
    val isChantier: Boolean? = null,
    @SerializedName("history")
    val history: List<HistoryItemDto> = emptyList(),
    @SerializedName("notes")
    val notes: String? = null,
    @SerializedName("actualTypeId")
    val actualTypeId: String? = null,
    @SerializedName("actualTypeCode")
    val actualTypeCode: String? = null,
    @SerializedName("actualTypeLabel")
    val actualTypeLabel: String? = null,
    @SerializedName("actualTypes")
    val actualTypes: List<InterventionActualTypeItemDto> = emptyList(),
    @SerializedName("pacMeasures")
    val pacMeasures: List<PacMeasurePullDto> = emptyList(),
)

data class PacMeasurePullDto(
    @SerializedName("interventionId") val interventionId: String = "",
    @SerializedName("equipmentOrder") val equipmentOrder: Int = 0,
    @SerializedName("pacVentilation") val pacVentilation: String? = null,
    @SerializedName("pacNetail") val pacNetail: String? = null,
    @SerializedName("pacVerail") val pacVerail: String? = null,
    @SerializedName("pacFiltre") val pacFiltre: String? = null,
    @SerializedName("pacFuite") val pacFuite: String? = null,
    @SerializedName("pacEvac") val pacEvac: String? = null,
    @SerializedName("pacPression1") val pacPression1: String? = null,
    @SerializedName("pacPression2") val pacPression2: Double? = null,
    @SerializedName("pacGlycol1") val pacGlycol1: String? = null,
    @SerializedName("pacGlycol2") val pacGlycol2: Double? = null,
    @SerializedName("pacTenStat") val pacTenStat: Double? = null,
    @SerializedName("pacTenDyna") val pacTenDyna: Double? = null,
    @SerializedName("pacIntensite") val pacIntensite: Double? = null,
    @SerializedName("pacResserage1") val pacResserage1: String? = null,
    @SerializedName("pacResserage2") val pacResserage2: String? = null,
    @SerializedName("pacInterieure") val pacInterieure: Double? = null,
    @SerializedName("pacExterieure") val pacExterieure: Double? = null,
    @SerializedName("pacDepart") val pacDepart: Double? = null,
    @SerializedName("pacRetour") val pacRetour: Double? = null,
    @SerializedName("pacDeltaT") val pacDeltaT: Double? = null,
    @SerializedName("pacHiver") val pacHiver: Double? = null,
    @SerializedName("pacAppoint") val pacAppoint: Double? = null,
    @SerializedName("pacConfort") val pacConfort: Double? = null,
    @SerializedName("pacNonChauf") val pacNonChauf: Double? = null,
    @SerializedName("pacEcsConsigne") val pacEcsConsigne: Double? = null,
    @SerializedName("pacEcs") val pacEcs: Double? = null,
    @SerializedName("pacManometreBp") val pacManometreBp: Double? = null,
    @SerializedName("pacManometreHp") val pacManometreHp: Double? = null,
    @SerializedName("pacDegivrage") val pacDegivrage: String? = null,
    @SerializedName("pacInversion") val pacInversion: String? = null,
    @SerializedName("pacHFonct") val pacHFonct: Double? = null,
    @SerializedName("pacHComp1") val pacHComp1: Double? = null,
    @SerializedName("pacHVenti") val pacHVenti: Double? = null,
    @SerializedName("pacNbDemarr") val pacNbDemarr: Double? = null,
    @SerializedName("pacHAppoint1") val pacHAppoint1: Double? = null,
    @SerializedName("pacHAppoint2") val pacHAppoint2: Double? = null,
    @SerializedName("pacAlarme1") val pacAlarme1: String? = null,
    @SerializedName("pacAlarme2") val pacAlarme2: String? = null,
    @SerializedName("pacBlocage1") val pacBlocage1: String? = null,
    @SerializedName("pacBlocage2") val pacBlocage2: String? = null,
    @SerializedName("pacReleve") val pacReleve: Double? = null,
    @SerializedName("pacRem1") val pacRem1: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
)

data class InterventionActualTypeItemDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("code") val code: String,
    @SerializedName("label") val label: String,
    @SerializedName("color") val color: String? = null,
    @SerializedName("isVeType") val isVeType: Boolean = false,
    @SerializedName("order") val order: Int = 1
)

data class HistoryItemDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("number")
    val number: String? = null,
    @SerializedName("scheduledAt")
    val scheduledAt: String,
    @SerializedName("completedAt")
    val completedAt: String? = null,
    @SerializedName("report")
    val report: String? = null,
    @SerializedName("notes")
    val notes: String? = null,
    @SerializedName("typeCode")
    val typeCode: String,
    @SerializedName("typeLabel")
    val typeLabel: String,
    @SerializedName("typeColor")
    val typeColor: String? = null,
    @SerializedName("completedAsVe")
    val completedAsVe: Boolean = false,
    @SerializedName("technicianFirstName")
    val technicianFirstName: String? = null,
    @SerializedName("technicianLastName")
    val technicianLastName: String? = null,
    @SerializedName("photoKeys")
    val photoKeys: List<String> = emptyList(),
    @SerializedName("history")
val history: List<HistoryItemDto> = emptyList()
)

data class UnitDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("street")
    val street: String,
    @SerializedName("addressLine2")
    val addressLine2: String?,
    @SerializedName("postalCode")
    val postalCode: String,
    @SerializedName("city")
    val city: String,
    @SerializedName("floor")
    val floor: String?,
    @SerializedName("doorCode")
    val doorCode: String?,
    @SerializedName("latitude")
    val latitude: Double?,
    @SerializedName("longitude")
    val longitude: Double?
)

data class CustomerDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("email")
    val email: String?
)

data class EquipmentDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("brand")
    val brand: String?,
    @SerializedName("model")
    val model: String?,
    @SerializedName("type_code")
    val typeCode: String?,
    @SerializedName("energy_code")
    val energyCode: String?,
    @SerializedName("serialNumber")
    val serialNumber: String?,
    @SerializedName("commissioningDate")
    val installDate: String?,
    @SerializedName("is_primary")
    val isPrimary: Boolean,
    @SerializedName("equipmentCatalogId")
    val equipmentCatalogId: String? = null,
    @SerializedName("catalogBrandId")
    val catalogBrandId: String? = null,
    @SerializedName("parentEquipmentId")
    val parentEquipmentId: String? = null,
    @SerializedName("order")
    val order: Int? = null,
    @SerializedName("evacuation_mode")
    val evacuationMode: String? = null,
    @SerializedName("hybridePacEquipmentId")
    val hybridePacEquipmentId: String? = null,
    @SerializedName("powerKw")
    val powerKw: Double? = null,
    @SerializedName("attrs")
    val attrs: JsonElement? = null,
)

data class ContractDto(
    @SerializedName("type")
    val type: String?,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("renewalDate")
    val renewalDate: String?,
    @SerializedName("tariff")
    val tariff: Double?,
    @SerializedName("vatRate")
    val vatRate: Double?
)

data class ReferentielsDto(
    @SerializedName("interventionTypes")
    val interventionTypes: List<InterventionTypeDto>?,
    @SerializedName("equipmentTypes")
    val equipmentTypes: List<CodeLabelDto>?,
    @SerializedName("energyTypes")
    val energyTypes: List<CodeLabelDto>?
)

data class CodeLabelDto(
    @SerializedName("code")
    val code: String,
    @SerializedName("label")
    val label: String
)

data class AnomalyTypeDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("code")
    val code: String,
    @SerializedName("designation")
    val designation: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("level")
    val level: String,
    @SerializedName("nomenclature")
    val nomenclature: String,
    @SerializedName("energyType")
    val energyType: String,
    @SerializedName("isActive")
    val isActive: Boolean = true,
)

data class SettingsDto(
    @SerializedName("allowCreateIntervention")
    val allowCreateIntervention: Boolean = false,
    @SerializedName("allowProposal")
    val allowProposal: Boolean = true
)

data class MobilePendingInterventionRequestDto(
    @SerializedName("localId") val localId: String,
    @SerializedName("clientNameFree") val clientNameFree: String,
    @SerializedName("addressFree") val addressFree: String,
    @SerializedName("city") val city: String? = null,
    @SerializedName("zipCode") val zipCode: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("interventionType") val interventionType: String,
    @SerializedName("scheduledAt") val scheduledAt: String,
    @SerializedName("notes") val notes: String? = null,
)

data class MobilePendingInterventionResponseDto(
    @SerializedName("localId") val localId: String,
    @SerializedName("remoteId") val remoteId: String,
    @SerializedName("clientId") val clientId: String,
    @SerializedName("status") val status: String,
)