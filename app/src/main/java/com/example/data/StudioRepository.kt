package com.example.data

import kotlinx.coroutines.flow.Flow

class StudioRepository(private val database: AppDatabase) {
    val presets: Flow<List<CustomPresetEntity>> = database.customPresetDao().getAllPresets()
    val recentProjects: Flow<List<RecentProjectEntity>> = database.recentProjectDao().getRecentProjects()

    suspend fun savePreset(preset: CustomPresetEntity): Long {
        return database.customPresetDao().insertPreset(preset)
    }

    suspend fun deletePreset(id: Long) {
        database.customPresetDao().deletePresetById(id)
    }

    suspend fun saveRecentProject(project: RecentProjectEntity): Long {
        return database.recentProjectDao().insertProject(project)
    }

    suspend fun deleteRecentProject(id: Long) {
        database.recentProjectDao().deleteProjectById(id)
    }
}
