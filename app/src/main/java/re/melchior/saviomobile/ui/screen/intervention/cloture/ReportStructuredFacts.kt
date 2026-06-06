package re.melchior.saviomobile.ui.screen.intervention.cloture

import re.melchior.saviomobile.data.local.entity.AnomalyDraftEntity
import re.melchior.saviomobile.data.local.entity.AnomalyTypeEntity
import re.melchior.saviomobile.data.local.entity.AttestationVeEntity
import re.melchior.saviomobile.data.local.entity.EquipmentEntity
import re.melchior.saviomobile.data.local.entity.InstallationCheckEntity
import re.melchior.saviomobile.data.local.entity.InvoiceLineEntity
import re.melchior.saviomobile.data.local.entity.MeasureEntity
import re.melchior.saviomobile.data.local.entity.PacMeasureEntity
import re.melchior.saviomobile.ui.theme.formatEquipmentTypeLabel
import re.melchior.saviomobile.util.GasPipeValidityStatus
import re.melchior.saviomobile.util.SavioTimeZone
import re.melchior.saviomobile.util.evaluateGasPipeValidity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val STRUCTURED_FACTS_MAX_CHARS = 800

private const val INSTALLATION_FACT_BLOCK_HEADER =
    "Installation logement (données générales — tuyau gaz cuisine, turbidité eau circuit) :"

fun buildStructuredFactsBlocks(
    equipments: List<EquipmentEntity>,
    attestations: List<AttestationVeEntity>,
    measures: List<MeasureEntity>,
    pacMeasures: List<PacMeasureEntity>,
    anomalyDrafts: List<AnomalyDraftEntity>,
    anomalyCatalog: List<AnomalyTypeEntity>,
    invoiceLines: List<InvoiceLineEntity>,
    installationCheck: InstallationCheckEntity? = null,
): List<String> {
    val partLabels = billableInvoiceLines(invoiceLines).map(::formatPartLineForFacts)
    val anomalyDisplays = buildAnomalyDraftDisplays(anomalyDrafts, anomalyCatalog)
    val installationAnomalies =
        anomalyDisplays
            .filter { belongsToInstallation(it.draft) }
            .map { formatAnomalySummary(it) }

    val blocks = mutableListOf<String>()

    val installationBlock =
        buildInstallationFactBlock(installationCheck, installationAnomalies)
    if (installationBlock != null) {
        blocks.add(installationBlock)
    }

    val equipmentBlocks =
        if (equipments.isEmpty()) {
            listOf(
                buildEquipmentFactBlock(
                    header = "Intervention logement",
                    combustion = null,
                    expansion = null,
                    pacPressure = null,
                    anomalies =
                        if (installationBlock == null) {
                            anomalyDisplays.map { formatAnomalySummary(it) }
                        } else {
                            emptyList()
                        },
                    parts = partLabels,
                    replaced = false,
                ),
            )
        } else {
            equipments.map { equipment ->
                val order = equipment.order
                val att = attestations.firstOrNull { it.equipmentOrder == order }
                val measure = measures.firstOrNull { it.equipmentOrder == order }
                val pac = pacMeasures.firstOrNull { it.equipmentOrder == order }
                val combustion = formatAttestationCombustion(att) ?: formatMeasureCombustion(measure)
                val expansion = formatExpansionPressure(att, measure)
                val pacPressure = formatPacPressure(pac)
                val header = formatEquipmentHeader(equipment)
                val equipmentAnomalies =
                    anomalyDisplays
                        .filter { belongsToEquipment(it.draft, equipment.id) }
                        .map { formatAnomalySummary(it) }
                buildEquipmentFactBlock(
                    header = header,
                    combustion = combustion,
                    expansion = expansion,
                    pacPressure = pacPressure,
                    anomalies = equipmentAnomalies,
                    parts = if (equipment.isPrimary) partLabels else emptyList(),
                    replaced = false,
                )
            }
        }

    blocks.addAll(equipmentBlocks)
    return truncateStructuredFacts(blocks)
}

private fun formatEquipmentHeader(equipment: EquipmentEntity): String {
    val brandModel = listOfNotNull(
        equipment.brand?.trim()?.takeIf { it.isNotEmpty() },
        equipment.model?.trim()?.takeIf { it.isNotEmpty() },
    ).joinToString(" ")
    val typePart = equipment.typeCode?.trim()?.takeIf { it.isNotEmpty() }
        ?: formatEquipmentTypeLabel(equipment.typeCode.orEmpty()).takeIf { it.isNotBlank() }
    val powerPart = equipment.powerKw?.trim()?.takeIf { it.isNotEmpty() }?.let { "$it kW" }
    val label = when {
        brandModel.isNotEmpty() -> listOfNotNull(brandModel, powerPart).joinToString(" ")
        !typePart.isNullOrBlank() -> listOfNotNull(typePart, powerPart).joinToString(" ")
        else -> powerPart ?: "Équipement"
    }
    return "Appareil ${equipment.order} — $label"
}

internal fun formatAttestationCombustion(att: AttestationVeEntity?): String? {
    att ?: return null
    val parts = buildList {
        att.co2Fumees.trim().takeIf { it.isNotEmpty() }?.let { add("CO2 $it%") }
        val co = att.co.trim().takeIf { it.isNotEmpty() }
            ?: att.coConduitPpm?.let { trimDecimal(it) }?.let { "CO ${it}ppm" }
        if (co != null) add(co)
        att.rendementEvalue.trim().takeIf { it.isNotEmpty() }?.let { add("rendement $it%") }
        att.tempFumees.trim().takeIf { it.isNotEmpty() }?.let { add("température fumées ${it}°C") }
        att.indiceNoircissement.trim().takeIf { it.isNotEmpty() }?.let { add("indice fumée $it") }
    }
    if (parts.isEmpty()) return null
    return "Mesures combustion: ${parts.joinToString(", ")}."
}

internal fun formatMeasureCombustion(measure: MeasureEntity?): String? {
    measure ?: return null
    val parts = buildList {
        measure.co2?.let { add("CO2 ${trimDecimal(it)}%") }
        measure.co?.let { add("CO ${trimDecimal(it)}ppm") }
        (measure.rend ?: measure.eta)?.let { add("rendement ${trimDecimal(it)}%") }
        measure.temfu?.let { add("température fumées ${trimDecimal(it)}°C") }
    }
    if (parts.isEmpty()) return null
    return "Mesures combustion: ${parts.joinToString(", ")}."
}

internal fun formatExpansionPressure(
    att: AttestationVeEntity?,
    measure: MeasureEntity?,
): String? {
    val bp = att?.pressionBp?.trim()?.takeIf { it.isNotEmpty() }
    val hp = att?.pressionHp?.trim()?.takeIf { it.isNotEmpty() }
    if (!bp.isNullOrEmpty() && !hp.isNullOrEmpty()) {
        return "Pression circuit: ${bp}bar → ${hp}bar."
    }
    val before = measure?.prega ?: measure?.pregas
    val after = measure?.pregm ?: measure?.pregn
    if (before != null && after != null) {
        return "Vase expansion: ${trimDecimal(before)}bar → ${trimDecimal(after)}bar."
    }
    if (before != null) {
        return "Vase expansion: ${trimDecimal(before)}bar."
    }
    return null
}

internal fun formatPacPressure(pac: PacMeasureEntity?): String? {
    pac ?: return null
    val value = pac.pacPression2.trim().takeIf { it.isNotEmpty() } ?: return null
    return "Pression PAC: ${value}bar."
}

private fun formatPartLineForFacts(line: InvoiceLineEntity): String {
    val ref = line.reference?.trim()?.takeIf { it.isNotEmpty() }
    val qtyPrefix =
        if (line.quantity != 1.0) {
            val qty =
                if (line.quantity % 1.0 == 0.0) line.quantity.toInt().toString()
                else line.quantity.toString()
            "${qty}× "
        } else {
            ""
        }
    val label = line.label.trim()
    return if (ref != null) "$ref $qtyPrefix$label".trim() else "$qtyPrefix$label".trim()
}

private fun buildEquipmentFactBlock(
    header: String,
    combustion: String?,
    expansion: String?,
    pacPressure: String?,
    anomalies: List<String>,
    parts: List<String>,
    replaced: Boolean,
): String {
    return buildList {
        add(header)
        if (replaced) add("Appareil remplacé lors de cette intervention.")
        combustion?.let { add(it) }
        expansion?.let { add(it) }
        pacPressure?.let { add(it) }
        if (anomalies.isNotEmpty()) {
            add("Anomalies: ${anomalies.joinToString("; ")}.")
        }
        if (parts.isNotEmpty()) {
            add("Pièces: ${parts.joinToString("; ")}.")
        }
    }.joinToString("\n")
}

private val displayDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRANCE)

private fun buildInstallationFactBlock(
    check: InstallationCheckEntity?,
    installationAnomalies: List<String>,
): String? {
    check ?: return if (installationAnomalies.isEmpty()) null else buildInstallationOnlyAnomaliesBlock(installationAnomalies)
    val lines = buildInstallationCheckLines(check).toMutableList()
    if (installationAnomalies.isNotEmpty()) {
        lines += "Anomalies logement: ${installationAnomalies.joinToString("; ")}."
    }
    if (lines.isEmpty()) return null
    return buildList {
        add(INSTALLATION_FACT_BLOCK_HEADER)
        addAll(lines)
    }.joinToString("\n")
}

private fun buildInstallationOnlyAnomaliesBlock(anomalies: List<String>): String =
    buildList {
        add(INSTALLATION_FACT_BLOCK_HEADER)
        add("Anomalies logement: ${anomalies.joinToString("; ")}.")
    }.joinToString("\n")

private fun belongsToInstallation(draft: AnomalyDraftEntity): Boolean {
    if (draft.scope == "installation") return true
    if (draft.equipmentId.isNullOrBlank() && draft.scope != "equipment") return true
    return false
}

private fun belongsToEquipment(draft: AnomalyDraftEntity, equipmentId: String): Boolean {
    if (draft.scope == "installation") return false
    val draftEquipmentId = draft.equipmentId?.trim().orEmpty()
    if (draftEquipmentId.isEmpty()) return false
    return draftEquipmentId == equipmentId
}

private fun formatAnomalySummary(display: AnomalyDraftDisplay): String =
    buildString {
        append(display.label)
        append(" (")
        append(display.levelTag)
        append(')')
        display.draft.action?.trim()?.takeIf { it.isNotEmpty() }?.let { action ->
            append(" — ")
            append(action)
        }
        if (display.draft.corrected) {
            append(" — corrigée sur place")
        }
    }

internal fun buildInstallationCheckLines(check: InstallationCheckEntity): List<String> {
    val lines = mutableListOf<String>()
    if (check.turbidityTested) {
        val stateLabel = formatTurbidityStateLabel(check.turbidityState)
        val ntu = check.turbidityNtu.trim().takeIf { it.isNotEmpty() }
        lines +=
            when {
                ntu != null && stateLabel != null ->
                    "Turbidité eau circuit: $ntu NTU — Eau $stateLabel."
                ntu != null -> "Turbidité eau circuit: $ntu NTU."
                stateLabel != null -> "Turbidité eau circuit — Eau $stateLabel."
                else -> "Turbidité eau circuit mesurée."
            }
    }
    if (check.gasPipeReplaced) {
        lines += "Tuyau gaz cuisine (installation logement) : remplacé."
    }
    if (
        evaluateGasPipeValidity(
            gasPipeType = check.gasPipeType,
            gasPipeValidityDateIso = check.gasPipeValidityDate,
            today = LocalDate.now(SavioTimeZone.appZone),
        ) == GasPipeValidityStatus.Expired
    ) {
        val displayDate =
            runCatching { LocalDate.parse(check.gasPipeValidityDate) }
                .getOrNull()
                ?.format(displayDateFormatter)
        lines +=
            if (displayDate != null) {
                "Tuyau gaz cuisine (installation logement) : hors validité depuis $displayDate."
            } else {
                "Tuyau gaz cuisine (installation logement) : hors validité."
            }
    }
    return lines
}

private fun formatTurbidityStateLabel(state: String?): String? =
    when (state) {
        "propre" -> "propre"
        "embouee" -> "embouée"
        "tres_embouee" -> "très embouée"
        else -> null
    }

private fun truncateStructuredFacts(blocks: List<String>): List<String> {
    val result = mutableListOf<String>()
    var total = 0
    for (block in blocks) {
        if (total + block.length > STRUCTURED_FACTS_MAX_CHARS) break
        result.add(block)
        total += block.length + 2
    }
    return result
}

private fun trimDecimal(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
