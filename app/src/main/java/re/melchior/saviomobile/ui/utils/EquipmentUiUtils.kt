package re.melchior.saviomobile.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.ui.graphics.vector.ImageVector

fun equipmentIcon(typeCode: String?): ImageVector {
    val tc = (typeCode ?: "").uppercase()
    return when {
        tc.contains("CHAUDIERE") -> Icons.Filled.Whatshot
        tc.contains("PAC") -> Icons.Filled.Air
        tc.contains("CLIM") -> Icons.Filled.AcUnit
        tc.contains("BALLON") || tc.contains("CHAUFFE") -> Icons.Filled.WaterDrop
        tc.contains("VMC") -> Icons.Filled.Air
        tc.contains("BRULEUR") -> Icons.Filled.LocalFireDepartment
        tc.contains("POELE") -> Icons.Filled.Fireplace
        tc.contains("RADIATEUR") -> Icons.Filled.Thermostat
        else -> Icons.Filled.Build
    }
}
