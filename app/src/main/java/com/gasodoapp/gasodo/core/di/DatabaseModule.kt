package com.gasodoapp.gasodo.core.di

import android.content.Context
import androidx.room.Room
import com.gasodoapp.gasodo.core.database.AppDatabase
import com.gasodoapp.gasodo.core.database.dao.EventDao
import com.gasodoapp.gasodo.core.database.dao.InspectionEventDao
import com.gasodoapp.gasodo.core.database.dao.MaintenanceEventDao
import com.gasodoapp.gasodo.core.database.dao.MaintenanceServiceTypeDao
import com.gasodoapp.gasodo.core.database.dao.RefuelEventDao
import com.gasodoapp.gasodo.core.database.dao.SavedLocationDao
import com.gasodoapp.gasodo.core.database.seed.SeedCallbacks
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        provider: Provider<AppDatabase>,
        @ApplicationScope scope: CoroutineScope
    ): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "car_refuel_tracker.db"
        )
            // Drop all data on database updates
            .fallbackToDestructiveMigration(true)
            .addCallback(SeedCallbacks(provider, scope))
            .build()

    @Provides
    fun provideSavedLocationDao(database: AppDatabase): SavedLocationDao =
        database.savedLocationDao()

    @Provides
    fun provideRefuelEventDao(database: AppDatabase): RefuelEventDao =
        database.refuelEventDao()

    @Provides
    fun provideMaintenanceEventDao(database: AppDatabase): MaintenanceEventDao =
        database.maintenanceEventDao()

    @Provides
    fun provideInspectionEventDao(database: AppDatabase): InspectionEventDao =
        database.inspectionEventDao()

    @Provides
    fun provideEventDao(database: AppDatabase): EventDao =
        database.eventDao()

    @Provides
    fun provideMaintenanceServiceTypeDao(database: AppDatabase): MaintenanceServiceTypeDao =
        database.maintenanceServiceTypeDao()
}