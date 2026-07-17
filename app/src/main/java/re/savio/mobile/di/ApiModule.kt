package re.savio.mobile.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import re.savio.mobile.data.remote.api.ClientFinancialApi
import re.savio.mobile.data.remote.api.CustomerApi
import re.savio.mobile.data.remote.api.CustomerSearchApi
import re.savio.mobile.data.remote.api.DocumentApi
import re.savio.mobile.data.remote.api.EquipmentApi
import re.savio.mobile.data.remote.api.InterventionApi
import re.savio.mobile.data.remote.api.InterventionPdfApi
import re.savio.mobile.data.remote.api.InvoiceApi
import re.savio.mobile.data.remote.api.PushApi
import re.savio.mobile.data.remote.api.RagApi
import re.savio.mobile.data.remote.api.SyncApi
import re.savio.mobile.data.remote.api.CompanySettingsApi
import re.savio.mobile.data.remote.api.MeasurementDeviceApi
import re.savio.mobile.data.remote.api.RefrigerantContainerApi
import re.savio.mobile.data.remote.api.RefrigerantWastePartnerApi
import re.savio.mobile.data.remote.api.TenantArticleApi
import re.savio.mobile.data.remote.api.TourneeApi
import re.savio.mobile.data.remote.api.UnitsApi
import re.savio.mobile.data.remote.api.ContractsApi
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun provideSyncApi(retrofit: Retrofit): SyncApi =
        retrofit.create(SyncApi::class.java)

    @Provides
    @Singleton
    fun providePushApi(retrofit: Retrofit): PushApi =
        retrofit.create(PushApi::class.java)

    @Provides
    @Singleton
    fun provideDocumentApi(retrofit: Retrofit): DocumentApi =
        retrofit.create(DocumentApi::class.java)

    @Provides
    @Singleton
    fun provideTourneeApi(retrofit: Retrofit): TourneeApi =
        retrofit.create(TourneeApi::class.java)

    @Provides
    @Singleton
    fun provideInvoiceApi(retrofit: Retrofit): InvoiceApi =
        retrofit.create(InvoiceApi::class.java)

    @Provides
    @Singleton
    fun provideInterventionPdfApi(retrofit: Retrofit): InterventionPdfApi =
        retrofit.create(InterventionPdfApi::class.java)

    @Provides
    @Singleton
    fun provideCustomerSearchApi(retrofit: Retrofit): CustomerSearchApi =
        retrofit.create(CustomerSearchApi::class.java)

    @Provides
    @Singleton
    fun provideClientFinancialApi(retrofit: Retrofit): ClientFinancialApi =
        retrofit.create(ClientFinancialApi::class.java)

    @Provides
    @Singleton
    fun provideCustomerApi(retrofit: Retrofit): CustomerApi =
        retrofit.create(CustomerApi::class.java)

    @Provides
    @Singleton
    fun provideEquipmentApi(retrofit: Retrofit): EquipmentApi =
        retrofit.create(EquipmentApi::class.java)

    @Provides
    @Singleton
    fun provideInterventionApi(retrofit: Retrofit): InterventionApi =
        retrofit.create(InterventionApi::class.java)

    @Provides
    @Singleton
    fun provideCompanySettingsApi(retrofit: Retrofit): CompanySettingsApi =
        retrofit.create(CompanySettingsApi::class.java)

    @Provides
    @Singleton
    fun provideMeasurementDeviceApi(retrofit: Retrofit): MeasurementDeviceApi =
        retrofit.create(MeasurementDeviceApi::class.java)

    @Provides
    @Singleton
    fun provideRefrigerantContainerApi(retrofit: Retrofit): RefrigerantContainerApi =
        retrofit.create(RefrigerantContainerApi::class.java)

    @Provides
    @Singleton
    fun provideRefrigerantWastePartnerApi(retrofit: Retrofit): RefrigerantWastePartnerApi =
        retrofit.create(RefrigerantWastePartnerApi::class.java)

    @Provides
    @Singleton
    fun provideTenantArticleApi(retrofit: Retrofit): TenantArticleApi =
        retrofit.create(TenantArticleApi::class.java)

    @Provides
    @Singleton
    fun provideRagApi(retrofit: Retrofit): RagApi =
        retrofit.create(RagApi::class.java)

    @Provides
    @Singleton
    fun provideUnitsApi(retrofit: Retrofit): UnitsApi =
        retrofit.create(UnitsApi::class.java)

    @Provides
    @Singleton
    fun provideContractsApi(retrofit: Retrofit): ContractsApi =
        retrofit.create(ContractsApi::class.java)
}