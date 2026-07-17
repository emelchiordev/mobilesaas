package re.savio.mobile.ui.screen.intervention.diagnostic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import re.savio.mobile.data.remote.dto.RagSynthesizeResponseDto
import re.savio.mobile.ui.refonte.SavioBetaBadge
import re.savio.mobile.ui.theme.SavioRefonte

@Composable
fun DiagnosticSynthesisCard(
    synthesis: RagSynthesizeResponseDto,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF0F7FF),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Analyse IA",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SavioRefonte.Navy,
                )
                SavioBetaBadge()
            }

            SynthesisSection(
                title = "Causes probables",
                items = synthesis.causes,
                bullet = "•",
            )

            SynthesisSection(
                title = "Ordre de vérification",
                items = synthesis.verification,
                numbered = true,
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Solution fréquente",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SavioRefonte.Ink,
                )
                Text(
                    text = synthesis.solutionFrequente,
                    fontSize = 13.sp,
                    color = SavioRefonte.Ink,
                )
            }

            Text(
                text = "Analyse IA · résultats sources ci-dessous",
                fontSize = 11.sp,
                color = SavioRefonte.Muted,
            )
        }
    }
}

@Composable
private fun SynthesisSection(
    title: String,
    items: List<String>,
    bullet: String? = null,
    numbered: Boolean = false,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = SavioRefonte.Ink,
        )
        items.forEachIndexed { index, item ->
            Text(
                text =
                    if (numbered) {
                        "${index + 1}. $item"
                    } else {
                        "$bullet $item"
                    },
                fontSize = 13.sp,
                color = SavioRefonte.Ink,
            )
        }
    }
}
