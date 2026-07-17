package re.savio.mobile.ui.screen.intervention.cloture

import re.savio.mobile.data.local.entity.AnomalyDraftEntity
import re.savio.mobile.data.local.entity.AttestationVeEntity
import re.savio.mobile.data.local.entity.InstallationCheckEntity
import re.savio.mobile.data.local.entity.InvoiceLineEntity
import re.savio.mobile.data.local.entity.MeasureEntity
import re.savio.mobile.data.local.entity.PacMeasureEntity
import re.savio.mobile.ui.util.hasMeaningfulData

data class ReportGenerationContentAssessment(
    val technicalFactsCount: Int,
    val hasExplicitContent: Boolean,
    val canGenerate: Boolean,
)

fun assessReportGenerationContent(
    technicianNotes: String?,
    invoiceLines: List<InvoiceLineEntity>,
    anomalyDrafts: List<AnomalyDraftEntity>,
    attestations: List<AttestationVeEntity>,
    measures: List<MeasureEntity>,
    pacMeasures: List<PacMeasureEntity>,
    coldMeasureCount: Int,
    installationCheck: InstallationCheckEntity?,
    attestationPoints: Map<AttestationPointsKey, Map<String, String>> = emptyMap(),
): ReportGenerationContentAssessment {
    val hasExplicitContent =
        !technicianNotes.isNullOrBlank() ||
            billableInvoiceLines(invoiceLines).isNotEmpty() ||
            anomalyDrafts.isNotEmpty() ||
            attestations.isNotEmpty() ||
            coldMeasureCount > 0

    var technicalFactsCount = 0
    if (measures.isNotEmpty()) {
        technicalFactsCount += measures.count { measure ->
            formatMeasureCombustion(measure) != null ||
                formatExpansionPressure(null, measure) != null
        }
    }
    if (attestations.isNotEmpty()) {
        technicalFactsCount +=
            attestations.count { att ->
                val points = attestationPoints[attestationPointsKey(att.equipmentOrder, att.type)].orEmpty()
                when {
                    att.type == "ECS" -> att.hasMeaningfulData(points)
                    else ->
                        formatAttestationCombustion(att) != null ||
                            formatExpansionPressure(att, null) != null
                }
            }
    }
    if (pacMeasures.isNotEmpty()) {
        technicalFactsCount += pacMeasures.count { formatPacPressure(it) != null }
    }
    technicalFactsCount += countInstallationCheckTechnicalFacts(installationCheck)

    val canGenerate = hasExplicitContent || technicalFactsCount > 0
    return ReportGenerationContentAssessment(
        technicalFactsCount = technicalFactsCount,
        hasExplicitContent = hasExplicitContent,
        canGenerate = canGenerate,
    )
}

internal fun countInstallationCheckTechnicalFacts(check: InstallationCheckEntity?): Int {
    check ?: return 0
    val lineCount = buildInstallationCheckLines(check).size
    if (lineCount > 0) return lineCount
    if (check.turbidityTested) return 1
    if (check.gasPipeReplaced) return 1
    if (!check.gasPipeType.isNullOrBlank()) return 1
    if (!check.gasTapCompliant.isNullOrBlank()) return 1
    if (check.notes.isNotBlank()) return 1
    if (check.gasPipeValidityDate.isNotBlank()) return 1
    return 0
}
