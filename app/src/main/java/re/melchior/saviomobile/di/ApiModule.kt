package re.melchior.saviomobile.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import re.melchior.saviomobile.data.remote.api.DocumentApi
import re.melchior.saviomobile.data.remote.api.InvoiceApi
import re.melchior.saviomobile.data.remote.api.PushApi
import re.melchior.saviomobile.data.remote.api.SyncApi
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
}