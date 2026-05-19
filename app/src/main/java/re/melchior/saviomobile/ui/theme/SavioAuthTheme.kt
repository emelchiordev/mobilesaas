package re.melchior.saviomobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/** Schéma Field Pro sombre pour les écrans auth (indépendant du thème système). */
@Composable
fun SavioAuthTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SavioDarkColorScheme,
        typography = AppTypography,
        content = content,
    )
}
