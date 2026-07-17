package re.savio.mobile.data.repository

import re.savio.mobile.data.local.entity.EquipmentEntity

/**
 * Réparation de la file push : CREATE_EQUIPMENT manquant pour équipements créés localement.
 */
object PushCreateEquipmentRepair {

    const val MOBILE_EQUIPMENT_ORDER_MIN = 101
    const val PENDING_OP_STATUS = "pending"
    const val SENT_OP_STATUS = "sent"

    fun isPendingOpStatus(status: String): Boolean = status == PENDING_OP_STATUS

    /** CREATE/REPLACE marqué sent alors que l’intervention n’est pas encore SYNCED côté serveur. */
    fun shouldRequeueSentEquipmentOp(
        opStatus: String?,
        interventionAwaitingServerSync: Boolean,
    ): Boolean =
        interventionAwaitingServerSync && opStatus == SENT_OP_STATUS

    fun shouldRepairCreateEquipment(
        equipmentOrder: Int,
        hasCreatePending: Boolean,
        hasReplacePending: Boolean,
        hasCreateOrReplaceAnyStatus: Boolean,
    ): Boolean =
        equipmentOrder >= MOBILE_EQUIPMENT_ORDER_MIN
            && !hasCreatePending
            && !hasReplacePending
            && !hasCreateOrReplaceAnyStatus

    fun payloadOrder(payloadJson: String): Int? {
        return runCatching {
            val gson = com.google.gson.Gson()
            @Suppress("UNCHECKED_CAST")
            val map = gson.fromJson(payloadJson, Map::class.java) as? Map<String, Any?> ?: return null
            (map["order"] as? Number)?.toInt()
        }.getOrNull()
    }

    fun matchesEquipmentOrder(payloadJson: String, equipmentOrder: Int): Boolean =
        payloadOrder(payloadJson) == equipmentOrder

    fun buildPayloadFromEntity(equipment: EquipmentEntity): Map<String, Any?> = buildMap {
        put("interventionId", equipment.interventionId)
        put("unitId", equipment.unitId)
        put("order", equipment.order)
        put("model", equipment.model)
        put("brand", equipment.brand ?: "")
        put("typeCode", equipment.typeCode ?: "")
        put("energyCode", equipment.energyCode ?: "")
        put("isPrimary", equipment.isPrimary)
        put("equipmentCatalogId", equipment.equipmentCatalogId)
        put("catalogBrandId", equipment.catalogBrandId)
        put("parentEquipmentId", equipment.parentEquipmentId)
        equipment.serialNumber?.let { put("serialNumber", it) }
        equipment.powerKw?.let { put("powerKw", it) }
    }
}
