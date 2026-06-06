package re.melchior.saviomobile.ui.screen.intervention.cloture

import re.melchior.saviomobile.data.local.entity.AnomalyDraftEntity
import re.melchior.saviomobile.data.local.entity.AttestationVeEntity
import re.melchior.saviomobile.data.local.entity.InstallationCheckEntity
import re.melchior.saviomobile.data.local.entity.InvoiceLineEntity
import re.melchior.saviomobile.data.local.entity.MeasureEntity
import re.melchior.saviomobile.data.local.entity.PacMeasureEntity

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
                formatAttestationCombustion(att) != null ||
                    formatExpansionPressure(att, null) != null
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
