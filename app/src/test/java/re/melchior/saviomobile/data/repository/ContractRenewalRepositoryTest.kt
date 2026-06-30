package re.melchior.saviomobile.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import re.melchior.saviomobile.data.remote.api.ContractsApi
import re.melchior.saviomobile.data.remote.dto.CreateRenewalInvoiceRequestDto
import re.melchior.saviomobile.data.remote.dto.CreateRenewalInvoiceResponseDto
import re.melchior.saviomobile.data.remote.dto.EquipmentContractStatusDto

class ContractRenewalRepositoryTest {

    private val contractsApi: ContractsApi = mock()
    private val repository = ContractRenewalRepository(contractsApi)

    @Test
    fun getRenewalEligibility_returnsNullWhenNotRenewable() = runTest {
        whenever(contractsApi.getEquipmentContractStatus("eq-1")).thenReturn(
            EquipmentContractStatusDto(
                status = "active",
                contractLineId = "line-1",
                renewable = false,
            ),
        )

        assertNull(repository.getRenewalEligibility("eq-1"))
    }

    @Test
    fun getRenewalEligibility_returnsEligibilityWhenRenewable() = runTest {
        whenever(contractsApi.getEquipmentContractStatus("eq-1")).thenReturn(
            EquipmentContractStatusDto(
                status = "active",
                contractLineId = "line-1",
                billingMode = "on_visit",
                renewable = true,
                priceTtc = "149.00",
            ),
        )

        val eligibility = repository.getRenewalEligibility("eq-1")

        assertEquals(
            RenewalEligibility(
                contractLineId = "line-1",
                billingMode = "on_visit",
                priceTtc = "149.00",
                renewable = true,
            ),
            eligibility,
        )
    }

    @Test
    fun createRenewalInvoice_postsMobileBodyAndReturnsInvoiceId() = runTest {
        whenever(
            contractsApi.createRenewalInvoice(
                "line-1",
                CreateRenewalInvoiceRequestDto(interventionId = "int-1"),
            ),
        ).thenReturn(CreateRenewalInvoiceResponseDto(invoiceId = "inv-1"))

        val invoiceId = repository.createRenewalInvoice("line-1", "int-1")

        assertEquals("inv-1", invoiceId)
        verify(contractsApi).createRenewalInvoice(
            "line-1",
            CreateRenewalInvoiceRequestDto(interventionId = "int-1"),
        )
    }
}
