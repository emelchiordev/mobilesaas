package re.melchior.saviomobile.ui.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import re.melchior.saviomobile.ui.theme.SavioPalette
import re.melchior.saviomobile.ui.theme.SavioDimens
import re.melchior.saviomobile.ui.theme.SavioType

/**
 * Header standard (fond page, texte blanc / secondaire gris, hauteur 64dp).
 */
@Composable
fun AppHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    badge: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .height(SavioDimens.HeaderHeight),
        color = SavioPalette.BackgroundPage,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = SavioDimens.SpaceLG),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SavioDimens.SpaceSM),
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = SavioPalette.TextPrimary,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = title,
                    style = SavioType.H1,
                    color = SavioPalette.TextPrimary,
                )
                subtitle?.let { s ->
                    Text(
                        text = s,
                        style = SavioType.BodySmall,
                        color = SavioPalette.TextSecondary,
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(SavioDimens.SpaceSM),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                badge?.invoke()
                actions()
            }
        }
    }
}
