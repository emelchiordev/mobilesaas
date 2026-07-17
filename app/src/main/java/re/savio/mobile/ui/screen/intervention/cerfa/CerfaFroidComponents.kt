package re.savio.mobile.ui.screen.intervention.cerfa

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import re.savio.mobile.ui.theme.SavioRefonte
import re.savio.mobile.ui.theme.ViolettTokens

private val CfBg = Color(0xFFEFF3F7)
private val CfLine = Color(0xFFE6EBF1)
private val CfMuted = Color(0xFF8893A2)
private val CfFieldBg = Color(0xFFF7F9FC)
private val CfTrackOff = Color(0xFFCBD3DE)
private val CfAmber = Color(0xFFE89A1E)
private val CfAmberDeep = Color(0xFFB5710A)
private val CfAmberBg = Color(0xFFFBEFD7)
private val CfOkFg = Color(0xFF1E7A4F)
private val CfOkBg = Color(0xFFE2F3E9)
private val CfMissSeg = Color(0xFFF0C6C0)
private val CfMissBorder = Color(0xFFD98A7E)
private val CfSegIdle = Color(0xFFD7DEE7)
private val CfTabIdle = Color(0xFF7B8595)
private val CfEmptyVal = Color(0xFFAEB7C4)

@Composable
fun CerfaFroidHeader(
    onBack: () -> Unit,
    onApercuPdf: () -> Unit,
    apercuEnabled: Boolean,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(SavioRefonte.Navy)
                .statusBarsPadding(),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp, end = 14.dp, top = 6.dp, bottom = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour",
                    tint = Color.White,
                )
            }
            Text(
                text = "CERFA 15497",
                modifier = Modifier.weight(1f),
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.2).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(11.dp))
                        .background(Color.White.copy(alpha = if (apercuEnabled) 0.14f else 0.06f))
                        .clickable(enabled = apercuEnabled, onClick = onApercuPdf)
                        .padding(horizontal = 13.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Description,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = if (apercuEnabled) 1f else 0.45f),
                    modifier = Modifier.size(17.dp),
                )
                Text(
                    text = "Aperçu PDF",
                    color = Color.White.copy(alpha = if (apercuEnabled) 1f else 0.45f),
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
fun CerfaFroidProgress(
    currentIndex: Int,
    tabStates: List<CerfaTabFillState>,
) {
    val missing = tabStates.count { it == CerfaTabFillState.Missing }
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(CfBg)
                .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Onglet ${currentIndex + 1} sur ${tabStates.size}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF5A6573),
                modifier = Modifier.semantics {
                    contentDescription = "Progression CERFA ${currentIndex + 1} sur ${tabStates.size}"
                },
            )
            when {
                missing > 0 -> {
                    Row(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(CfAmberBg)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = null,
                            tint = CfAmberDeep,
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            text =
                                if (missing > 1) {
                                    "$missing obligatoires manquants"
                                } else {
                                    "1 obligatoire manquant"
                                },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CfAmberDeep,
                        )
                    }
                }
                tabStates.all { it == CerfaTabFillState.Done } -> {
                    Row(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(CfOkBg)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = CfOkFg,
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            text = "Tout est complété",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CfOkFg,
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            tabStates.forEachIndexed { i, st ->
                val color =
                    when {
                        i == currentIndex -> CfAmber
                        st == CerfaTabFillState.Done -> SavioRefonte.Navy
                        st == CerfaTabFillState.Missing -> CfMissSeg
                        else -> CfSegIdle
                    }
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(color)
                            .then(
                                if (st == CerfaTabFillState.Missing && i != currentIndex) {
                                    Modifier.border(1.5.dp, CfMissBorder, RoundedCornerShape(3.dp))
                                } else {
                                    Modifier
                                },
                            ),
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CerfaFroidTabRow(
    selectedIndex: Int,
    tabStates: List<CerfaTabFillState>,
    onSelect: (Int) -> Unit,
) {
    val scrollState = rememberScrollState()
    val bringIntoViewRequesters =
        remember(CERFA_FROID_TABS.size) {
            List(CERFA_FROID_TABS.size) { BringIntoViewRequester() }
        }

    LaunchedEffect(selectedIndex) {
        delay(16)
        bringIntoViewRequesters.getOrNull(selectedIndex)?.bringIntoView()
    }

    Box(modifier = Modifier.fillMaxWidth().background(Color.White)) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            CERFA_FROID_TABS.forEachIndexed { index, tab ->
                val on = index == selectedIndex
                val st = tabStates.getOrElse(index) { CerfaTabFillState.Todo }
                Column(
                    modifier =
                        Modifier
                            .bringIntoViewRequester(bringIntoViewRequesters[index])
                            .clickable { onSelect(index) }
                            .padding(horizontal = 12.dp, vertical = 11.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        when (st) {
                            CerfaTabFillState.Done ->
                                Box(
                                    modifier =
                                        Modifier
                                            .size(15.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2E9E6B)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(11.dp),
                                    )
                                }
                            CerfaTabFillState.Missing ->
                                Box(
                                    modifier =
                                        Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(CfAmber)
                                            .border(3.dp, CfAmberBg, CircleShape),
                                )
                            CerfaTabFillState.Todo -> Unit
                        }
                        Text(
                            text = tab.title,
                            fontSize = 15.sp,
                            fontWeight = if (on) FontWeight.ExtraBold else FontWeight.SemiBold,
                            color = if (on) SavioRefonte.Navy else CfTabIdle,
                            maxLines = 1,
                        )
                    }
                    Text(
                        text = "CERFA ${tab.cerfaRef}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (on) Color(0xFF7FA0C4) else Color(0xFFB3BDCA),
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.6.sp,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                    if (on) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier =
                                Modifier
                                    .width(48.dp)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                    .background(SavioRefonte.Navy),
                        )
                    } else {
                        Spacer(modifier = Modifier.height(11.dp))
                    }
                }
            }
        }
        // Indices de scroll : dégradés latéraux si d'autres onglets sont hors écran
        if (scrollState.canScrollBackward) {
            Box(
                modifier =
                    Modifier
                        .align(Alignment.CenterStart)
                        .width(28.dp)
                        .height(56.dp)
                        .background(
                            brush =
                                Brush.horizontalGradient(
                                    listOf(Color.White, Color.White.copy(alpha = 0f)),
                                ),
                        ),
            )
        }
        if (scrollState.canScrollForward) {
            Box(
                modifier =
                    Modifier
                        .align(Alignment.CenterEnd)
                        .width(28.dp)
                        .height(56.dp)
                        .background(
                            brush =
                                Brush.horizontalGradient(
                                    listOf(Color.White.copy(alpha = 0f), Color.White),
                                ),
                        ),
            )
        }
    }
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(CfLine),
    )
}

@Composable
fun CerfaOuiNonToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier =
            Modifier
                .wrapContentWidth()
                .widthIn(min = 88.dp)
                .clickable { onCheckedChange(!checked) },
    ) {
        Text(
            text = if (checked) "Oui" else "Non",
            fontSize = 13.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (checked) SavioRefonte.Navy else CfEmptyVal,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.widthIn(min = 32.dp),
        )
        Box(
            modifier =
                Modifier
                    .width(52.dp)
                    .height(31.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (checked) SavioRefonte.Navy else CfTrackOff),
        ) {
            Box(
                modifier =
                    Modifier
                        .padding(3.dp)
                        .align(if (checked) Alignment.CenterEnd else Alignment.CenterStart)
                        .size(25.dp)
                        .shadow(2.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White),
            )
        }
    }
}

@Composable
fun CerfaSectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x0A142846), spotColor = Color(0x0A142846))
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, CfLine, RoundedCornerShape(16.dp)),
        content = content,
    )
}

@Composable
fun CerfaCardHead(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    warnTint: Boolean = false,
    checked: Boolean? = null,
    onCheckedChange: ((Boolean) -> Unit)? = null,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (warnTint) Color(0xFFFDF4E6) else SavioRefonte.Tint),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (warnTint) CfAmberDeep else SavioRefonte.Navy,
                modifier = Modifier.size(19.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.5.sp,
                fontWeight = FontWeight.Bold,
                color = SavioRefonte.Ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.5.sp,
                    color = CfMuted,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (checked != null && onCheckedChange != null) {
            CerfaOuiNonToggle(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
fun CerfaInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    readOnly: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (readOnly) ViolettTokens.Faint else CfFieldBg)
                .border(1.dp, CfLine, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 11.dp)
                .semantics { contentDescription = "Champ CERFA $label" },
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label.uppercase(),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = CfMuted,
            letterSpacing = 0.4.sp,
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = readOnly,
            enabled = !readOnly,
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle =
                TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (value.isBlank() && !readOnly) CfEmptyVal else SavioRefonte.Ink,
                    lineHeight = 20.sp,
                ),
            cursorBrush = SolidColor(SavioRefonte.Navy),
            decorationBox = { inner ->
                Box {
                    if (value.isBlank()) {
                        Text(
                            text = if (readOnly) "—" else "À renseigner",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = CfEmptyVal,
                        )
                    }
                    inner()
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun CerfaAutoField(
    label: String,
    value: String,
) {
    CerfaInputField(
        label = label,
        value = value,
        onValueChange = {},
        readOnly = true,
        singleLine = true,
    )
}

@Composable
fun CerfaFuiteEntry(
    index: Int,
    localisation: String,
    reparation: String,
    onLocChange: (String) -> Unit,
    onRepChange: (String) -> Unit,
    onDelete: (() -> Unit)?,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .border(1.dp, CfLine, RoundedCornerShape(14.dp))
                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 11.dp, start = 3.dp, end = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Fuite $index",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SavioRefonte.Navy,
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(SavioRefonte.Tint)
                        .padding(horizontal = 11.dp, vertical = 4.dp),
            )
            if (onDelete != null) {
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Supprimer fuite $index",
                        tint = Color(0xFFC2CBD6),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
        CerfaInputField("Localisation", localisation, onLocChange)
        CerfaInputField("Réparation effectuée", reparation, onRepChange)
    }
}

@Composable
fun CerfaAddDashedButton(
    text: String,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(13.dp))
                .border(1.5.dp, Color(0xFFC2CEDC), RoundedCornerShape(13.dp))
                .background(Color.White)
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Add, contentDescription = null, tint = SavioRefonte.Navy, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = SavioRefonte.Navy)
    }
}

@Composable
fun CerfaEmptyState(
    title: String,
    subtitle: String,
) {
    Row(
        modifier = Modifier.padding(horizontal = 2.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CfOkBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = CfOkFg, modifier = Modifier.size(20.dp))
        }
        Column {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SavioRefonte.Ink)
            Text(
                subtitle,
                fontSize = 12.5.sp,
                color = CfMuted,
                modifier = Modifier.padding(top = 2.dp),
                lineHeight = 17.sp,
            )
        }
    }
}

@Composable
fun CerfaRadioOption(
    title: String,
    subtitle: String?,
    selected: Boolean,
    recommended: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(
                    1.5.dp,
                    if (selected) SavioRefonte.Navy else CfLine,
                    RoundedCornerShape(14.dp),
                )
                .background(if (selected) SavioRefonte.Tint else Color.White)
                .clickable(onClick = onClick)
                .padding(horizontal = 15.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(23.dp)
                    .border(
                        2.dp,
                        if (selected) SavioRefonte.Navy else Color(0xFFC2CBD6),
                        CircleShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Box(
                    modifier =
                        Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(SavioRefonte.Navy),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.5.sp, fontWeight = FontWeight.Bold, color = SavioRefonte.Ink)
            if (subtitle != null) {
                Text(subtitle, fontSize = 12.sp, color = CfMuted, modifier = Modifier.padding(top = 2.dp))
            }
        }
        if (recommended) {
            Text(
                text = "Recommandé",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CfAmberDeep,
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(CfAmberBg)
                        .padding(horizontal = 9.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
fun CerfaToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    showDivider: Boolean = true,
) {
    Column {
        if (showDivider) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                        .height(1.dp)
                        .background(Color(0xFFEDF1F6)),
            )
        }
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = SavioRefonte.Ink,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            CerfaOuiNonToggle(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
fun CerfaGroupLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 11.5.sp,
        fontWeight = FontWeight.ExtraBold,
        color = CfMuted,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 4.dp),
    )
}

@Composable
fun CerfaBottomNav(
    onPrevious: (() -> Unit)?,
    nextLabel: String,
    onNext: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(CfBg)
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        if (onPrevious != null) {
            Row(
                modifier =
                    Modifier
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, CfLine, RoundedCornerShape(14.dp))
                        .clickable(onClick = onPrevious)
                        .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = SavioRefonte.Navy,
                    modifier = Modifier.size(19.dp),
                )
                Text("Précédent", fontSize = 15.5.sp, fontWeight = FontWeight.Bold, color = SavioRefonte.Navy)
            }
        }
        Row(
            modifier =
                Modifier
                    .weight(1f)
                    .height(52.dp)
                    .shadow(8.dp, RoundedCornerShape(14.dp), ambientColor = Color(0x3D1B4E80), spotColor = Color(0x3D1B4E80))
                    .clip(RoundedCornerShape(14.dp))
                    .background(SavioRefonte.Navy)
                    .clickable(onClick = onNext),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(nextLabel, fontSize = 15.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.width(7.dp))
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(19.dp),
            )
        }
    }
}

val CerfaPageBg: Color get() = CfBg
