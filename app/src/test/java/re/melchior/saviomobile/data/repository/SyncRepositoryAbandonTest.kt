package re.melchior.saviomobile.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import re.melchior.saviomobile.data.local.dao.AnomalyDraftDao
import re.melchior.saviomobile.data.local.dao.AnomalyTypeDao
import re.melchior.saviomobile.data.local.dao.AttestationVeDao
import re.melchior.saviomobile.data.local.dao.AttestationVePointControleDao
import re.melchior.saviomobile.data.local.dao.CatalogEquipmentDao
import re.melchior.saviomobile.data.local.dao.ColdMeasureDao
import re.melchior.saviomobile.data.local.dao.EquipmentDao
import re.melchior.saviomobile.data.local.dao.EquipmentSnapshotDao
import re.melchior.saviomobile.data.local.dao.InstallationCheckDao
import re.melchior.saviomobile.data.local.dao.InterventionActualTypeDao
import re.melchior.saviomobile.data.local.dao.InterventionDao
import re.melchior.saviomobile.data.local.dao.InterventionHistoryDao
import re.melchior.saviomobile.data.local.dao.PendingOperationDao
import re.melchior.saviomobile.data.local.dao.ReferentielDao
import re.melchior.saviomobile.data.local.dao.SettingsDao
import re.melchior.saviomobile.data.remote.api.SyncApi

class SyncRepositoryAbandonTest {

    @Test
    fun abandonInterventionLocally_purge_toutes_les_saisies_session() = runTest {
        val interventionId = "int-1"
        val syncApi: SyncApi = mock()
        val interventionDao: InterventionDao = mock()
        val equipmentDao: EquipmentDao = mock()
        val catalogEquipmentDao: CatalogEquipmentDao = mock()
        val referentielDao: ReferentielDao = mock()
        val settingsDao: SettingsDao = mock()
        val interventionHistoryDao: InterventionHistoryDao = mock()
        val interventionActualTypeDao: InterventionActualTypeDao = mock()
        val pendingOperationDao: PendingOperationDao = mock()
        val coldMeasureDao: ColdMeasureDao = mock()
        val equipmentSnapshotDao: EquipmentSnapshotDao = mock()
        val anomalyTypeDao: AnomalyTypeDao = mock()
        val anomalyDraftDao: AnomalyDraftDao = mock()
        val installationCheckDao: InstallationCheckDao = mock()
        val attestationVeDao: AttestationVeDao = mock()
        val attestationVePointControleDao: AttestationVePointControleDao = mock()
        val measureRepository: MeasureRepository = mock()
        val pacMeasureRepository: PacMeasureRepository = mock()
        val installationCheckRepository: InstallationCheckRepository = mock()
        val photoRepository: PhotoRepository = mock()
        val invoiceRepository: InvoiceRepository = mock()
        val pushRepository: PushRepository = mock()

        whenever(interventionDao.getInterventionByIdOnce(interventionId)).thenReturn(null)
        whenever(equipmentSnapshotDao.getByInterventionId(interventionId)).thenReturn(emptyList())
        whenever(equipmentDao.getEquipmentsByInterventionOnce(interventionId)).thenReturn(emptyList())

        val repository =
            SyncRepository(
                syncApi = syncApi,
                interventionDao = interventionDao,
                equipmentDao = equipmentDao,
                catalogEquipmentDao = catalogEquipmentDao,
                referentielDao = referentielDao,
                settingsDao = settingsDao,
                interventionHistoryDao = interventionHistoryDao,
                interventionActualTypeDao = interventionActualTypeDao,
                pendingOperationDao = pendingOperationDao,
                coldMeasureDao = coldMeasureDao,
                equipmentSnapshotDao = equipmentSnapshotDao,
                anomalyTypeDao = anomalyTypeDao,
                anomalyDraftDao = anomalyDraftDao,
                installationCheckDao = installationCheckDao,
                attestationVeDao = attestationVeDao,
                attestationVePointControleDao = attestationVePointControleDao,
                measureRepository = measureRepository,
                pacMeasureRepository = pacMeasureRepository,
                installationCheckRepository = installationCheckRepository,
                photoRepository = photoRepository,
                invoiceRepository = invoiceRepository,
                pushRepository = pushRepository,
            )

        repository.abandonInterventionLocally(interventionId)

        verify(pendingOperationDao).deleteByInterventionId(interventionId)
        verify(anomalyDraftDao).deleteByInterventionId(interventionId)
        verify(installationCheckDao).deleteByInterventionId(interventionId)
        verify(attestationVePointControleDao).deleteByInterventionId(interventionId)
        verify(attestationVeDao).deleteByInterventionId(interventionId)
        verify(coldMeasureDao).deleteByInterventionId(interventionId)
        verify(measureRepository).deleteByInterventionId(interventionId)
        verify(pacMeasureRepository).deleteByInterventionId(interventionId)
        verify(photoRepository).deleteAllForIntervention(interventionId)
        verify(invoiceRepository).deleteDraftByIntervention(interventionId)
        verify(equipmentSnapshotDao).deleteByInterventionId(interventionId)
        verify(interventionActualTypeDao).deleteForIntervention(interventionId)
        verify(interventionDao).resetToScheduledAfterAbandon(interventionId)
    }
}
