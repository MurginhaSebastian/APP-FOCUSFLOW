package com.example.focusflow.data.repository

import com.example.focusflow.data.local.RoutineDao
import com.example.focusflow.data.model.Routine
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineRepository @Inject constructor(
    private val routineDao: RoutineDao
) {
    fun getRoutinesByUser(userId: String): Flow<List<Routine>> {
        return routineDao.getRoutinesByUser(userId)
    }

    suspend fun getRoutineById(routineId: Int): Routine? {
        return routineDao.getRoutineById(routineId)
    }

    suspend fun createRoutine(routine: Routine): Long {
        return routineDao.insertRoutine(routine)
    }

    suspend fun updateRoutine(routine: Routine) {
        routineDao.updateRoutine(routine)
    }

    suspend fun deleteRoutine(routine: Routine) {
        routineDao.deleteRoutine(routine)
    }
}
