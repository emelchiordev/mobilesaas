package re.melchior.saviomobile.ui.screen.tournee

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val FollowUpBg = Color(0xFFFFEDD5)
private val FollowUpFg = Color(0xFF9A3412)

@Composable
fun FollowUpPendingBanner(
    note: String?,
    canResolve: Boolean,
    isResolving: Boolean,
    onResolve: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = FollowUpBg,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(
                text = "Intervention à revoir",
                style = MaterialTheme.typography.titleSmall,
                color = FollowUpFg,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = note?.trim()?.takeIf { it.isNotEmpty() }
                    ?: "Complétez le suivi (rapport, photos, devis) depuis les onglets ci-dessous, puis marquez comme traité.",
                style = MaterialTheme.typography.bodyMedium,
                color = FollowUpFg,
                modifier = Modifier.padding(top = 4.dp),
            )
            if (canResolve) {
                Button(
                    onClick = onResolve,
                    enabled = !isResolving,
                    modifier = Modifier.padding(top = 10.dp),
                ) {
                    Text(if (isResolving) "Enregistrement…" else "Marquer comme traité")
                }
            } else {
                Text(
                    text = "Clôture du suivi réservée au bureau.",
                    style = MaterialTheme.typography.bodySmall,
                    color = FollowUpFg,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
fun FollowUpBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = FollowUpBg,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = "À revoir",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = FollowUpFg,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
