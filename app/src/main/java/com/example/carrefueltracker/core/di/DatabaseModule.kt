package com.example.carrefueltracker.core.di

import android.content.Context
import androidx.room.Room
import com.example.carrefueltracker.core.database.AppDatabase
import com.example.carrefueltracker.core.database.dao.EventDao
import com.example.carrefueltracker.core.database.dao.InspectionEventDao
import com.example.carrefueltracker.core.database.dao.MaintenanceEventDao
import com.example.carrefueltracker.core.database.dao.RefuelEventDao
import com.example.carrefueltracker.core.database.dao.SavedLocationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "car_refuel_tracker.db"
        )
            // Drop all data on database updates
            .fallbackToDestructiveMigration(true)
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
}