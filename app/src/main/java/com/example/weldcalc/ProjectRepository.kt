package com.example.weldcalc

import kotlinx.coroutines.flow.Flow

class ProjectRepository(private val projectDao: ProjectDao) {

    val allProjects: Flow<List<Project>> = projectDao.getAllProjects()

    suspend fun insert(project: Project): Long {
        return projectDao.insert(project)
    }

    suspend fun deleteById(id: Long) {
        projectDao.deleteById(id)
    }

    suspend fun deleteAll() {
        projectDao.deleteAll()
    }
}
