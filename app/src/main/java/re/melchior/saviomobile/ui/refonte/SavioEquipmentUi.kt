package re.melchior.saviomobile.ui.refonte

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.melchior.saviomobile.ui.component.BrandLogo
import re.melchior.saviomobile.ui.theme.SavioRefonte
import re.melchior.saviomobile.ui.utils.equipmentIcon

private val ChevronColor = Color(0xFFC2CBD6)
private val BrandLogoRed = Color(0xFFD33B4A)
private val UnitBadgeBg = Color(0xFFE9F0F8)
private val UnitBadgeFg = Color(0xFF2C5A8C)
private val TodoColor = Color(0xFFAEB7C4)
private val WarnOutline = Color(0xFFE6CB9C)
private val WarnText = Color(0xFFB5710A)
private val DangerOutline = Color(0xFFDB93A6)
private val DangerText = Color(0xFFB02748)
private val InfoText = Color(0xFF46505F)

@Composable
fun SavioEquipListHeader(
    count: Int,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SavioInfoIconBox(icon = Icons.Filled.Build, size = 34.dp)
        Text(
            text = "Équipements",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = SavioRefonte.Ink,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = count.toString(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = SavioRefonte.Navy,
            modifier =
                Modifier
                    .background(SavioRefonte.Tint, RoundedCornerShape(999.dp))
                    .padding(horizontal = 11.dp, vertical = 2.dp),
        )
        IconButton(
            onClick = onAddClick,
            modifier =
                Modifier
                    .size(38.dp)
                    .background(Color.White, RoundedCornerShape(11.dp))
                    .border(1.dp, SavioRefonte.Line, RoundedCornerShape(11.dp)),
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Ajouter un équipement",
                tint = SavioRefonte.Navy,
            )
        }
    }
}

@Composable
fun SavioHybridSystemHeader(
    title: String,
    badge: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                imageVector = Icons.Filled.Bolt,
                contentDescription = null,
                tint = SavioRefonte.OrangeAction,
                modifier = Modifier.size(17.dp),
            )
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SavioRefonte.Navy,
            )
        }
        Text(
            text = badge,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier =
                Modifier
                    .background(SavioRefonte.Navy, RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp),
        )
    }
}

@Composable
fun SavioEquipSubLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(),
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Bold,
        color = SavioRefonte.Muted,
        letterSpacing = 0.6.sp,
        modifier = modifier.padding(start = 2.dp, top = 12.dp, bottom = 4.dp),
    )
}

@Composable
fun SavioHybridNote(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(start = 2.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Bolt,
            contentDescription = null,
            tint = SavioRefonte.OrangeAction,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = text,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = SavioRefonte.Navy,
        )
    }
}

@Composable
fun SavioEquipListItem(
    name: String,
    subtitle: String?,
    unitBadge: String?,
    catalogBrandId: String?,
    typeCode: String?,
    isChild: Boolean,
    isReplaced: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    interactive: Boolean = true,
    statusBadge: (@Composable () -> Unit)? = null,
) {
    val typeUc = typeCode?.uppercase().orEmpty()
    val redLogo = typeUc.contains("CHAUDIERE") || typeUc.contains("GAZ")
    val childBarColor = Color(0xFFE0E7F0)
    val density = LocalDensity.current
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .then(
                    if (interactive) Modifier.clickable(onClick = onClick) else Modifier,
                )
                .then(
                    if (isChild) {
                        Modifier
                            .padding(start = 14.dp)
                            .drawBehind {
                                val stroke = with(density) { 2.dp.toPx() }
                                drawLine(
                                    color = childBarColor,
                                    start = Offset(stroke / 2f, 0f),
                                    end = Offset(stroke / 2f, size.height),
                                    strokeWidth = stroke,
                                )
                            }
                            .padding(start = 14.dp, top = 10.dp, bottom = 10.dp)
                    } else {
                        Modifier.padding(vertical = 10.dp, horizontal = 2.dp)
                    },
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, SavioRefonte.Line, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            BrandLogo(
                brandId = catalogBrandId,
                modifier = Modifier.size(28.dp),
                fallback = {
                    Icon(
                        imageVector = equipmentIcon(typeCode),
                        contentDescription = null,
                        tint = if (redLogo) BrandLogoRed else SavioRefonte.Navy,
                        modifier = Modifier.size(20.dp),
                    )
                },
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = name,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = SavioRefonte.Ink,
                    textDecoration = if (isReplaced) TextDecoration.LineThrough else TextDecoration.None,
                )
                unitBadge?.let {
                    Text(
                        text = it,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = UnitBadgeFg,
                        modifier =
                            Modifier
                                .background(UnitBadgeBg, RoundedCornerShape(6.dp))
                                .padding(horizontal = 9.dp, vertical = 3.dp),
                    )
                }
            }
            subtitle?.let {
                Text(
                    text = it,
                    fontSize = 13.sp,
                    color = SavioRefonte.Muted,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            statusBadge?.invoke()
        }
        if (interactive) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = ChevronColor,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
fun SavioEquipBrandCard(
    eyebrow: String,
    title: String,
    tags: List<String>,
    catalogBrandId: String?,
    typeCode: String?,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    SavioRefonteCard(modifier = modifier) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(54.dp)
                        .background(SavioRefonte.Tint, RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center,
            ) {
                BrandLogo(
                    brandId = catalogBrandId,
                    modifier = Modifier.size(32.dp),
                    fallback = {
                        Icon(
                            imageVector = equipmentIcon(typeCode),
                            contentDescription = null,
                            tint = SavioRefonte.Navy,
                            modifier = Modifier.size(26.dp),
                        )
                    },
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = eyebrow,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SavioRefonte.Muted,
                )
                Text(
                    text = title,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = SavioRefonte.Ink,
                    letterSpacing = (-0.01).sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
                if (tags.isNotEmpty()) {
                    Row(
                        modifier = Modifier.padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        tags.forEach { tag ->
                            Text(
                                text = tag,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SavioRefonte.Navy,
                                modifier =
                                    Modifier
                                        .background(SavioRefonte.Tint, RoundedCornerShape(999.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                            )
                        }
                    }
                }
            }
            trailing?.invoke()
        }
    }
}

@Composable
fun SavioEquipSpecRow(
    icon: ImageVector,
    label: String,
    value: String,
    isTodo: Boolean,
    onActionClick: (() -> Unit)?,
    showDivider: Boolean,
) {
    if (showDivider) SavioRowLine()
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SavioInfoIconBox(icon = icon, size = 34.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Bold,
                color = SavioRefonte.Ink,
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontStyle = if (isTodo) FontStyle.Italic else FontStyle.Normal,
                color = if (isTodo) TodoColor else SavioRefonte.Muted,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        if (onActionClick != null) {
            IconButton(
                onClick = onActionClick,
                modifier =
                    Modifier
                        .size(40.dp)
                        .background(SavioRefonte.Tint, RoundedCornerShape(11.dp)),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = SavioRefonte.Navy,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
fun SavioEquipNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    showDivider: Boolean = true,
) {
    if (showDivider) SavioRowLine()
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 15.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SavioInfoIconBox(icon = icon, size = 34.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Bold,
                color = SavioRefonte.Ink,
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = SavioRefonte.Muted,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = ChevronColor,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
fun SavioEquipInfoBlock(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    SavioRefonteCard(modifier = modifier) {
        Row(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(34.dp)
                        .background(SavioRefonte.Tint, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = SavioRefonte.Navy,
                    modifier = Modifier.size(19.dp),
                )
            }
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SavioRefonte.Ink,
                )
                Text(
                    text = body,
                    fontSize = 14.sp,
                    color = InfoText,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
        }
    }
}

@Composable
fun SavioEquipActionButtons(
    onCorrect: () -> Unit,
    onReplace: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        OutlinedButton(
            onClick = onCorrect,
            modifier = Modifier.weight(1f).height(52.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.5.dp, WarnOutline),
        ) {
            Text(
                text = "Corriger",
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Bold,
                color = WarnText,
            )
        }
        if (onReplace != null) {
            OutlinedButton(
                onClick = onReplace,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, DangerOutline),
            ) {
                Text(
                    text = "Remplacer",
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = DangerText,
                )
            }
        }
    }
}

@Composable
fun SavioEquipStatusBadge(
    text: String,
    isNew: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (isNew) Color(0xFF27500A) else Color(0xFFA32D2D),
        modifier =
            modifier
                .padding(top = 4.dp)
                .background(
                    if (isNew) Color(0xFFEAF3DE) else Color(0xFFFFECEC),
                    RoundedCornerShape(6.dp),
                )
                .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}
