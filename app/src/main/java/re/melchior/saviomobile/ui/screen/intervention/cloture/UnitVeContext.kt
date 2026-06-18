package re.melchior.saviomobile.ui.screen.intervention.cloture



import re.melchior.saviomobile.data.local.entity.InterventionEntity

import re.melchior.saviomobile.data.local.entity.InterventionHistoryEntity

import re.melchior.saviomobile.data.remote.dto.UnitVeStatusDto

import re.melchior.saviomobile.util.SavioTimeZone
import re.melchior.saviomobile.util.AttestableEquipmentInput
import re.melchior.saviomobile.util.attestationAnchorEquipmentId

import java.time.LocalDate

import java.time.YearMonth

import java.time.format.DateTimeFormatter

import java.util.Locale



data class VeCoverageSummary(
    val unavailable: Boolean = true,
    val attested: Int? = null,
    val expected: Int? = null,
    val complete: Boolean? = null,
    /** Couverture projetée localement (clôture en cours, pas encore sur le serveur). */
    val closureProjected: Boolean = false,
)



data class UnitVeContext(

    val contractInfo: ContractSummary?,

    val lastVe: LastVeSummary?,

    val nextVe: NextVeDisplay?,

    val coverage: VeCoverageSummary? = null,

) {

    val hasVisibleContent: Boolean

        get() =

            contractInfo != null ||

                lastVe != null ||

                nextVe != null ||

                formatVeCoverageLabel(coverage) != null

}



fun coverageFromIntervention(intervention: InterventionEntity): VeCoverageSummary? {

    if (intervention.unitVeCoverageUnavailable == true) {

        return VeCoverageSummary(unavailable = true)

    }

    val expected = intervention.unitVeCoverageExpected ?: return null

    return VeCoverageSummary(

        unavailable = false,

        attested = intervention.unitVeCoverageAttested,

        expected = expected,

        complete = intervention.unitVeCoverageComplete,

    )

}



fun coverageFromApi(api: UnitVeStatusDto): VeCoverageSummary? =

    when {

        api.coverageUnavailable == true -> VeCoverageSummary(unavailable = true)

        api.coverageExpected != null ->

            VeCoverageSummary(

                unavailable = false,

                attested = api.coverageAttested,

                expected = api.coverageExpected,

                complete = api.coverageComplete,

            )

        else -> null

    }



fun formatVeCoverageLabel(coverage: VeCoverageSummary?): String? {

    if (coverage == null || coverage.unavailable) return null

    val expected = coverage.expected ?: return null

    if (expected <= 0) return null

    val attested = coverage.attested ?: 0

    return "$attested/$expected appareils"

}

fun formatVeCoverageHint(coverage: VeCoverageSummary?): String? {
    val label = formatVeCoverageLabel(coverage) ?: return null
    val attested = coverage?.attested ?: 0
    val expected = coverage?.expected ?: 0
    return when {
        expected > 0 && attested < expected ->
            "$label · attestation manquante ou non rattachée"
        coverage?.closureProjected == true && attested >= expected ->
            "$label (après clôture)"
        else -> label
    }
}

/**
 * Projette la couverture pendant la clôture : attestation locale + type VE coché,
 * avant push serveur (GET /ve-status reste à 0/X).
 */
fun projectClosureCoverage(
    base: VeCoverageSummary?,
    closureIsVe: Boolean,
    localAttestationEquipmentOrders: Set<Int>,
    unitEquipments: List<AttestableEquipmentInput>,
    equipmentOrderForInput: (AttestableEquipmentInput) -> Int?,
): VeCoverageSummary? {
    if (base == null) return null
    if (!closureIsVe || base.unavailable) return base
    val expected = base.expected ?: return base
    if (expected <= 0) return base
    if (localAttestationEquipmentOrders.isEmpty()) return base

    val equipmentByOrder =
        unitEquipments.mapNotNull { input ->
            val order = equipmentOrderForInput(input) ?: return@mapNotNull null
            order to input
        }.toMap()

    val projectedAnchors = mutableSetOf<String>()
    for (order in localAttestationEquipmentOrders) {
        val equipment = equipmentByOrder[order] ?: continue
        val anchor = attestationAnchorEquipmentId(equipment, unitEquipments) ?: continue
        projectedAnchors.add(anchor)
    }

    if (projectedAnchors.isEmpty()) return base

    val baseAttested = base.attested ?: 0
    val projectedAttested = projectedAnchors.size.coerceAtMost(expected)
    val attested = maxOf(baseAttested, projectedAttested)
    if (attested == baseAttested && !base.closureProjected) return base

    return base.copy(
        attested = attested,
        complete = attested >= expected,
        closureProjected = attested > baseAttested || base.closureProjected,
    )
}



fun buildUnitVeContextFromLocal(

    contractSource: InterventionEntity?,

    lastHistory: InterventionHistoryEntity?,

    today: LocalDate = LocalDate.now(SavioTimeZone.appZone),

): UnitVeContext {

    val contractInfo = contractSource?.let { contractSummaryFrom(it) }

    val lastVe = lastVeSummaryFrom(lastHistory)

    val nextDate = computeNextVeDate(lastVe, contractInfo?.renewalDate)

    val nextVe = nextDate?.let { nextVeDisplay(it, today) }

    val coverage = contractSource?.let { coverageFromIntervention(it) }

    return UnitVeContext(

        contractInfo = contractInfo,

        lastVe = lastVe,

        nextVe = nextVe,

        coverage = coverage,

    )

}



fun mergeUnitVeContextWithApi(

    local: UnitVeContext,

    api: UnitVeStatusDto,

    today: LocalDate = LocalDate.now(SavioTimeZone.appZone),

): UnitVeContext {

    if (api.status == "na") return local



    val lastVe =

        local.lastVe

            ?: api.lastVeCompletedAt?.let { iso ->

                parseIsoToLocalDate(iso)?.let { date ->

                    LastVeSummary(completedAt = date, technicianFirstName = null)

                }

            }



    val nextVe =

        local.nextVe

            ?: parseVePrevisionalMonth(api.nextVePrevisionalMonth)?.let { monthStart ->

                nextVeDisplay(monthStart, today)

            }



    val coverage = coverageFromApi(api) ?: local.coverage



    return UnitVeContext(

        contractInfo = local.contractInfo,

        lastVe = lastVe,

        nextVe = nextVe,

        coverage = coverage,

    )

}



enum class VeHintUrgency { OK, SOON, OVERDUE, NEUTRAL }



data class VeListHint(

    val text: String,

    val urgency: VeHintUrgency,

)



fun formatVeListHint(

    veStatus: String?,

    lastVeCompletedAt: String?,

    nextVePrevisionalMonth: String?,

    displayContractStatus: String? = null,

    contractStatus: String? = null,

    coverageUnavailable: Boolean? = null,

    coverageAttested: Int? = null,

    coverageExpected: Int? = null,

    today: LocalDate = LocalDate.now(SavioTimeZone.appZone),

): VeListHint? {

    val status = veStatus?.trim()?.lowercase()

    if (status == "na" || status.isNullOrEmpty()) {

        if (displayContractStatus == "hors_contrat" || contractStatus == "hors_contrat") {

            return null

        }

        if (displayContractStatus == null && contractStatus == null) return null

    }



    val nextMonth = parseVePrevisionalMonth(nextVePrevisionalMonth)

    val nextDate = nextMonth ?: lastVeCompletedAt?.let { parseIsoToLocalDate(it)?.plusYears(1) }



    val urgency =

        when {

            status == "done" && (nextDate == null || !nextDate.isBefore(today)) -> VeHintUrgency.OK

            nextDate != null && nextDate.isBefore(today) -> VeHintUrgency.OVERDUE

            nextDate != null && !nextDate.isAfter(today.plusDays(30)) -> VeHintUrgency.SOON

            status == "partial" -> VeHintUrgency.SOON

            status == "pending" -> VeHintUrgency.OVERDUE

            else -> VeHintUrgency.NEUTRAL

        }



    val coverageSuffix =

        when {

            coverageUnavailable == true -> ""

            coverageExpected != null && coverageExpected > 0 -> {

                val attested = coverageAttested ?: 0

                " · $attested/$coverageExpected"

            }

            else -> ""

        }



    val text =

        when (status) {

            "done" -> {

                val monthLabel = nextMonth?.format(monthFormatter())

                if (monthLabel != null) "VE · $monthLabel$coverageSuffix" else "VE à jour$coverageSuffix"

            }

            "partial" -> "VE partielle$coverageSuffix"

            "pending" -> if (urgency == VeHintUrgency.OVERDUE) "VE due$coverageSuffix" else "VE à planifier$coverageSuffix"

            else -> {

                val monthLabel = nextMonth?.format(monthFormatter())

                when {

                    monthLabel != null -> "VE · $monthLabel$coverageSuffix"

                    lastVeCompletedAt != null -> "VE à jour$coverageSuffix"

                    displayContractStatus == "sous_contrat" || contractStatus == "sous_contrat" ->

                        "VE à planifier$coverageSuffix"

                    else -> return null

                }

            }

        }



    return VeListHint(text = text, urgency = urgency)

}



fun parseVePrevisionalMonth(raw: String?): LocalDate? {

    val trimmed = raw?.trim().orEmpty()

    if (trimmed.isEmpty()) return null

    return runCatching {

        YearMonth.parse(trimmed.take(7)).atDay(1)

    }.getOrNull()

        ?: runCatching {

            val parts = trimmed.split("-", "/", " ")

                .filter { it.isNotBlank() }

            if (parts.size >= 2) {

                val year = parts[0].toInt()

                val month = parts[1].toInt()

                LocalDate.of(year, month, 1)

            } else {

                null

            }

        }.getOrNull()

}



private fun monthFormatter(): DateTimeFormatter =

    DateTimeFormatter.ofPattern("MM/yyyy", Locale.FRANCE)

