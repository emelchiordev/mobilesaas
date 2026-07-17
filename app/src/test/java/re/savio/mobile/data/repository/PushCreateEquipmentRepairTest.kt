package re.savio.mobile.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import re.savio.mobile.data.local.entity.EquipmentEntity

class PushCreateEquipmentRepairTest {

    @Test
    fun shouldRepair_when_mobile_order_and_no_existing_ops() {
        assertTrue(
            PushCreateEquipmentRepair.shouldRepairCreateEquipment(
                equipmentOrder = 101,
                hasCreatePending = false,
                hasReplacePending = false,
                hasCreateOrReplaceAnyStatus = false,
            ),
        )
    }

    @Test
    fun shouldNotRepair_when_catalog_order_or_pending_exists() {
        assertFalse(
            PushCreateEquipmentRepair.shouldRepairCreateEquipment(
                equipmentOrder = 1,
                hasCreatePending = false,
                hasReplacePending = false,
                hasCreateOrReplaceAnyStatus = false,
            ),
        )
        assertFalse(
            PushCreateEquipmentRepair.shouldRepairCreateEquipment(
                equipmentOrder = 101,
                hasCreatePending = true,
                hasReplacePending = false,
                hasCreateOrReplaceAnyStatus = true,
            ),
        )
    }

    @Test
    fun shouldNotRepair_when_sent_create_exists() {
        assertFalse(
            PushCreateEquipmentRepair.shouldRepairCreateEquipment(
                equipmentOrder = 101,
                hasCreatePending = false,
                hasReplacePending = false,
                hasCreateOrReplaceAnyStatus = true,
            ),
        )
    }

    @Test
    fun shouldRequeueSent_when_intervention_awaiting_sync() {
        assertTrue(
            PushCreateEquipmentRepair.shouldRequeueSentEquipmentOp(
                opStatus = "sent",
                interventionAwaitingServerSync = true,
            ),
        )
        assertFalse(
            PushCreateEquipmentRepair.shouldRequeueSentEquipmentOp(
                opStatus = "sent",
                interventionAwaitingServerSync = false,
            ),
        )
        assertFalse(
            PushCreateEquipmentRepair.shouldRequeueSentEquipmentOp(
                opStatus = "pending",
                interventionAwaitingServerSync = true,
            ),
        )
    }

    @Test
    fun isPendingOpStatus_only_pending() {
        assertTrue(PushCreateEquipmentRepair.isPendingOpStatus("pending"))
        assertFalse(PushCreateEquipmentRepair.isPendingOpStatus("sent"))
    }

    @Test
    fun matchesEquipmentOrder_fromPayloadJson() {
        val payload =
            """{"interventionId":"int-1","order":101,"unitId":"unit-1"}"""
        assertTrue(PushCreateEquipmentRepair.matchesEquipmentOrder(payload, 101))
        assertFalse(PushCreateEquipmentRepair.matchesEquipmentOrder(payload, 102))
    }

    @Test
    fun buildPayload_includes_intervention_and_unit() {
        val equipment = EquipmentEntity(
            interventionId = "int-1",
            order = 101,
            id = "eq-local",
            unitId = "unit-1",
            model = "Model X",
            brand = "Brand",
            typeCode = "PAC",
            energyCode = "ELEC",
        )
        val payload = PushCreateEquipmentRepair.buildPayloadFromEntity(equipment)
        assertEquals("int-1", payload["interventionId"])
        assertEquals("unit-1", payload["unitId"])
        assertEquals(101, payload["order"])
        assertEquals("Model X", payload["model"])
    }
}
