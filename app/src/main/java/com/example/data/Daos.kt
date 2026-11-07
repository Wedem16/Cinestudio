package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomPresetDao {
    @Query("SELECT * FROM custom_presets ORDER BY createdAt DESC")
    fun getAllPresets(): Flow<List<CustomPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: CustomPresetEntity): Long

    @Query("DELETE FROM custom_presets WHERE id = :id")
    suspend fun deletePresetById(id: Long)
}

@Dao
interface RecentProjectDao {
    @Query("SELECT * FROM recent_projects ORDER BY lastEditedAt DESC LIMIT 15")
    fun getRecentProjects(): Flow<List<RecentProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: RecentProjectEntity): Long

    @Query("DELETE FROM recent_projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)
}
