package re.melchior.saviomobile.util

import re.melchior.saviomobile.data.local.entity.InstallationCheckEntity
import java.time.LocalDate

const val GAS_PIPE_EXPIRED_ANOMALY_CODE = "31"

enum class GasPipeValidityStatus {
    None,
    Ok,
    ExpiringSoon,
    Expired,
}

fun evaluateGasPipeValidity(
    gasPipeType: String?,
    gasPipeValidityDateIso: String?,
    today: LocalDate = LocalDate.now(SavioTimeZone.appZone),
): GasPipeValidityStatus {
    if (gasPipeType.isNullOrBlank()) return GasPipeValidityStatus.None
    val dateIso = gasPipeValidityDateIso?.trim().orEmpty()
    if (dateIso.isEmpty()) return GasPipeValidityStatus.None
    val validityDate =
        runCatching { LocalDate.parse(dateIso) }.getOrNull()
            ?: return GasPipeValidityStatus.None
    if (validityDate.isBefore(today)) return GasPipeValidityStatus.Expired
    if (!validityDate.isAfter(today.plusDays(30))) return GasPipeValidityStatus.ExpiringSoon
    return GasPipeValidityStatus.Ok
}

fun isGasPipeExpiredWithoutAnomaly(
    check: InstallationCheckEntity?,
    hasGasPipeAnomalyDraft: Boolean,
    today: LocalDate = LocalDate.now(SavioTimeZone.appZone),
): Boolean {
    if (hasGasPipeAnomalyDraft) return false
    return evaluateGasPipeValidity(
        gasPipeType = check?.gasPipeType,
        gasPipeValidityDateIso = check?.gasPipeValidityDate,
        today = today,
    ) == GasPipeValidityStatus.Expired
}
