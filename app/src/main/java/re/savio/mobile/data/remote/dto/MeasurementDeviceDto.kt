package re.savio.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName
import re.savio.mobile.data.local.objectbox.MeasurementDeviceBox

data class MeasurementDevicesForMobileResponseDto(
    @SerializedName("devices") val devices: List<MeasurementDeviceDto>,
    @SerializedName("lastCerfaMeasurementDeviceId")
    val lastCerfaMeasurementDeviceId: String? = null,
    @SerializedName("total") val total: Int,
    @SerializedName("syncedAt") val syncedAt: String,
)

data class MeasurementDeviceDto(
    @SerializedName("id") val id: String,
    @SerializedName("brand") val brand: String,
    @SerializedName("model") val model: String,
    @SerializedName("serialNumber") val serialNumber: String? = null,
    @SerializedName("useCases") val useCases: List<String> = listOf("cerfa"),
    @SerializedName("lastControlDate") val lastControlDate: String? = null,
    @SerializedName("assignedTechnicianId") val assignedTechnicianId: String? = null,
    @SerializedName("updatedAt") val updatedAt: String,
)

data class CreateMeasurementDeviceBody(
    @SerializedName("brand") val brand: String,
    @SerializedName("model") val model: String,
    @SerializedName("serialNumber") val serialNumber: String? = null,
    @SerializedName("useCases") val useCases: List<String> = listOf("cerfa"),
    @SerializedName("lastControlDate") val lastControlDate: String? = null,
    @SerializedName("assignToSelf") val assignToSelf: Boolean = true,
)

data class RememberCerfaMeasurementDeviceBody(
    @SerializedName("deviceId") val deviceId: String,
)

fun MeasurementDeviceDto.toBoxEntity(
    existing: MeasurementDeviceBox? = null,
): MeasurementDeviceBox {
    val target = existing ?: MeasurementDeviceBox()
    target.id = id
    target.brand = brand
    target.model = model
    target.serialNumber = serialNumber
    target.useCasesJson = useCases.joinToString(",", prefix = "[", postfix = "]") {
        "\"$it\""
    }
    target.lastControlDate = lastControlDate
    target.assignedTechnicianId = assignedTechnicianId
    target.updatedAt = updatedAt
    return target
}

fun MeasurementDeviceBox.formatControlDateFr(): String {
    return formatControlDateFr(lastControlDate)
}

/** Affiche jj/mm/aaaa (accepte ISO yyyy-MM-dd ou déjà FR). */
fun formatControlDateFr(raw: String?): String {
    val value = raw?.trim().orEmpty()
    if (value.isEmpty()) return ""
    if (value.length >= 10 && value[4] == '-') {
        val parts = value.take(10).split("-")
        if (parts.size == 3) return "${parts[2]}/${parts[1]}/${parts[0]}"
    }
    if (value.contains('/')) {
        val parts = value.split("/")
        if (parts.size == 3) {
            val day = parts[0].padStart(2, '0')
            val month = parts[1].padStart(2, '0')
            val year = parts[2]
            if (year.length == 4) return "$day/$month/$year"
        }
    }
    return value
}

/** Convertit jj/mm/aaaa → yyyy-MM-dd pour l’API ; laisse passer un ISO déjà valide. */
fun controlDateToIso(raw: String?): String? {
    val value = raw?.trim().orEmpty()
    if (value.isEmpty()) return null
    if (value.length >= 10 && value[4] == '-') {
        return value.take(10)
    }
    val match = Regex("""^(\d{1,2})/(\d{1,2})/(\d{4})$""").matchEntire(value) ?: return null
    val day = match.groupValues[1].padStart(2, '0')
    val month = match.groupValues[2].padStart(2, '0')
    val year = match.groupValues[3]
    return "$year-$month-$day"
}

fun MeasurementDeviceBox.hasUseCase(useCase: String): Boolean {
    return useCasesJson.contains("\"$useCase\"")
}
