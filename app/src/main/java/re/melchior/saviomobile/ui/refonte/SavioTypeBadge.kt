package re.melchior.saviomobile.ui.refonte

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import re.melchior.saviomobile.ui.theme.SavioRefonte

fun savioTypeIcon(typeLabel: String, typeCode: String? = null): ImageVector {
    val key = (typeCode ?: typeLabel).lowercase()
    return when {
        key.contains("mise") && key.contains("service") -> Icons.Outlined.PowerSettingsNew
        key.contains("visite") || key.contains("entretien") -> Icons.Outlined.Assignment
        key.contains("maintenance") -> Icons.Outlined.Build
        key.contains("dépannage") || key.contains("depannage") -> Icons.Outlined.Build
        typeLabel.contains("Mise en service", ignoreCase = true) -> Icons.Outlined.PowerSettingsNew
        typeLabel.contains("Visite", ignoreCase = true) -> Icons.Outlined.Assignment
        typeLabel.contains("Maintenance", ignoreCase = true) -> Icons.Outlined.Build
        typeLabel.contains("Dépannage", ignoreCase = true) -> Icons.Outlined.Build
        else -> Icons.Outlined.Build
    }
}

@Composable
fun SavioTypeBadge(
    typeLabel: String,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
    typeCode: String? = null,
) {
    Box(
        modifier =
            modifier
                .size(size)
                .background(SavioRefonte.Tint, RoundedCornerShape(13.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = savioTypeIcon(typeLabel, typeCode),
            contentDescription = typeLabel,
            tint = SavioRefonte.Navy,
            modifier = Modifier.size(size * 0.48f),
        )
    }
}
