package re.melchior.saviomobile.ui.screen.intervention.cloture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@Composable
fun ContractVeSummaryCard(
    contractInfo: ContractSummary?,
    lastVe: LastVeSummary?,
    nextVe: NextVeDisplay?,
    today: LocalDate = LocalDate.now(),
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "CONTRAT & ENTRETIEN",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )

            if (contractInfo != null) {
                SummaryRow(
                    label = "Contrat",
                    value = "${contractTypeLabel(contractInfo.type)} · ${contractStatusLabel(contractInfo.status)}",
                )
                contractInfo.renewalDate?.let { renewal ->
                    val expired = renewal.isBefore(today)
                    SummaryRow(
                        label = "Renouvellement",
                        value = if (expired) {
                            "Expiré le ${formatClosureDate(renewal)}"
                        } else {
                            formatClosureDate(renewal)
                        },
                        valueColor = if (expired) Color(0xFFB71C1C) else null,
                    )
                }
            } else {
                Text(
                    text = "Pas de contrat enregistré",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic,
                )
            }

            if (lastVe != null) {
                val tech = lastVe.technicianFirstName?.let { " · $it" }.orEmpty()
                SummaryRow(
                    label = "Dernier entretien",
                    value = "${formatClosureDate(lastVe.completedAt)}$tech",
                )
            } else if (contractInfo == null) {
                Text(
                    text = "Aucun entretien enregistré",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic,
                )
            } else {
                Text(
                    text = "Aucun entretien enregistré",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic,
                )
            }

            nextVe?.let { next ->
                val suffix = when (next.urgency) {
                    VeDueUrgency.OVERDUE -> " · Dépassé"
                    VeDueUrgency.SOON -> " · Bientôt"
                    VeDueUrgency.OK -> ""
                }
                val color = when (next.urgency) {
                    VeDueUrgency.OVERDUE -> Color(0xFFB71C1C)
                    VeDueUrgency.SOON -> Color(0xFFE65100)
                    VeDueUrgency.OK -> Color(0xFF2E7D32)
                }
                SummaryRow(
                    label = "Prochain entretien prévu",
                    value = formatClosureDate(next.date) + suffix,
                    valueColor = color,
                )
            }
        }
    }
}

@Composable
fun ClosureConsequencesCard(
    consequences: List<ClosureConsequence>,
    modifier: Modifier = Modifier,
) {
    if (consequences.isEmpty()) return

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "CE QUI VA SE PASSER",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
            consequences.forEach { item ->
                ConsequenceRow(item)
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.6f),
        )
    }
}

@Composable
private fun ConsequenceRow(item: ClosureConsequence) {
    val tint = when {
        item.isWarning -> Color(0xFFE65100)
        else -> Color(0xFF2E7D32)
    }
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = item.text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (item.isWarning) {
                Color(0xFFE65100)
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
    }
}
