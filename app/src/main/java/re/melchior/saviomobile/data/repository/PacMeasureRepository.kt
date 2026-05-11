package re.melchior.saviomobile.data.repository

import re.melchior.saviomobile.data.local.dao.PacMeasureDao
import re.melchior.saviomobile.data.local.entity.PacMeasureEntity
import re.melchior.saviomobile.data.remote.dto.PacMeasurePullDto
import java.time.Instant
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PacMeasureRepository @Inject constructor(
    private val dao: PacMeasureDao,
) {

    suspend fun getByInterventionAndOrder(
        interventionId: String,
        equipmentOrder: Int,
    ): PacMeasureEntity? = dao.getByInterventionAndOrder(interventionId, equipmentOrder)

    suspend fun upsert(entity: PacMeasureEntity) {
        dao.upsert(
            entity.copy(
                isDirty = true,
                updatedAt = Instant.now().toString(),
            ),
        )
    }

    suspend fun getDirty(): List<PacMeasureEntity> = dao.getDirty()

    suspend fun markClean(interventionId: String, equipmentOrder: Int) {
        dao.markClean(interventionId, equipmentOrder)
    }

    suspend fun deleteByInterventionId(interventionId: String) {
        dao.deleteByInterventionId(interventionId)
    }

    suspend fun mergeFromPull(rows: List<PacMeasurePullDto>, interventionId: String) {
        for (pm in rows) {
            val iid = pm.interventionId.ifBlank { interventionId }
            if (iid != interventionId) continue
            val local = dao.getByInterventionAndOrder(iid, pm.equipmentOrder)
            if (local?.isDirty == true) continue
            dao.upsert(pm.toEntity(iid))
        }
    }

    private fun PacMeasurePullDto.toEntity(interventionId: String): PacMeasureEntity {
        return PacMeasureEntity(
            interventionId = interventionId,
            equipmentOrder = equipmentOrder,
            pacVentilation = pacVentilation.orEmpty(),
            pacNetail = pacNetail.orEmpty(),
            pacVerail = pacVerail.orEmpty(),
            pacFiltre = pacFiltre.orEmpty(),
            pacFuite = pacFuite.orEmpty(),
            pacEvac = pacEvac.orEmpty(),
            pacPression1 = pacPression1.orEmpty(),
            pacPression2 = numToStr(pacPression2),
            pacGlycol1 = pacGlycol1.orEmpty(),
            pacGlycol2 = numToStr(pacGlycol2),
            pacTenStat = numToStr(pacTenStat),
            pacTenDyna = numToStr(pacTenDyna),
            pacIntensite = numToStr(pacIntensite),
            pacResserage1 = pacResserage1.orEmpty(),
            pacResserage2 = pacResserage2.orEmpty(),
            pacInterieure = numToStr(pacInterieure),
            pacExterieure = numToStr(pacExterieure),
            pacDepart = numToStr(pacDepart),
            pacRetour = numToStr(pacRetour),
            pacDeltaT = numToStr(pacDeltaT),
            pacHiver = numToStr(pacHiver),
            pacAppoint = numToStr(pacAppoint),
            pacConfort = numToStr(pacConfort),
            pacNonChauf = numToStr(pacNonChauf),
            pacEcsConsigne = numToStr(pacEcsConsigne),
            pacEcs = numToStr(pacEcs),
            pacManometreBp = numToStr(pacManometreBp),
            pacManometreHp = numToStr(pacManometreHp),
            pacDegivrage = pacDegivrage.orEmpty(),
            pacInversion = pacInversion.orEmpty(),
            pacHFonct = intLikeToStr(pacHFonct),
            pacHComp1 = intLikeToStr(pacHComp1),
            pacHVenti = intLikeToStr(pacHVenti),
            pacNbDemarr = intLikeToStr(pacNbDemarr),
            pacHAppoint1 = intLikeToStr(pacHAppoint1),
            pacHAppoint2 = intLikeToStr(pacHAppoint2),
            pacAlarme1 = pacAlarme1.orEmpty(),
            pacAlarme2 = pacAlarme2.orEmpty(),
            pacBlocage1 = pacBlocage1.orEmpty(),
            pacBlocage2 = pacBlocage2.orEmpty(),
            pacReleve = numToStr(pacReleve),
            pacRem1 = pacRem1.orEmpty(),
            updatedAt = updatedAt.orEmpty(),
            isDirty = false,
        )
    }

    private fun numToStr(v: Double?): String {
        if (v == null) return ""
        if (v % 1.0 == 0.0) return v.toInt().toString()
        return String.format(Locale.US, "%.2f", v)
    }

    private fun intLikeToStr(v: Double?): String {
        if (v == null) return ""
        return v.toInt().toString()
    }
}
