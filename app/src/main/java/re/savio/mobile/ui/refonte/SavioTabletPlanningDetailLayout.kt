package re.savio.mobile.ui.refonte



import androidx.compose.foundation.background

import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Row

import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.height

import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.layout.width

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.PlayArrow

import androidx.compose.material3.Button

import androidx.compose.material3.ButtonDefaults

import androidx.compose.material3.Icon

import androidx.compose.material3.Text

import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.draw.shadow

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp

import re.savio.mobile.data.local.entity.InterventionEntity

import re.savio.mobile.ui.theme.SavioRefonte

import re.savio.mobile.util.UnitEnergySummaryItem

import re.savio.mobile.ui.theme.ViolettTokens



@Composable

fun SavioTabletPlanningDetailContent(

    intervention: InterventionEntity,

    energyBadges: List<UnitEnergySummaryItem>,

    onClientClick: (String) -> Unit,

    onCallClick: (String) -> Unit,

    onNavigateClick: () -> Unit,

    onPrimaryClick: () -> Unit,

    showPrimaryAction: Boolean = true,

    modifier: Modifier = Modifier,

) {

    val primaryLabel =

        if (intervention.status == "scheduled") {

            "Démarrer l'intervention"

        } else {

            "Reprendre l'intervention"

        }



    Column(

        modifier = modifier.fillMaxWidth(),

        verticalArrangement = Arrangement.spacedBy(14.dp),

    ) {

        SavioInterventionDetailContent(

            intervention = intervention,

            energyBadges = energyBadges,

            onClientClick = onClientClick,

            onNavigateClick = onNavigateClick,

            onCallClick = onCallClick,

        )



        if (showPrimaryAction) {

            Button(

                onClick = onPrimaryClick,

                modifier =

                    Modifier

                        .fillMaxWidth()

                        .height(52.dp)

                        .shadow(12.dp, RoundedCornerShape(15.dp), spotColor = ViolettTokens.FabShadow),

                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White),

                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),

                shape = RoundedCornerShape(15.dp),

            ) {

                Box(

                    modifier =

                        Modifier

                            .fillMaxWidth()

                            .height(52.dp)

                            .background(SavioRefonte.PrimaryGradient, RoundedCornerShape(15.dp)),

                    contentAlignment = Alignment.Center,

                ) {

                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Icon(

                            imageVector = Icons.Filled.PlayArrow,

                            contentDescription = null,

                            modifier = Modifier.height(18.dp),

                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(

                            text = primaryLabel,

                            fontSize = 15.sp,

                            fontWeight = FontWeight.Medium,

                        )

                    }

                }

            }

        }

    }

}

