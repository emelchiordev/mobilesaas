package re.melchior.saviomobile.ui.screen.intervention

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.melchior.saviomobile.ui.theme.SavioPalette

@Composable
fun QuitInterventionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(12.dp),
        containerColor = SavioPalette.SurfaceCard,
        title = {
            Text(
                text = "Quitter l'intervention ?",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = SavioPalette.TextPrimary,
            )
        },
        text = {
            Text(
                text =
                    "Toute saisie en cours sera perdue. L'intervention repassera en \"À venir\".",
                fontSize = 14.sp,
                color = SavioPalette.TextSecondary,
                lineHeight = 20.sp,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE24B4A),
                    ),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("Quitter", color = Color.White, fontSize = 13.sp)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, SavioPalette.Accent),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("Rester", color = SavioPalette.TextPrimary, fontSize = 13.sp)
            }
        },
    )
}
