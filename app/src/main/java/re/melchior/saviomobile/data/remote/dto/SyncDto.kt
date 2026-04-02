package re.melchior.saviomobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PullResponseDto(
    @SerializedName("pulledAt")
    val pulledAt: String,
    @SerializedName("technician")
    val technician: TechnicianDto,
    @SerializedName("interventions")
    val interventions: List<InterventionDto>,
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
    val phone: String?
)

data class InterventionDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("scheduledAt")
    val scheduledAt: String,
    @SerializedName("status")
    val status: String,
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
    val startedAt: String? = null
)

data class InterventionTypeDto(
    @SerializedName("code")
    val code: String,
    @SerializedName("label")
    val label: String,
    @SerializedName("color")
    val color: String?
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
    @SerializedName("installDate")
    val installDate: String?,
    @SerializedName("is_primary")
    val isPrimary: Boolean
)

data class ContractDto(
    @SerializedName("type")
    val type: String?,
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

data class SettingsDto(
    @SerializedName("allowCreateIntervention")
    val allowCreateIntervention: Boolean = false,
    @SerializedName("allowProposal")
    val allowProposal: Boolean = true
)