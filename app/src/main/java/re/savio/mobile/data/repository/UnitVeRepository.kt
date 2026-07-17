package re.savio.mobile.data.repository



import re.savio.mobile.data.local.dao.InterventionDao

import re.savio.mobile.data.local.dao.InterventionHistoryDao

import re.savio.mobile.data.local.entity.InterventionEntity

import re.savio.mobile.data.remote.api.UnitsApi

import re.savio.mobile.ui.screen.intervention.cloture.UnitVeContext

import re.savio.mobile.ui.screen.intervention.cloture.buildUnitVeContextFromLocal

import re.savio.mobile.ui.screen.intervention.cloture.mergeUnitVeContextWithApi

import javax.inject.Inject

import javax.inject.Singleton



@Singleton

class UnitVeRepository @Inject constructor(

    private val interventionDao: InterventionDao,

    private val interventionHistoryDao: InterventionHistoryDao,

    private val unitsApi: UnitsApi,

) {

    suspend fun getVeContextForUnit(

        unitId: String,

        interventionHint: InterventionEntity? = null,

        fetchRemoteIfEmpty: Boolean = true,

    ): UnitVeContext {

        if (unitId.isBlank()) return UnitVeContext(null, null, null)



        val contractSource =

            interventionHint?.takeIf { hasContractSnapshot(it) }

                ?: interventionDao.getLatestContractSnapshotForUnit(unitId)

        val lastHistory = interventionHistoryDao.getLastCompletedVeForUnit(unitId)

        var context = buildUnitVeContextFromLocal(contractSource, lastHistory)



        if (fetchRemoteIfEmpty) {

            runCatching { unitsApi.getVeStatus(unitId) }

                .getOrNull()

                ?.let { api -> context = mergeUnitVeContextWithApi(context, api) }

        }



        return context

    }



    private fun hasContractSnapshot(intervention: InterventionEntity): Boolean {

        val type = intervention.contractType?.trim().orEmpty()

        val renewal = intervention.contractRenewalDate?.trim().orEmpty()

        return type.isNotEmpty() || renewal.isNotEmpty()

    }

}

