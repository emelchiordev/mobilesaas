package re.melchior.saviomobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import re.melchior.saviomobile.data.local.entity.AttestationVePointControleEntity

@Dao
interface AttestationVePointControleDao {

    @Query(
        """
        SELECT * FROM attestation_ve_point_controle
        WHERE interventionId = :interventionId
        AND equipmentOrder = :equipmentOrder
        AND type = :type
        """,
    )
    suspend fun getByAttestationOnce(
        interventionId: String,
        equipmentOrder: Int,
        type: String,
    ): List<AttestationVePointControleEntity>

    @Query(
        """
        SELECT * FROM attestation_ve_point_controle
        WHERE interventionId = :interventionId
        AND equipmentOrder = :equipmentOrder
        AND type = :type
        """,
    )
    fun getByAttestation(
        interventionId: String,
        equipmentOrder: Int,
        type: String,
    ): Flow<List<AttestationVePointControleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(
        points: List<AttestationVePointControleEntity>,
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(
        point: AttestationVePointControleEntity,
    )

    @Query(
        """
        DELETE FROM attestation_ve_point_controle
        WHERE interventionId = :interventionId
        AND equipmentOrder = :equipmentOrder
        AND type = :type
        AND cle = :cle
        """,
    )
    suspend fun deleteByKey(
        interventionId: String,
        equipmentOrder: Int,
        type: String,
        cle: String,
    )

    @Query(
        """
        DELETE FROM attestation_ve_point_controle
        WHERE interventionId = :interventionId
        AND equipmentOrder >= 101
        """,
    )
    suspend fun deleteLocalForIntervention(
        interventionId: String,
    )

    @Query(
        "DELETE FROM attestation_ve_point_controle WHERE interventionId = :interventionId",
    )
    suspend fun deleteByInterventionId(interventionId: String)
}
