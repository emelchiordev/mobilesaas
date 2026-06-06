package re.melchior.saviomobile.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import re.melchior.saviomobile.data.remote.api.CustomerApi
import re.melchior.saviomobile.data.remote.api.CustomerSearchApi
import re.melchior.saviomobile.data.remote.api.DocumentApi
import re.melchior.saviomobile.data.remote.api.EquipmentApi
import re.melchior.saviomobile.data.remote.api.InterventionApi
import re.melchior.saviomobile.data.remote.api.InterventionPdfApi
import re.melchior.saviomobile.data.remote.api.InvoiceApi
import re.melchior.saviomobile.data.remote.api.PushApi
import re.melchior.saviomobile.data.remote.api.RagApi
import re.melchior.saviomobile.data.remote.api.SyncApi
import re.melchior.saviomobile.data.remote.api.TenantArticleApi
import re.melchior.saviomobile.data.remote.api.TourneeApi
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
    fun provideTenantArticleApi(retrofit: Retrofit): TenantArticleApi =
        retrofit.create(TenantArticleApi::class.java)

    @Provides
    @Singleton
    fun provideRagApi(retrofit: Retrofit): RagApi =
        retrofit.create(RagApi::class.java)
}