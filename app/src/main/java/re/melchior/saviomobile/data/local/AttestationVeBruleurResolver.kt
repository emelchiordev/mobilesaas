package re.melchior.saviomobile.data.local

import re.melchior.saviomobile.data.local.dao.CatalogEquipmentDao
import re.melchior.saviomobile.data.local.dao.EquipmentDao
import re.melchior.saviomobile.data.local.entity.AttestationVeEntity
import re.melchior.saviomobile.data.local.entity.EquipmentEntity

/** Données brûleur résolues pour affichage / export (hors entité attestation). */
data class AttestationVeBruleurLinked(
    val brand: String?,
    val model: String?,
    val serialNumber: String?,
    val installDate: String?,
    val powerKw: Double?,
)

object AttestationVeBruleurResolver {

    /**
     * Priorité :
     * 1. [AttestationVeEntity.bruleurEquipmentOrder] → ligne `equipments` (post-clôture)
     * 2. Colonnes snapshot sur l’attestation (pré-clôture)
     * 3. Parcours live des équipements (parent courant + enfant BRULEUR)
     */
    suspend fun findLinkedBruleur(
        attestation: AttestationVeEntity,
        equipmentDao: EquipmentDao,
        catalogEquipmentDao: CatalogEquipmentDao,
    ): AttestationVeBruleurLinked? {
        attestation.bruleurEquipmentOrder?.let { ord ->
            equipmentDao.getEquipmentByInterventionAndOrder(
                attestation.interventionId,
                ord,
            )?.let { return it.toLinked(catalogEquipmentDao) }
        }

        if (hasBruleurSnapshot(attestation)) {
            return AttestationVeBruleurLinked(
                brand = attestation.bruleurMarque,
                model = attestation.bruleurModele,
                serialNumber = attestation.bruleurSerialNumber,
                installDate = attestation.bruleurCommissioningDate,
                powerKw = attestation.bruleurPuissanceKw,
            )
        }

        val parent = equipmentDao.getEquipmentByInterventionAndOrder(
            attestation.interventionId,
            attestation.equipmentOrder,
        ) ?: return null

        val list = equipmentDao.getEquipmentsByInterventionOnce(attestation.interventionId)
        val bruleur = list.find { eq ->
            eq.parentEquipmentId != null &&
                eq.typeCode?.uppercase() == "BRULEUR" &&
                parent.id == eq.parentEquipmentId
        } ?: return null

        return bruleur.toLinked(catalogEquipmentDao)
    }

    private fun hasBruleurSnapshot(a: AttestationVeEntity): Boolean =
        a.bruleurMarque != null ||
            a.bruleurModele != null ||
            a.bruleurSerialNumber != null ||
            a.bruleurCommissioningDate != null ||
            a.bruleurPuissanceKw != null

    private suspend fun EquipmentEntity.toLinked(
        catalogEquipmentDao: CatalogEquipmentDao,
    ): AttestationVeBruleurLinked {
        val powerKw = equipmentCatalogId?.let { cid ->
            catalogEquipmentDao.getById(cid)?.powerKw
        }
        return AttestationVeBruleurLinked(
            brand = brand,
            model = model,
            serialNumber = serialNumber,
            installDate = installDate,
            powerKw = powerKw,
        )
    }
}
