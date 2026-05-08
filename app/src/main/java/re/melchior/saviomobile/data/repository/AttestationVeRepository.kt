package re.melchior.saviomobile.data.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import re.melchior.saviomobile.data.local.dao.AttestationVeDao
import re.melchior.saviomobile.data.local.dao.AttestationVePointControleDao
import re.melchior.saviomobile.data.local.entity.AttestationVeEntity
import re.melchior.saviomobile.data.local.entity.AttestationVePointControleEntity

@Singleton
class AttestationVeRepository @Inject constructor(
    private val attestationVeDao: AttestationVeDao,
    private val pointControleDao: AttestationVePointControleDao,
) {

suspend fun getOrCreate(
    interventionId: String,
    equipmentOrder: Int,
    type: String,
): AttestationVeEntity {
    android.util.Log.d("ATTEST_CREATE",
        "getOrCreate interventionId=$interventionId " +
        "equipmentOrder=$equipmentOrder type=$type"
    )
    
    val existing = attestationVeDao.getByInterventionEquipmentType(
        interventionId,
        equipmentOrder,
        type,
    )
    
    android.util.Log.d("ATTEST_CREATE",
        "existing=${existing?.interventionId} " +
        "existingOrder=${existing?.equipmentOrder}"
    )
    
    return existing ?: run {
        val entity = AttestationVeEntity(
            interventionId = interventionId,
            equipmentOrder = equipmentOrder,
            id = java.util.UUID.randomUUID().toString(),
            type = type,
            updatedAt = java.time.Instant.now().toString(),
            isDirty = true,
        )
        android.util.Log.d("ATTEST_CREATE",
            "creating new entity interventionId=${entity.interventionId}"
        )
        attestationVeDao.insert(entity)
        entity
    }
}

    suspend fun save(entity: AttestationVeEntity) {
        val updated = entity.copy(
            isDirty = true,
            updatedAt = java.time.Instant.now().toString(),
        )
        attestationVeDao.insert(updated)
    }

    fun getFlow(
        interventionId: String,
        equipmentOrder: Int,
        type: String,
    ): Flow<AttestationVeEntity?> =
        attestationVeDao.getByIntervention(interventionId)
            .map { list ->
                list.find {
                    it.equipmentOrder == equipmentOrder && it.type == type
                }
            }

    suspend fun getDirty() = attestationVeDao.getDirty()

    suspend fun markClean(
        interventionId: String,
        equipmentOrder: Int,
        type: String,
    ) = attestationVeDao.markClean(
        interventionId,
        equipmentOrder,
        type,
    )

    fun getPointsFlow(
        interventionId: String,
        equipmentOrder: Int,
        type: String,
    ) = pointControleDao.getByAttestation(
        interventionId,
        equipmentOrder,
        type,
    )

    suspend fun savePoint(
        interventionId: String,
        equipmentOrder: Int,
        type: String,
        attestationId: String,
        cle: String,
        resultat: String,
    ) {
        if (resultat.isEmpty()) {
            pointControleDao.deleteByKey(
                interventionId,
                equipmentOrder,
                type,
                cle,
            )
        } else {
            pointControleDao.insert(
                AttestationVePointControleEntity(
                    id = "$attestationId-$cle",
                    attestationId = attestationId,
                    interventionId = interventionId,
                    equipmentOrder = equipmentOrder,
                    type = type,
                    cle = cle,
                    resultat = resultat,
                ),
            )
        }
    }
}
