package re.melchior.saviomobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/** Même [SavioDarkColorScheme] que [SavioTheme] — évite une divergence visuelle sur les écrans auth. */
@Composable
fun SavioAuthTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SavioDarkColorScheme,
        typography = AppTypography,
        content = content,
    )
}
