package com.gasodoapp.gasodo.core.di

import com.gasodoapp.gasodo.core.database.repository.EventRepository
import com.gasodoapp.gasodo.core.database.repository.EventRepositoryImpl
import com.gasodoapp.gasodo.core.database.repository.InspectionRepository
import com.gasodoapp.gasodo.core.database.repository.InspectionRepositoryImpl
import com.gasodoapp.gasodo.core.database.repository.MaintenanceRepository
import com.gasodoapp.gasodo.core.database.repository.MaintenanceRepositoryImpl
import com.gasodoapp.gasodo.core.database.repository.RefuelRepository
import com.gasodoapp.gasodo.core.database.repository.RefuelRepositoryImpl
import com.gasodoapp.gasodo.core.database.repository.SavedLocationRepository
import com.gasodoapp.gasodo.core.database.repository.SavedLocationRepositoryImpl
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

    @Binds
    @Singleton
    abstract fun bindEventRepository(
        impl: EventRepositoryImpl
    ): EventRepository
}