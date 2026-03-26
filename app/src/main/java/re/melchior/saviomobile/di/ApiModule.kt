package re.melchior.saviomobile.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import re.melchior.saviomobile.data.remote.api.PushApi
import re.melchior.saviomobile.data.remote.api.SyncApi
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
}