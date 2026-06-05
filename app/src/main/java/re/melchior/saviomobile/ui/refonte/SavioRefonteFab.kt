package re.melchior.saviomobile.ui.refonte

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import re.melchior.saviomobile.ui.theme.SavioDimens
import re.melchior.saviomobile.ui.theme.SavioRefonte

@Composable
fun SavioRefonteFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Nouvelle intervention",
) {
    Box(
        modifier =
            modifier
                .size(SavioDimens.FabSize)
                .shadow(8.dp, RoundedCornerShape(SavioDimens.RadiusFab), clip = false)
                .background(
                    brush =
                        Brush.linearGradient(
                            colors = listOf(SavioRefonte.OrangeAction, SavioRefonte.OrangeDark),
                        ),
                    shape = RoundedCornerShape(SavioDimens.RadiusFab),
                )
                .clickable(onClick = onClick)
                .semantics { role = Role.Button },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(28.dp),
        )
    }
}
