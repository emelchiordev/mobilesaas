package re.melchior.saviomobile.ui.screen.intervention.cloture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import re.melchior.saviomobile.data.local.entity.EquipmentEntity
import re.melchior.saviomobile.data.remote.dto.InterventionTypeDto

class InterventionTypeConsequencesTest {

    private val veType =
        InterventionTypeDto(
            id = "ve-id",
            code = "VE",
            label = "Visite d'entretien",
            color = null,
            isVeType = true,
        )

    private val depType =
        InterventionTypeDto(
            id = "02-id",
            code = "DEP",
            label = "Dépannage",
            color = null,
        )

    @Test
    fun isPlannedVeNotSelected_whenOtherTypeChecked() {
        assertTrue(isPlannedVeNotSelected(veType, listOf(depType)))
    }

    @Test
    fun isPlannedVeNotSelected_whenVeChecked() {
        assertFalse(isPlannedVeNotSelected(veType, listOf(veType)))
    }

    @Test
    fun blocksClosureForMissingCommissioning() {
        val mes =
            InterventionTypeDto(
                code = "MES",
                label = "Mise en service",
                color = null,
                triggerEquipmentSetup = true,
            )
        val eq =
            EquipmentEntity(
                interventionId = "int-1",
                order = 1,
                id = "eq-1",
                unitId = "unit-1",
                installDate = null,
            )
        assertTrue(blocksClosureForMissingCommissioning(listOf(mes), listOf(eq)))
    }

    @Test
    fun doesNotBlockWhenDatesPresent() {
        val mes =
            InterventionTypeDto(
                code = "MES",
                label = "Mise en service",
                color = null,
                triggerEquipmentSetup = true,
            )
        val eq =
            EquipmentEntity(
                interventionId = "int-1",
                order = 1,
                id = "eq-1",
                unitId = "unit-1",
                installDate = "2026-01-15",
            )
        assertFalse(blocksClosureForMissingCommissioning(listOf(mes), listOf(eq)))
    }
}
