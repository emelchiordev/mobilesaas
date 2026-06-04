package re.melchior.saviomobile.ui.component

import androidx.compose.material3.MaterialTheme

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import re.melchior.saviomobile.ui.theme.SavioPalette
import re.melchior.saviomobile.ui.theme.SavioDimens

@Composable
fun SavioSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier.padding(horizontal = SavioDimens.SpaceLG, vertical = SavioDimens.SpaceSM),
        snackbar = { data: SnackbarData ->
            val msg = data.visuals.message
            val isError =
                msg.startsWith("erreur", ignoreCase = true) ||
                    msg.startsWith("échec", ignoreCase = true)
            val isSuccess =
                !isError &&
                (
                msg.contains("enregistr", ignoreCase = true) ||
                    msg.contains("succès", ignoreCase = true) ||
                    msg.contains("créé", ignoreCase = true) ||
                    msg.contains("créée", ignoreCase = true) ||
                    msg.contains("clôtur", ignoreCase = true) ||
                    msg.contains("mise à jour", ignoreCase = true) ||
                    msg.contains("validé", ignoreCase = true) ||
                    msg.contains("signé", ignoreCase = true) ||
                    msg.contains("synchronisation", ignoreCase = true) ||
                    msg.contains("termin", ignoreCase = true) ||
                    msg.contains("soumise", ignoreCase = true)
                )
            val isWarning =
                !isError &&
                    !isSuccess &&
                    (
                        msg.contains("attente", ignoreCase = true) ||
                            msg.contains("validation", ignoreCase = true) ||
                            msg.contains("attention", ignoreCase = true)
                    )
            val containerColor =
                when {
                    isSuccess -> SavioPalette.SuccessDark
                    isWarning -> SavioPalette.SnackbarWarning
                    else -> SavioPalette.SnackbarError
                }
            val contentColor =
                when {
                    isSuccess -> SavioPalette.Success
                    else -> MaterialTheme.colorScheme.onSurface
                }
            Snackbar(
                snackbarData = data,
                shape = RoundedCornerShape(SavioDimens.RadiusMD),
                containerColor = containerColor,
                contentColor = contentColor,
                actionColor = SavioPalette.Accent,
                dismissActionContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            )
        },
    )
}
