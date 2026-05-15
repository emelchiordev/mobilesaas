package re.melchior.saviomobile.ui.screen.tournee

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.melchior.saviomobile.ui.theme.SavioPalette

@Composable
fun ResumeBanner(
    intervention: InterventionItem,
    onResume: () -> Unit,
    onDismiss: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(SavioPalette.WarningTintBg)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "Intervention interrompue",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = SavioPalette.TextPrimary,
            )
            Text(
                text = intervention.clientName,
                fontSize = 11.sp,
                color = SavioPalette.Accent,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onDismiss) {
                Text("Ignorer", color = SavioPalette.TextSecondary, fontSize = 12.sp)
            }
            Button(
                onClick = onResume,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = SavioPalette.Accent,
                        contentColor = SavioPalette.OnAccent,
                    ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            ) {
                Text("Reprendre", fontSize = 12.sp)
            }
        }
    }
}
