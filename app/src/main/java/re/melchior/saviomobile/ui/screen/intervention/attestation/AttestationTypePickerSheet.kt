package re.melchior.saviomobile.ui.screen.intervention.attestation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.melchior.saviomobile.ui.theme.SavioPalette

fun attestationTypeLabel(type: String): String =
    when (type) {
        "GAZ" -> "Chaudière Gaz"
        "FIOUL" -> "Chaudière Fioul"
        "BOIS" -> "Chaudière Bois"
        "PAC" -> "PAC"
        "PAC_HYBRIDE_GAZ" -> "PAC Hybride Gaz"
        "PAC_HYBRIDE_FIOUL" -> "PAC Hybride Fioul"
        else -> type
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttestationTypePickerSheet(
    suggestedType: String?,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val allTypes = listOf(
        Triple("GAZ", "🔥", "Chaudière Gaz"),
        Triple("FIOUL", "🛢️", "Chaudière Fioul"),
        Triple("BOIS", "🪵", "Chaudière Bois"),
        Triple("PAC", "♨️", "PAC"),
        Triple("PAC_HYBRIDE_GAZ", "♨️🔥", "PAC Hybride Gaz"),
        Triple("PAC_HYBRIDE_FIOUL", "♨️🛢️", "PAC Hybride Fioul"),
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 32.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Type d'attestation",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            allTypes.forEach { (type, emoji, label) ->
                val isSuggested = type == suggestedType

                Surface(
                    onClick = { onSelect(type) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color =
                        if (isSuggested) {
                            SavioPalette.Accent.copy(alpha = 0.12f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerLow
                        },
                    border =
                        if (isSuggested) {
                            BorderStroke(1.5.dp, SavioPalette.Accent)
                        } else {
                            null
                        },
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 12.dp,
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(text = emoji, fontSize = 20.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = label,
                                fontSize = 14.sp,
                                fontWeight = if (isSuggested) {
                                    FontWeight.SemiBold
                                } else {
                                    FontWeight.Normal
                                },
                            )
                            if (isSuggested) {
                                Text(
                                    text = "Suggestion basée sur l'équipement",
                                    fontSize = 11.sp,
                                    color = SavioPalette.Accent,
                                )
                            }
                        }
                        if (isSuggested) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                tint = SavioPalette.Accent,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
