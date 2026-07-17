package re.savio.mobile.data.repository

import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import re.savio.mobile.data.local.dao.AnomalyDraftDao
import re.savio.mobile.data.local.dao.AnomalyTypeDao
import re.savio.mobile.data.local.dao.AttestationVeDao
import re.savio.mobile.data.local.dao.AttestationVePointControleDao
import re.savio.mobile.data.local.dao.CatalogEquipmentDao
import re.savio.mobile.data.local.dao.ColdMeasureDao
import re.savio.mobile.data.local.dao.EquipmentDao
import re.savio.mobile.data.local.dao.EquipmentSnapshotDao
import re.savio.mobile.data.local.dao.InstallationCheckDao
import re.savio.mobile.data.local.dao.InterventionActualTypeDao
import re.savio.mobile.data.local.dao.InterventionDao
import re.savio.mobile.data.local.dao.InterventionHistoryDao
import re.savio.mobile.data.local.dao.PendingOperationDao
import re.savio.mobile.data.local.dao.ReferentielDao
import re.savio.mobile.data.local.dao.SettingsDao
import re.savio.mobile.data.local.entity.InterventionEntity
import re.savio.mobile.data.remote.api.SyncApi

class SyncRepositoryStartInterventionTest {

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.w(any(), any<String>(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun startIntervention_marksInProgressBeforeReturning() = runTest {
        val interventionId = "int-1"
        val interventionDao: InterventionDao = mock()
        val equipmentSnapshotDao: EquipmentSnapshotDao = mock()
        val pendingOperationDao: PendingOperationDao = mock()
        val settingsDao: SettingsDao = mock()
        val equipmentDao: EquipmentDao = mock()

        whenever(pendingOperationDao.getPendingByInterventionIdOnce(interventionId)).thenReturn(emptyList())
        whenever(interventionDao.getInterventionByIdOnce(interventionId)).thenReturn(
            sampleIntervention(interventionId),
        )
        whenever(equipmentSnapshotDao.countByInterventionId(interventionId)).thenReturn(1)
        whenever(equipmentDao.getEquipmentsByInterventionOnce(interventionId)).thenReturn(emptyList())

        val repository = buildRepository(
            interventionDao = interventionDao,
            equipmentSnapshotDao = equipmentSnapshotDao,
            pendingOperationDao = pendingOperationDao,
            settingsDao = settingsDao,
            equipmentDao = equipmentDao,
        )

        repository.startIntervention(interventionId)

        verify(interventionDao).markAsInProgress(eq(interventionId), any())
    }

    private fun buildRepository(
        interventionDao: InterventionDao,
        equipmentSnapshotDao: EquipmentSnapshotDao,
        pendingOperationDao: PendingOperationDao,
        settingsDao: SettingsDao,
        equipmentDao: EquipmentDao,
    ): SyncRepository =
        SyncRepository(
            syncApi = mock(),
            interventionDao = interventionDao,
            equipmentDao = equipmentDao,
            catalogEquipmentDao = mock(),
            referentielDao = mock(),
            settingsDao = settingsDao,
            interventionHistoryDao = mock(),
            interventionActualTypeDao = mock(),
            pendingOperationDao = pendingOperationDao,
            coldMeasureDao = mock(),
            equipmentSnapshotDao = equipmentSnapshotDao,
            anomalyTypeDao = mock(),
            anomalyDraftDao = mock(),
            installationCheckDao = mock(),
            attestationVeDao = mock(),
            attestationVePointControleDao = mock(),
            measureRepository = mock(),
            pacMeasureRepository = mock(),
            installationCheckRepository = mock(),
            photoRepository = mock(),
            invoiceRepository = mock(),
            pushRepository = mock(),
            clientFinancialRepository = mock(),
        )

    private fun sampleIntervention(id: String) =
        InterventionEntity(
            id = id,
            scheduledAt = "2026-06-04T08:00:00Z",
            timeSlot = "matin",
            status = "scheduled",
            typeCode = "depannage",
            typeLabel = "Dépannage",
            typeColor = null,
            unitId = "unit-1",
            unitStreet = "1 rue Test",
            unitAddressLine2 = null,
            unitPostalCode = "75001",
            unitCity = "Paris",
            unitFloor = null,
            unitDoorCode = null,
            unitLatitude = null,
            unitLongitude = null,
            customerId = "cust-1",
            customerFirstName = "Jean",
            customerLastName = "Dupont",
            customerPhone = null,
            customerEmail = null,
            contractType = null,
            contractRenewalDate = null,
            contractTariff = null,
            contractVatRate = null,
            pulledAt = "2026-06-04T07:00:00Z",
        )
}
