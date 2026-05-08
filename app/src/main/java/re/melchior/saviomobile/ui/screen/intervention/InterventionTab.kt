package re.melchior.saviomobile.ui.screen.intervention

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.ui.graphics.vector.ImageVector

enum class InterventionTab(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    DETAIL(
        label = "Détail",
        icon = Icons.Outlined.Info,
        selectedIcon = Icons.Filled.Info,
    ),
    EQUIPEMENTS(
        label = "Équipements",
        icon = Icons.Outlined.Build,
        selectedIcon = Icons.Filled.Build,
    ),
    PHOTOS(
        label = "Photos",
        icon = Icons.Outlined.CameraAlt,
        selectedIcon = Icons.Filled.CameraAlt,
    ),
    FACTURE(
        label = "Facture",
        icon = Icons.Outlined.Receipt,
        selectedIcon = Icons.Filled.Receipt,
    ),
}
