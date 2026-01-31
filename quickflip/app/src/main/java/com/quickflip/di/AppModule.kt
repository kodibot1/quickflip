package com.quickflip.di

import android.app.Application
import com.quickflip.data.local.AppDatabase
import com.quickflip.data.local.ListingDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase {
        return AppDatabase.getInstance(app)
    }

    @Provides
    fun provideListingDao(db: AppDatabase): ListingDao {
        return db.listingDao()
    }
}
