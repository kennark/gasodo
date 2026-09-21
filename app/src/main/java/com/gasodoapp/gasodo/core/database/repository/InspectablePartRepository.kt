package com.gasodoapp.gasodo.core.database.repository

import com.gasodoapp.gasodo.core.database.entity.InspectablePart
import kotlinx.coroutines.flow.Flow

interface InspectablePartRepository {

    fun getAll(): Flow<List<InspectablePart>>
    suspend fun getById(id: Long): InspectablePart?
    suspend fun insert(entity: InspectablePart): Long
    suspend fun update(entity: InspectablePart)
    suspend fun delete(entity: InspectablePart)
}