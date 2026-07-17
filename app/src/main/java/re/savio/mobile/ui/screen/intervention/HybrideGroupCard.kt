package re.savio.mobile.ui.screen.intervention

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import re.savio.mobile.data.local.entity.EquipmentEntity
import re.savio.mobile.ui.refonte.SavioEquipCard
import re.savio.mobile.ui.refonte.SavioEquipSubLabel
import re.savio.mobile.ui.refonte.SavioHybridNote
import re.savio.mobile.ui.refonte.SavioHybridSystemHeader
import re.savio.mobile.ui.theme.useSavioRefonteUi

data class HybrideGroup(
    val chaudiere: EquipmentEntity,
    val chaudiereChildren: List<EquipmentEntity>,
    val pac: EquipmentEntity,
    val pacChildren: List<EquipmentEntity>,
    val label: String,
)

fun computeHybrideGroups(
    equipmentRoots: List<EquipmentEntity>,
    allEquipments: List<EquipmentEntity>,
    childrenByParent: Map<String?, List<EquipmentEntity>>,
): List<HybrideGroup> =
    equipmentRoots
        .filter { eq ->
            (eq.typeCode ?: "").trim().uppercase() == "CHAUDIERE" &&
                !eq.hybridePacEquipmentId.isNullOrBlank()
        }
        .mapNotNull { chaud ->
            val pacId = chaud.hybridePacEquipmentId!!.trim()
            val pac = allEquipments.firstOrNull { it.id == pacId } ?: return@mapNotNull null
            val ec = chaud.energyCode?.uppercase().orEmpty()
            val label =
                if (ec.contains("FIOUL") || ec.contains("FUEL")) {
                    "PAC Hybride FIOUL"
                } else {
                    "PAC Hybride GAZ"
                }
            HybrideGroup(
                chaudiere = chaud,
                chaudiereChildren = childrenByParent[chaud.id].orEmpty(),
                pac = pac,
                pacChildren = childrenByParent[pac.id].orEmpty(),
                label = label,
            )
        }

fun hybrideEquipmentIds(groups: List<HybrideGroup>): Set<String> =
    groups.flatMap { listOf(it.chaudiere.id, it.pac.id) }.toSet()

@Composable
fun HybrideGroupCard(
    group: HybrideGroup,
    newEquipmentIds: Set<String>,
    interventionId: String,
    interactive: Boolean = true,
    onEquipementClick: (interventionId: String, equipmentId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val refonte = useSavioRefonteUi()
    if (refonte) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SavioHybridSystemHeader(
                title = "Système ${group.label}",
                badge = "Hybride",
            )
            SavioEquipSubLabel(text = "Chaudière")
            SavioEquipCard {
                EquipmentRowItem(
                    equipment = group.chaudiere,
                    isChild = false,
                    badge = badgeFor(group.chaudiere, newEquipmentIds),
                    roleLabel =
                        group.chaudiere.pacClimRoleLabel(
                            isChild = false,
                            hasChildren = group.chaudiereChildren.isNotEmpty(),
                        ),
                    interactive = interactive,
                    onClick = { onEquipementClick(interventionId, group.chaudiere.id) },
                )
                SavioHybridNote(
                    text = "Point de départ attestation hybride",
                    embeddedInCard = true,
                )
            }
            group.chaudiereChildren.forEach { child ->
                SavioEquipCard {
                    EquipmentRowItem(
                        equipment = child,
                        isChild = false,
                        badge = badgeFor(child, newEquipmentIds),
                        roleLabel =
                            child.pacClimRoleLabel(
                                isChild = true,
                                hasChildren = false,
                            ),
                        interactive = interactive,
                        onClick = { onEquipementClick(interventionId, child.id) },
                    )
                }
            }
            SavioEquipSubLabel(text = "Pompe à chaleur")
            SavioEquipCard {
                EquipmentRowItem(
                    equipment = group.pac,
                    isChild = false,
                    badge = badgeFor(group.pac, newEquipmentIds),
                    roleLabel =
                        group.pac.pacClimRoleLabel(
                            isChild = false,
                            hasChildren = group.pacChildren.isNotEmpty(),
                        ),
                    interactive = interactive,
                    onClick = { onEquipementClick(interventionId, group.pac.id) },
                )
            }
            group.pacChildren.forEach { child ->
                SavioEquipCard {
                    EquipmentRowItem(
                        equipment = child,
                        isChild = false,
                        badge = badgeFor(child, newEquipmentIds),
                        roleLabel =
                            child.pacClimRoleLabel(
                                isChild = true,
                                hasChildren = false,
                            ),
                        interactive = interactive,
                        onClick = { onEquipementClick(interventionId, child.id) },
                    )
                }
            }
        }
        return
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(
            width = 1.5.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
        ),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "Système ${group.label}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primary,
                ) {
                    Text(
                        text = "Hybride",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(top = 10.dp, bottom = 4.dp),
            ) {
                Text(
                    text = "CHAUDIÈRE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 6.dp),
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column {
                        EquipmentRowItem(
                            equipment = group.chaudiere,
                            isChild = false,
                            badge = badgeFor(group.chaudiere, newEquipmentIds),
                            roleLabel = group.chaudiere.pacClimRoleLabel(
                                isChild = false,
                                hasChildren = group.chaudiereChildren.isNotEmpty(),
                            ),
                            interactive = interactive,
                            onClick = { onEquipementClick(interventionId, group.chaudiere.id) },
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(start = 12.dp, top = 0.dp, bottom = 8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bolt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp),
                            )
                            Text(
                                text = "Point de départ attestation hybride",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
                group.chaudiereChildren.forEach { child ->
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    EquipmentRowItem(
                        equipment = child,
                        isChild = true,
                        badge = badgeFor(child, newEquipmentIds),
                        roleLabel = child.pacClimRoleLabel(
                            isChild = true,
                            hasChildren = false,
                        ),
                        interactive = interactive,
                        onClick = { onEquipementClick(interventionId, child.id) },
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 12.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(top = 10.dp, bottom = 12.dp),
            ) {
                Text(
                    text = "POMPE À CHALEUR",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 6.dp),
                )
                EquipmentRowItem(
                    equipment = group.pac,
                    isChild = false,
                    badge = badgeFor(group.pac, newEquipmentIds),
                    roleLabel = group.pac.pacClimRoleLabel(
                        isChild = false,
                        hasChildren = group.pacChildren.isNotEmpty(),
                    ),
                    interactive = interactive,
                    onClick = { onEquipementClick(interventionId, group.pac.id) },
                )
                group.pacChildren.forEach { child ->
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    EquipmentRowItem(
                        equipment = child,
                        isChild = true,
                        badge = badgeFor(child, newEquipmentIds),
                        roleLabel = child.pacClimRoleLabel(
                            isChild = true,
                            hasChildren = false,
                        ),
                        interactive = interactive,
                        onClick = { onEquipementClick(interventionId, child.id) },
                    )
                }
            }
        }
    }
}
