package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.ai.GeminiColoristService
import com.example.data.AppDatabase
import com.example.data.CustomPresetEntity
import com.example.data.RecentProjectEntity
import com.example.data.StudioRepository
import com.example.media.Media3EffectFactory
import com.example.media.SampleVideoGenerator
import com.example.media.VideoExportManager
import com.example.media.VideoMetadataHelper
import com.example.model.AiSceneAnalysis
import com.example.model.CinemaFilter
import com.example.model.ColorGradeParams
import com.example.model.ExportFrameRate
import com.example.model.ExportResolution
import com.example.model.ExportState
import com.example.model.VideoMetadata
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "CineAiViewModel"

enum class EditorDeckTab {
    FILTERS,
    ADJUSTMENTS,
    PRESETS_HISTORY
}

sealed interface AiStatus {
    data object Idle : AiStatus
    data class Analyzing(val stepMessage: String) : AiStatus
    data class Success(val analysis: AiSceneAnalysis) : AiStatus
    data class Error(val message: String) : AiStatus
}

class CineAiViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StudioRepository(AppDatabase.getDatabase(application))
    private val geminiService = GeminiColoristService()
    private val exportManager = VideoExportManager(application)

    // ExoPlayer for hardware preview
    val exoPlayer: ExoPlayer = ExoPlayer.Builder(application).build().apply {
        repeatMode = Player.REPEAT_MODE_ALL
    }

    // State flows
    private val _videoMetadata = MutableStateFlow<VideoMetadata?>(null)
    val videoMetadata: StateFlow<VideoMetadata?> = _videoMetadata.asStateFlow()

    private val _timelineThumbnails = MutableStateFlow<List<Bitmap>>(emptyList())
    val timelineThumbnails: StateFlow<List<Bitmap>> = _timelineThumbnails.asStateFlow()

    private val _currentParams = MutableStateFlow(ColorGradeParams())
    val currentParams: StateFlow<ColorGradeParams> = _currentParams.asStateFlow()

    private val _isComparingOriginal = MutableStateFlow(false)
    val isComparingOriginal: StateFlow<Boolean> = _isComparingOriginal.asStateFlow()

    private val _selectedTab = MutableStateFlow<EditorDeckTab>(EditorDeckTab.FILTERS)
    val selectedTab: StateFlow<EditorDeckTab> = _selectedTab.asStateFlow()

    private val _aiStatus = MutableStateFlow<AiStatus>(AiStatus.Idle)
    val aiStatus: StateFlow<AiStatus> = _aiStatus.asStateFlow()

    private val _aiSummaryCardExpanded = MutableStateFlow(true)
    val aiSummaryCardExpanded: StateFlow<Boolean> = _aiSummaryCardExpanded.asStateFlow()

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    private val _showExportDialog = MutableStateFlow(false)
    val showExportDialog: StateFlow<Boolean> = _showExportDialog.asStateFlow()

    private val _showSavePresetDialog = MutableStateFlow(false)
    val showSavePresetDialog: StateFlow<Boolean> = _showSavePresetDialog.asStateFlow()

    // Undo / Redo stacks
    private val undoStack = mutableListOf<ColorGradeParams>()
    private val redoStack = mutableListOf<ColorGradeParams>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    // Database reactive streams
    val customPresets: StateFlow<List<CustomPresetEntity>> = repository.presets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentProjects: StateFlow<List<RecentProjectEntity>> = repository.recentProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Automatically load or create procedural cinema sample clip on launch so the app is instantly usable
        viewModelScope.launch {
            try {
                val sampleUri = SampleVideoGenerator.getOrCreateSampleVideo(getApplication())
                loadVideo(sampleUri, isSample = true)
            } catch (e: Exception) {
                Log.e(TAG, "Error generating sample video", e)
            }
        }
    }

    fun selectTab(tab: EditorDeckTab) {
        _selectedTab.value = tab
    }

    fun toggleAiSummaryCard() {
        _aiSummaryCardExpanded.value = !_aiSummaryCardExpanded.value
    }

    fun setComparingOriginal(isComparing: Boolean) {
        _isComparingOriginal.value = isComparing
        applyEffectsToPlayer(bypass = isComparing)
    }

    fun loadVideo(uri: Uri, isSample: Boolean = false) {
        viewModelScope.launch {
            try {
                val meta = VideoMetadataHelper.extractMetadata(getApplication(), uri)
                _videoMetadata.value = meta

                // Prepare ExoPlayer
                exoPlayer.stop()
                exoPlayer.clearMediaItems()
                val mediaItem = MediaItem.fromUri(uri)
                exoPlayer.setMediaItem(mediaItem)
                applyEffectsToPlayer()
                exoPlayer.prepare()
                exoPlayer.play()

                // Clear history
                undoStack.clear()
                redoStack.clear()
                updateUndoRedoStates()
                _currentParams.value = ColorGradeParams()
                _aiStatus.value = AiStatus.Idle

                // Extract timeline thumbnails
                val thumbnails = VideoMetadataHelper.extractKeyframes(getApplication(), uri, count = 5)
                _timelineThumbnails.value = thumbnails

                // Persist to recent projects
                repository.saveRecentProject(
                    RecentProjectEntity(
                        videoUri = uri.toString(),
                        videoTitle = if (isSample) "Coastal Sunset Demo (4K Master)" else meta.title,
                        durationMs = meta.durationMs,
                        resolutionLabel = meta.resolutionLabel,
                        selectedFilterId = "none",
                        filterIntensity = 1.0f,
                        exposure = 0.0f,
                        contrast = 0.0f,
                        saturation = 0.0f,
                        temperature = 0.0f,
                        tint = 0.0f,
                        highlights = 0.0f,
                        shadows = 0.0f,
                        sharpen = 0.0f,
                        vignette = 0.0f,
                        aiSummary = null
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load video", e)
            }
        }
    }

    fun runAiAutoEdit() {
        val meta = _videoMetadata.value ?: return
        viewModelScope.launch {
            _aiStatus.value = AiStatus.Analyzing("Extracting cinema keyframes...")
            try {
                val keyframes = VideoMetadataHelper.extractKeyframes(getApplication(), meta.uri, count = 3)
                if (keyframes.isEmpty()) {
                    _aiStatus.value = AiStatus.Error("Unable to extract frames from video.")
                    return@launch
                }

                _aiStatus.value = AiStatus.Analyzing("Gemini 3.8 Flash analyzing scene & lighting...")
                val result = geminiService.analyzeVideoFrames(keyframes)

                result.fold(
                    onSuccess = { analysis ->
                        pushUndoSnapshot()
                        val filter = CinemaFilter.fromId(analysis.recommendedFilterId)
                        val gradedParams = ColorGradeParams(
                            selectedFilter = filter,
                            filterIntensity = analysis.recommendedIntensity,
                            exposure = analysis.recommendedExposure,
                            contrast = analysis.recommendedContrast,
                            saturation = analysis.recommendedSaturation,
                            temperature = analysis.recommendedTemperature,
                            tint = analysis.recommendedTint,
                            highlights = analysis.recommendedHighlights,
                            shadows = analysis.recommendedShadows,
                            sharpen = analysis.recommendedSharpen,
                            vignette = analysis.recommendedVignette
                        )
                        _currentParams.value = gradedParams
                        applyEffectsToPlayer()
                        _aiStatus.value = AiStatus.Success(analysis)
                        _aiSummaryCardExpanded.value = true

                        // Update recent project entry with AI rationale
                        repository.saveRecentProject(
                            RecentProjectEntity(
                                videoUri = meta.uri.toString(),
                                videoTitle = meta.title,
                                durationMs = meta.durationMs,
                                resolutionLabel = meta.resolutionLabel,
                                selectedFilterId = filter.id,
                                filterIntensity = gradedParams.filterIntensity,
                                exposure = gradedParams.exposure,
                                contrast = gradedParams.contrast,
                                saturation = gradedParams.saturation,
                                temperature = gradedParams.temperature,
                                tint = gradedParams.tint,
                                highlights = gradedParams.highlights,
                                shadows = gradedParams.shadows,
                                sharpen = gradedParams.sharpen,
                                vignette = gradedParams.vignette,
                                aiSummary = analysis.coloristRationale
                            )
                        )
                    },
                    onFailure = { error ->
                        _aiStatus.value = AiStatus.Error(error.localizedMessage ?: "AI Auto Edit failed.")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error in AI Auto Edit", e)
                _aiStatus.value = AiStatus.Error(e.localizedMessage ?: "Unexpected error during AI Auto Edit.")
            }
        }
    }

    fun selectFilter(filter: CinemaFilter) {
        pushUndoSnapshot()
        _currentParams.value = _currentParams.value.copy(
            selectedFilter = filter,
            filterIntensity = if (filter == CinemaFilter.NONE) 1.0f else filter.defaultIntensity
        )
        applyEffectsToPlayer()
    }

    fun updateIntensity(intensity: Float) {
        _currentParams.value = _currentParams.value.copy(filterIntensity = intensity)
        applyEffectsToPlayer()
    }

    fun updateExposure(value: Float) {
        _currentParams.value = _currentParams.value.copy(exposure = value)
        applyEffectsToPlayer()
    }

    fun updateContrast(value: Float) {
        _currentParams.value = _currentParams.value.copy(contrast = value)
        applyEffectsToPlayer()
    }

    fun updateSaturation(value: Float) {
        _currentParams.value = _currentParams.value.copy(saturation = value)
        applyEffectsToPlayer()
    }

    fun updateTemperature(value: Float) {
        _currentParams.value = _currentParams.value.copy(temperature = value)
        applyEffectsToPlayer()
    }

    fun updateTint(value: Float) {
        _currentParams.value = _currentParams.value.copy(tint = value)
        applyEffectsToPlayer()
    }

    fun updateHighlights(value: Float) {
        _currentParams.value = _currentParams.value.copy(highlights = value)
        applyEffectsToPlayer()
    }

    fun updateShadows(value: Float) {
        _currentParams.value = _currentParams.value.copy(shadows = value)
        applyEffectsToPlayer()
    }

    fun updateSharpen(value: Float) {
        _currentParams.value = _currentParams.value.copy(sharpen = value)
        applyEffectsToPlayer()
    }

    fun updateVignette(value: Float) {
        _currentParams.value = _currentParams.value.copy(vignette = value)
        applyEffectsToPlayer()
    }

    fun onSliderGestureEnd() {
        // Record undo point at gesture end
        pushUndoSnapshot()
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val previous = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(_currentParams.value)
            _currentParams.value = previous
            applyEffectsToPlayer()
            updateUndoRedoStates()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(_currentParams.value)
            _currentParams.value = next
            applyEffectsToPlayer()
            updateUndoRedoStates()
        }
    }

    fun resetToNeutral() {
        if (!_currentParams.value.isNeutral()) {
            pushUndoSnapshot()
            _currentParams.value = ColorGradeParams()
            applyEffectsToPlayer()
        }
    }

    private fun pushUndoSnapshot() {
        undoStack.add(_currentParams.value)
        if (undoStack.size > 30) undoStack.removeAt(0)
        redoStack.clear()
        updateUndoRedoStates()
    }

    private fun updateUndoRedoStates() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }

    private fun applyEffectsToPlayer(bypass: Boolean = false) {
        val effects = Media3EffectFactory.createEffects(_currentParams.value, bypass = bypass)
        exoPlayer.setVideoEffects(effects)
    }

    // Custom Preset actions
    fun openSavePresetDialog() {
        _showSavePresetDialog.value = true
    }

    fun dismissSavePresetDialog() {
        _showSavePresetDialog.value = false
    }

    fun saveCurrentAsCustomPreset(name: String) {
        val p = _currentParams.value
        viewModelScope.launch {
            repository.savePreset(
                CustomPresetEntity(
                    name = name.ifBlank { "Custom Grade ${System.currentTimeMillis() % 1000}" },
                    baseFilterId = p.selectedFilter.id,
                    filterIntensity = p.filterIntensity,
                    exposure = p.exposure,
                    contrast = p.contrast,
                    saturation = p.saturation,
                    temperature = p.temperature,
                    tint = p.tint,
                    highlights = p.highlights,
                    shadows = p.shadows,
                    sharpen = p.sharpen,
                    vignette = p.vignette
                )
            )
            _showSavePresetDialog.value = false
        }
    }

    fun applyCustomPreset(preset: CustomPresetEntity) {
        pushUndoSnapshot()
        _currentParams.value = ColorGradeParams(
            selectedFilter = CinemaFilter.fromId(preset.baseFilterId),
            filterIntensity = preset.filterIntensity,
            exposure = preset.exposure,
            contrast = preset.contrast,
            saturation = preset.saturation,
            temperature = preset.temperature,
            tint = preset.tint,
            highlights = preset.highlights,
            shadows = preset.shadows,
            sharpen = preset.sharpen,
            vignette = preset.vignette
        )
        applyEffectsToPlayer()
    }

    fun deleteCustomPreset(id: Long) {
        viewModelScope.launch {
            repository.deletePreset(id)
        }
    }

    // Export Sheet Actions
    fun openExportDialog() {
        _showExportDialog.value = true
    }

    fun dismissExportDialog() {
        _showExportDialog.value = false
        if (_exportState.value is ExportState.Completed || _exportState.value is ExportState.Error) {
            _exportState.value = ExportState.Idle
        }
    }

    fun startExport(resolution: ExportResolution, frameRate: ExportFrameRate) {
        val meta = _videoMetadata.value ?: return
        exportManager.exportVideo(
            sourceUri = meta.uri,
            params = _currentParams.value,
            resolution = resolution,
            frameRate = frameRate
        ) { state ->
            _exportState.value = state
        }
    }

    fun cancelExport() {
        exportManager.cancelExport {
            _exportState.value = ExportState.Cancelled
        }
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
    }
}
