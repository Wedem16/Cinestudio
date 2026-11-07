package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_presets")
data class CustomPresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val baseFilterId: String,
    val filterIntensity: Float,
    val exposure: Float,
    val contrast: Float,
    val saturation: Float,
    val temperature: Float,
    val tint: Float,
    val highlights: Float,
    val shadows: Float,
    val sharpen: Float,
    val vignette: Float,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "recent_projects")
data class RecentProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val videoUri: String,
    val videoTitle: String,
    val durationMs: Long,
    val resolutionLabel: String,
    val selectedFilterId: String,
    val filterIntensity: Float,
    val exposure: Float,
    val contrast: Float,
    val saturation: Float,
    val temperature: Float,
    val tint: Float,
    val highlights: Float,
    val shadows: Float,
    val sharpen: Float,
    val vignette: Float,
    val aiSummary: String?,
    val lastEditedAt: Long = System.currentTimeMillis()
)
