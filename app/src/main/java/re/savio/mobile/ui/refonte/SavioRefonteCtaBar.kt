package re.savio.mobile.ui.refonte

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.savio.mobile.ui.theme.SavioRefonte
import re.savio.mobile.ui.theme.ViolettTokens

@Composable
fun SavioRefonteCtaBar(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector = Icons.Filled.Add,
) {
    SavioViolettGradientCtaBar(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        icon = icon,
    )
}

@Composable
fun SavioViolettGradientCtaBar(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector = Icons.Filled.PlayArrow,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    Color.Transparent,
                                    SavioRefonte.BgPage.copy(alpha = 0.72f),
                                    SavioRefonte.BgPage,
                                ),
                        ),
                )
                .padding(horizontal = 18.dp)
                .padding(top = 14.dp, bottom = 30.dp)
                .navigationBarsPadding(),
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = ViolettTokens.FabShadow),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(SavioRefonte.PrimaryGradient, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = icon, contentDescription = null)
                    Spacer(modifier = Modifier.width(9.dp))
                    Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun SavioRefonteEditBottomBar(
    saveLabel: String,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    isSaving: Boolean = false,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(SavioRefonte.BgPage)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding(),
    ) {
        Button(
            onClick = onSave,
            enabled = !isSaving,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = ViolettTokens.FabShadow),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(SavioRefonte.PrimaryGradient, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = saveLabel, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        SavioGhostButton(
            text = "Annuler",
            onClick = onCancel,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Deprecated("Utiliser SavioViolettGradientCtaBar", ReplaceWith("SavioViolettGradientCtaBar(text, onClick, modifier, icon = icon)"))
@Composable
fun SavioRefonteOrangeCtaBar(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.PlayArrow,
) {
    SavioViolettGradientCtaBar(text = text, onClick = onClick, modifier = modifier, icon = icon)
}
