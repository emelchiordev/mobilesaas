package re.melchior.saviomobile.ui.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import re.melchior.saviomobile.ui.theme.SavioPalette
import re.melchior.saviomobile.ui.theme.SavioDimens
import re.melchior.saviomobile.ui.theme.SavioType

enum class AppButtonStyle {
    Primary,
    OutlineWhite,
    OutlinePrimary,
    OutlineError,
}

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: AppButtonStyle = AppButtonStyle.Primary,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(SavioDimens.RadiusPill)

    @Composable
    fun LabelRow() {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(SavioDimens.SpaceSM))
            }
            Text(text = text, style = SavioType.BodyLarge)
        }
    }

    when (style) {
        AppButtonStyle.Primary -> {
            val bg =
                when {
                    !enabled -> SavioPalette.TextHint.copy(alpha = 0.45f)
                    pressed -> SavioPalette.PrimaryDark
                    else -> SavioPalette.Primary
                }
            Button(
                onClick = onClick,
                enabled = enabled,
                modifier =
                    modifier
                        .heightIn(min = SavioDimens.AppButtonHeight)
                        .padding(horizontal = 20.dp),
                shape = shape,
                interactionSource = interactionSource,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = bg,
                        contentColor = SavioPalette.OnAccent,
                        disabledContentColor = SavioPalette.OnAccent.copy(alpha = 0.55f),
                    ),
            ) {
                LabelRow()
            }
        }
        AppButtonStyle.OutlineWhite ->
            OutlinedButton(
                onClick = onClick,
                enabled = enabled,
                modifier =
                    modifier
                        .heightIn(min = SavioDimens.AppButtonHeight)
                        .padding(horizontal = 20.dp),
                shape = shape,
                interactionSource = interactionSource,
                border =
                    BorderStroke(
                        SavioDimens.BorderMedium,
                        SavioPalette.Accent.copy(alpha = if (pressed && enabled) 0.85f else 1f),
                    ),
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        contentColor = SavioPalette.TextPrimary.copy(alpha = if (pressed && enabled) 0.85f else 1f),
                        containerColor = Color.Transparent,
                        disabledContentColor = SavioPalette.TextSecondary.copy(alpha = 0.45f),
                    ),
            ) {
                LabelRow()
            }
        AppButtonStyle.OutlinePrimary ->
            OutlinedButton(
                onClick = onClick,
                enabled = enabled,
                modifier =
                    modifier
                        .heightIn(min = SavioDimens.AppButtonHeight)
                        .padding(horizontal = 20.dp),
                shape = shape,
                interactionSource = interactionSource,
                border =
                    BorderStroke(
                        SavioDimens.BorderMedium,
                        SavioPalette.Accent.copy(alpha = if (pressed && enabled) 0.85f else 1f),
                    ),
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        contentColor = SavioPalette.TextPrimary.copy(alpha = if (pressed && enabled) 0.85f else 1f),
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                    ),
            ) {
                LabelRow()
            }
        AppButtonStyle.OutlineError ->
            OutlinedButton(
                onClick = onClick,
                enabled = enabled,
                modifier =
                    modifier
                        .heightIn(min = SavioDimens.AppButtonHeight)
                        .padding(horizontal = 20.dp),
                shape = shape,
                interactionSource = interactionSource,
                border =
                    BorderStroke(
                        SavioDimens.BorderMedium,
                        SavioPalette.Error.copy(alpha = if (pressed && enabled) 0.85f else 1f),
                    ),
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        contentColor = SavioPalette.Error.copy(alpha = if (pressed && enabled) 0.85f else 1f),
                        containerColor = Color.Transparent,
                    ),
            ) {
                LabelRow()
            }
    }
}
