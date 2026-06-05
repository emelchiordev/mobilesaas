package re.melchior.saviomobile.ui.refonte

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.melchior.saviomobile.ui.theme.SavioRefonte

enum class SavioHeaderStyle {
    /** Planning / Clients : titre gauche 28sp. */
    Primary,
    /** Fiche : retour + titre centré 19sp. */
    Detail,
    /** Fiche intervention : type + horaire + pill. */
    Intervention,
}

@Composable
fun SavioNavyHeader(
    title: String,
    modifier: Modifier = Modifier,
    style: SavioHeaderStyle = SavioHeaderStyle.Detail,
    subtitle: String? = null,
    leading: @Composable (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    SavioNavyStatusBarEffect()
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(SavioRefonte.Navy),
    ) {
        Column(modifier = Modifier.statusBarsPadding()) {
            when (style) {
                SavioHeaderStyle.Primary ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(start = 22.dp, end = 22.dp, top = 4.dp, bottom = 18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = title,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = (-0.01).sp,
                        )
                        Box(contentAlignment = Alignment.CenterEnd) {
                            actions?.invoke()
                        }
                    }

                SavioHeaderStyle.Intervention ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp)
                                .padding(top = 6.dp, bottom = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(modifier = Modifier.padding(end = 8.dp)) {
                            leading?.invoke()
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            subtitle?.let {
                                Text(
                                    text = it,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.78f),
                                    modifier = Modifier.padding(top = 3.dp),
                                )
                            }
                        }
                        trailing?.invoke()
                    }

                SavioHeaderStyle.Detail ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp)
                                .padding(top = 6.dp, bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(modifier = Modifier.padding(end = 8.dp)) {
                            leading?.invoke()
                        }
                        Text(
                            text = title,
                            modifier = Modifier.weight(1f),
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )
                        Box(
                            modifier = Modifier.padding(start = 8.dp),
                            contentAlignment = Alignment.CenterEnd,
                        ) {
                            actions?.invoke()
                        }
                    }
            }
        }
    }
}
