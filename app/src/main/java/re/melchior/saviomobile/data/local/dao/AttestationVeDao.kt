package re.melchior.saviomobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import re.melchior.saviomobile.data.local.entity.AttestationVeEntity

@Dao
interface AttestationVeDao {

    @Query(
        """
        SELECT * FROM attestation_ve
        WHERE interventionId = :interventionId
        AND equipmentOrder = :equipmentOrder
        AND type = :type
        LIMIT 1
        """,
    )
    suspend fun getByInterventionEquipmentType(
        interventionId: String,
        equipmentOrder: Int,
        type: String,
    ): AttestationVeEntity?

    @Query(
        """
        SELECT * FROM attestation_ve
        WHERE interventionId = :interventionId
        """,
    )
    fun getByIntervention(
        interventionId: String,
    ): Flow<List<AttestationVeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AttestationVeEntity)

    @Update
    suspend fun update(entity: AttestationVeEntity)

    @Query(
        """
        SELECT * FROM attestation_ve
        WHERE isDirty = 1
        """,
    )
    suspend fun getDirty(): List<AttestationVeEntity>

    @Query(
        """
        UPDATE attestation_ve
        SET isDirty = 0
        WHERE interventionId = :interventionId
        AND equipmentOrder = :equipmentOrder
        AND type = :type
        """,
    )
    suspend fun markClean(
        interventionId: String,
        equipmentOrder: Int,
        type: String,
    )

    @Query(
        """
        DELETE FROM attestation_ve
        WHERE interventionId = :interventionId
        AND equipmentOrder >= 101
        """,
    )
    suspend fun deleteLocalForIntervention(
        interventionId: String,
    )
}
