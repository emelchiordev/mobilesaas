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

@Composable
fun QuitInterventionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(12.dp),
        containerColor = Color.White,
        title = {
            Text(
                text = "Quitter l'intervention ?",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2C2C2A)
            )
        },
        text = {
            Text(
                text = "Toute saisie en cours sera perdue. L'intervention repassera en \"À venir\".",
                fontSize = 14.sp,
                color = Color(0xFF888780),
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE24B4A)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Quitter", color = Color.White, fontSize = 13.sp)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, Color(0xFF185FA5)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Rester", color = Color(0xFF185FA5), fontSize = 13.sp)
            }
        }
    )
}
