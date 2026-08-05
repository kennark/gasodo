package com.example.carrefueltracker.core.di

import com.example.carrefueltracker.core.database.repository.InspectionRepository
import com.example.carrefueltracker.core.database.repository.InspectionRepositoryImpl
import com.example.carrefueltracker.core.database.repository.MaintenanceRepository
import com.example.carrefueltracker.core.database.repository.MaintenanceRepositoryImpl
import com.example.carrefueltracker.core.database.repository.RefuelRepository
import com.example.carrefueltracker.core.database.repository.RefuelRepositoryImpl
import com.example.carrefueltracker.core.database.repository.SavedLocationRepository
import com.example.carrefueltracker.core.database.repository.SavedLocationRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindInspectionRepository(
        impl: InspectionRepositoryImpl
    ): InspectionRepository

    @Binds
    @Singleton
    abstract fun bindMaintenanceRepository(
        impl: MaintenanceRepositoryImpl
    ): MaintenanceRepository

    @Binds
    @Singleton
    abstract fun bindRefuelRepository(
        impl: RefuelRepositoryImpl
    ): RefuelRepository

    @Binds
    @Singleton
    abstract fun bindSavedLocationRepository(
        impl: SavedLocationRepositoryImpl
    ): SavedLocationRepository
}