package re.savio.mobile.ui.refonte

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.savio.mobile.ui.theme.ViolettTokens
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class SavioHeaderStyle {
    /** Planning / Clients : titre centré en top bar + zone hero sous la barre. */
    Primary,
    /** Fiche : retour + titre centré. */
    Detail,
    /** Fiche intervention : en-tête dégradé bleu → cyan. */
    Intervention,
}

@Composable
fun SavioHeroTitle(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 6.dp),
    ) {
        Text(
            text = title,
            fontSize = 34.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = (-0.01).sp,
            color = ViolettTokens.Ink,
        )
        subtitle?.let {
            Text(
                text = it,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = ViolettTokens.Muted,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

/** Hero planning : « Jeudi » + « 4 juin » en semi-gras (maquette Violett). */
@Composable
fun SavioPlanningHeroDate(
    selectedDate: LocalDate,
    modifier: Modifier = Modifier,
) {
    val dayName =
        selectedDate.format(DateTimeFormatter.ofPattern("EEEE", Locale.FRENCH))
            .replaceFirstChar { it.uppercase() }
    val dayPart =
        selectedDate.format(DateTimeFormatter.ofPattern("d MMMM", Locale.FRENCH))
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 6.dp, bottom = 8.dp),
    ) {
        Text(
            text =
                buildAnnotatedString {
                    append("$dayName ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Medium)) {
                        append(dayPart)
                    }
                },
            fontSize = 34.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = (-0.01).sp,
            color = ViolettTokens.Ink,
            lineHeight = 36.sp,
        )
    }
}

@Composable
fun SavioNavyHeader(
    title: String,
    modifier: Modifier = Modifier,
    style: SavioHeaderStyle = SavioHeaderStyle.Detail,
    subtitle: String? = null,
    interventionHeadline: String? = null,
    heroContent: (@Composable () -> Unit)? = null,
    leading: @Composable (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    val headerBackground =
        when (style) {
            SavioHeaderStyle.Intervention -> SavioRefonteHeaderBrush
            else -> Brush.linearGradient(listOf(ViolettTokens.Bg, ViolettTokens.Bg))
        }
    val titleColor =
        when (style) {
            SavioHeaderStyle.Intervention -> Color.White
            else -> ViolettTokens.Ink
        }
    val subtitleColor =
        when (style) {
            SavioHeaderStyle.Intervention -> Color.White.copy(alpha = 0.82f)
            else -> ViolettTokens.Muted
        }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .then(
                    if (style == SavioHeaderStyle.Intervention) {
                        Modifier
                            .shadow(8.dp, RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp))
                            .clip(RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp))
                    } else {
                        Modifier
                    },
                )
                .background(headerBackground),
    ) {
        Column(modifier = Modifier.statusBarsPadding()) {
            when (style) {
                SavioHeaderStyle.Primary ->
                    Column {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                leading?.invoke()
                            }
                            Text(
                                text = title,
                                modifier = Modifier.weight(1.2f),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Medium,
                                color = ViolettTokens.Ink,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                            )
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterEnd,
                            ) {
                                actions?.invoke()
                            }
                        }
                        when {
                            heroContent != null -> heroContent()
                            else -> SavioHeroTitle(title = title, subtitle = subtitle)
                        }
                    }

                SavioHeaderStyle.Intervention ->
                    Column {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                                leading?.invoke()
                            }
                            Text(
                                text = title,
                                modifier = Modifier.weight(1.2f),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Medium,
                                color = titleColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                            )
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                                actions?.invoke()
                            }
                        }
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(start = 22.dp, end = 18.dp, top = 4.dp, bottom = 20.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = interventionHeadline ?: title,
                                    fontSize = 23.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = titleColor,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                subtitle?.let {
                                    Text(
                                        text = it,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = subtitleColor,
                                        modifier = Modifier.padding(top = 5.dp),
                                    )
                                }
                            }
                            trailing?.invoke()
                        }
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
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            color = titleColor,
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

private val SavioRefonteHeaderBrush: Brush
    get() = ViolettTokens.PrimaryGradient
