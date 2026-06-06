package re.melchior.saviomobile.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import re.melchior.saviomobile.data.local.dao.AnomalyDraftDao
import re.melchior.saviomobile.data.local.dao.AnomalyTypeDao
import re.melchior.saviomobile.data.local.dao.PendingOperationDao

class AnomalyDraftRepositoryTest {

    private val anomalyDraftDao: AnomalyDraftDao = mock()
    private val anomalyTypeDao: AnomalyTypeDao = mock()
    private val pendingOperationDao: PendingOperationDao = mock()

    private val repository = AnomalyDraftRepository(
        anomalyDraftDao = anomalyDraftDao,
        anomalyTypeDao = anomalyTypeDao,
        pendingOperationDao = pendingOperationDao,
    )

    @Test
    fun setCorrected_updatesDraftFlag() = runTest {
        repository.setCorrected("local-1", true)

        verify(anomalyDraftDao).updateCorrected("local-1", true)
    }

    @Test
    fun deleteDraft_removesPendingOpAndLocalRow() = runTest {
        repository.deleteDraft("local-1")

        verify(pendingOperationDao).deleteById("local-1")
        verify(anomalyDraftDao).deleteByLocalId("local-1")
    }
}
