package re.melchior.saviomobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Orange SAVIO — identique en light et dark. */
val SavioAccent: Color get() = SavioPalette.Accent

val SavioOnAccent: Color get() = SavioPalette.OnAccent

/** Light : barre bleue logo ; dark : fond page inchangé. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun savioTopAppBarColors(): TopAppBarColors {
    val scheme = MaterialTheme.colorScheme
    return if (isSystemInDarkTheme()) {
        TopAppBarDefaults.topAppBarColors(
            containerColor = scheme.background,
            titleContentColor = scheme.onBackground,
            navigationIconContentColor = scheme.onBackground,
            actionIconContentColor = scheme.onBackground,
        )
    } else {
        TopAppBarDefaults.topAppBarColors(
            containerColor = scheme.primary,
            titleContentColor = scheme.onPrimary,
            navigationIconContentColor = scheme.onPrimary,
            actionIconContentColor = scheme.onPrimary,
        )
    }
}

@Composable
fun savioTopAppBarContentColor(): Color =
    if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.onBackground
    } else {
        MaterialTheme.colorScheme.onPrimary
    }

@Composable
fun savioTopAppBarSubtitleColor(): Color =
    savioTopAppBarContentColor().copy(alpha = 0.75f)

@Composable
fun savioFieldColors(): TextFieldColors =
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedBorderColor = SavioAccent,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        cursorColor = SavioAccent,
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        errorBorderColor = MaterialTheme.colorScheme.error,
        errorLabelColor = MaterialTheme.colorScheme.error,
        errorSupportingTextColor = MaterialTheme.colorScheme.error,
    )
