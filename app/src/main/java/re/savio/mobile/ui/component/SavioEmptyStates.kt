package re.savio.mobile.ui.component

import androidx.compose.material3.MaterialTheme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import re.savio.mobile.ui.designsystem.AppButton
import re.savio.mobile.ui.designsystem.AppButtonStyle
import re.savio.mobile.ui.designsystem.AppEmptyState
import re.savio.mobile.ui.designsystem.OfflineBanner
import re.savio.mobile.ui.theme.SavioPalette
import re.savio.mobile.ui.theme.SavioDimens
import re.savio.mobile.ui.theme.SavioType

@Composable
fun SavioOfflineBanner(modifier: Modifier = Modifier) {
    OfflineBanner(visible = true, modifier = modifier)
}

@Composable
fun SavioOfflineBannerSurface(modifier: Modifier = Modifier) {
    OfflineBanner(visible = true, modifier = modifier.fillMaxWidth())
}

@Composable
fun SavioEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    primaryActionLabel: String? = null,
    onPrimaryAction: (() -> Unit)? = null,
    primaryEnabled: Boolean = true,
) {
    AppEmptyState(
        icon = icon,
        title = title,
        subtitle = subtitle,
        modifier = modifier,
        action =
            if (primaryActionLabel != null && onPrimaryAction != null) {
                {
                    AppButton(
                        text = primaryActionLabel,
                        onClick = onPrimaryAction,
                        enabled = primaryEnabled,
                        modifier = Modifier.fillMaxWidth(),
                        style = AppButtonStyle.Primary,
                    )
                }
            } else {
                null
            },
    )
}

@Composable
fun SavioPrimaryCtaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    AppButton(
        text = text,
        onClick = onClick,
        enabled = enabled,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = SavioDimens.SpaceSM),
        style = AppButtonStyle.Primary,
    )
}

@Composable
fun SavioNetworkErrorState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Impossible de charger les données",
    subtitle: String = "Vérifiez votre connexion et réessayez",
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(SavioDimens.SpaceXL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SavioDimens.SpaceMD),
    ) {
        Icon(
            imageVector = Icons.Outlined.CloudOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = SavioPalette.PrimaryLight,
        )
        Text(
            text = title,
            style = SavioType.H3,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = subtitle,
            style = SavioType.BodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        AppButton(
            text = "Réessayer",
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
            style = AppButtonStyle.Primary,
        )
    }
}
