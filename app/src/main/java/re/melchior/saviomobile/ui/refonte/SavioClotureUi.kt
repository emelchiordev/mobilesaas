package re.melchior.saviomobile.ui.refonte

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.melchior.saviomobile.ui.theme.SavioRefonte

private val FormLabelColor = SavioRefonte.Ink
private val FormHelpColor = SavioRefonte.Muted
private val ChipOffBg = Color.White
private val ChipOffFg = Color(0xFF46505F)
private val ChipOffBorder = SavioRefonte.Line
private val KvKeyColor = Color(0xFF6B7686)
private val KvDivider = Color(0xFFF0F3F7)
private val WarnColor = Color(0xFFC0392B)
private val MutedValue = Color(0xFF97A1AE)
private val InfoCardBg = Color(0xFFEAF1F8)
private val InfoCardBorder = Color(0xFFDBE5F0)
private val DashedBorder = Color(0xFFC2CEDC)
private val TextAreaPlaceholder = Color(0xFF9AA4B2)
private val TextAreaCount = Color(0xFFB3BDCA)

@Composable
fun SavioClotureHeader(
    title: String,
    subtitle: String,
    step: Int,
    totalSteps: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SavioNavyStatusBarEffect()
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(SavioRefonte.Navy),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp)
                    .padding(top = 6.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = title,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 21.sp,
                )
                Text(
                    text = subtitle,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.padding(top = 1.dp),
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Étape $step",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(
                    text = " / $totalSteps",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
fun SavioClotureProgressBar(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(SavioRefonte.BgPage)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .padding(bottom = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(totalSteps) { index ->
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (index < currentStep) {
                                SavioRefonte.OrangeAction
                            } else {
                                Color(0xFFD7DEE7)
                            },
                        ),
            )
        }
    }
}

@Composable
fun SavioClotureClientCard(
    clientName: String,
    address: String,
    typeLabel: String,
    modifier: Modifier = Modifier,
) {
    SavioRefonteCard(modifier = modifier) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = clientName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SavioRefonte.Ink,
                )
                Text(
                    text = address,
                    fontSize = 13.sp,
                    color = SavioRefonte.Muted,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Text(
                text = typeLabel,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = SavioRefonte.Navy,
                modifier =
                    Modifier
                        .background(SavioRefonte.Tint, RoundedCornerShape(999.dp))
                        .padding(horizontal = 11.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
fun SavioFormSection(
    title: String,
    help: String? = null,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (trailing != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = FormLabelColor,
                    modifier = Modifier.weight(1f),
                )
                trailing()
            }
        } else {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = FormLabelColor,
            )
        }
        help?.let {
            Text(
                text = it,
                fontSize = 12.5.sp,
                color = FormHelpColor,
                modifier = Modifier.padding(top = 3.dp, bottom = 11.dp),
            )
        } ?: Spacer(modifier = Modifier.height(11.dp))
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SavioTypeChipGrid(
    labels: List<String>,
    selectedLabels: Set<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        labels.forEach { label ->
            val selected = label in selectedLabels
            SavioTypeChip(
                label = label,
                selected = selected,
                onClick = { onToggle(label) },
            )
        }
    }
}

@Composable
fun SavioTypeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (selected) SavioRefonte.Navy else ChipOffBg)
                .border(
                    width = 1.dp,
                    color = if (selected) SavioRefonte.Navy else ChipOffBorder,
                    shape = RoundedCornerShape(12.dp),
                )
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(15.dp),
            )
        }
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else ChipOffFg,
        )
    }
}

@Composable
fun SavioClotureBlockCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    SavioRefonteCard(modifier = modifier) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp)) {
            Text(
                text = title.uppercase(),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = SavioRefonte.Muted,
                letterSpacing = 0.6.sp,
                modifier = Modifier.padding(bottom = 10.dp),
            )
            content()
        }
    }
}

@Composable
fun SavioClotureKvRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isWarning: Boolean = false,
    isMuted: Boolean = false,
    showDivider: Boolean = true,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = KvKeyColor,
                modifier = Modifier.padding(end = 12.dp),
            )
            Text(
                text = value,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold,
                color =
                    when {
                        isWarning -> WarnColor
                        isMuted -> MutedValue
                        else -> SavioRefonte.Ink
                    },
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.End,
            )
        }
        if (showDivider) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(KvDivider),
            )
        }
    }
}

@Composable
fun SavioClotureInfoCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = InfoCardBg,
        border = BorderStroke(1.dp, InfoCardBorder),
        shadowElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp)) {
            Text(
                text = title,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = SavioRefonte.Muted,
                letterSpacing = 0.6.sp,
                modifier = Modifier.padding(bottom = 6.dp),
            )
            content()
        }
    }
}

@Composable
fun SavioClotureWillRow(
    icon: ImageVector,
    headline: String,
    bullets: List<String> = emptyList(),
    modifier: Modifier = Modifier,
    isWarning: Boolean = false,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier =
                Modifier
                    .size(34.dp)
                    .background(Color.White, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isWarning) WarnColor else SavioRefonte.Navy,
                modifier = Modifier.size(19.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = headline,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isWarning) WarnColor else SavioRefonte.Ink,
                lineHeight = 20.sp,
            )
            bullets.forEach { bullet ->
                Text(
                    text = "• $bullet",
                    fontSize = 14.sp,
                    color = Color(0xFF46505F),
                    modifier = Modifier.padding(top = 3.dp, start = 4.dp),
                )
            }
        }
    }
}

@Composable
fun SavioClotureDashedAddButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.5.dp, DashedBorder, RoundedCornerShape(14.dp))
                .background(Color.White)
                .clickable(onClick = onClick)
                .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            tint = SavioRefonte.Navy,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = SavioRefonte.Navy,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
fun SavioClotureAiButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    loading: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .clip(RoundedCornerShape(999.dp))
                .border(1.dp, SavioRefonte.Line, RoundedCornerShape(999.dp))
                .background(Color.White)
                .then(if (enabled && !loading) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 13.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(17.dp),
                strokeWidth = 2.dp,
                color = SavioRefonte.OrangeAction,
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = SavioRefonte.OrangeAction,
                modifier = Modifier.size(17.dp),
            )
        }
        Text(
            text = text,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (enabled) SavioRefonte.Navy else SavioRefonte.Muted,
        )
    }
}

@Composable
fun SavioClotureReportField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, SavioRefonte.Line, RoundedCornerShape(14.dp))
                .background(Color.White)
                .padding(14.dp),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle =
                androidx.compose.ui.text.TextStyle(
                    fontSize = 14.5.sp,
                    color = SavioRefonte.Ink,
                    lineHeight = 22.sp,
                ),
            cursorBrush = SolidColor(SavioRefonte.Navy),
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 14.5.sp,
                            color = TextAreaPlaceholder,
                            lineHeight = 22.sp,
                            modifier = Modifier.fillMaxWidth(0.88f),
                        )
                    }
                    inner()
                }
            },
        )
        Text(
            text = "${value.length} caractère${if (value.length > 1) "s" else ""}",
            fontSize = 12.sp,
            color = TextAreaCount,
            modifier = Modifier.align(Alignment.BottomEnd),
        )
    }
}

@Composable
fun SavioClotureNoticeCard(
    text: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isError) Color(0xFFFFEBEE) else InfoCardBg)
                .border(
                    1.dp,
                    if (isError) Color(0xFFEF9A9A) else InfoCardBorder,
                    RoundedCornerShape(12.dp),
                )
                .padding(12.dp),
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = if (isError) Color(0xFFB71C1C) else Color(0xFF46505F),
            lineHeight = 19.sp,
        )
    }
}

@Composable
fun SavioClotureNextCtaBar(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    loading: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(SavioRefonte.BgPage)
                .padding(horizontal = 16.dp)
                .padding(top = 6.dp, bottom = 18.dp)
                .navigationBarsPadding(),
    ) {
        Button(
            onClick = onClick,
            enabled = enabled && !loading,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Color(0x421B4E80)),
            shape = RoundedCornerShape(16.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = SavioRefonte.Navy,
                    contentColor = Color.White,
                    disabledContainerColor = SavioRefonte.Navy.copy(alpha = 0.5f),
                ),
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 6.dp).size(20.dp),
                )
            }
        }
    }
}

fun parseClosureConsequence(text: String): Pair<String, List<String>> {
    val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
    if (lines.isEmpty()) return "" to emptyList()
    val head = lines.first().removeSuffix(":").trim()
    val bullets =
        lines
            .drop(1)
            .map { it.removePrefix("•").trim() }
            .filter { it.isNotEmpty() }
    return head to bullets
}
