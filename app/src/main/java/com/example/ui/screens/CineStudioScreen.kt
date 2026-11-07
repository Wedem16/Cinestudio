package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.components.AdjustmentsDeckView
import com.example.ui.components.AiSceneSummaryCard
import com.example.ui.components.ExportSheetDialog
import com.example.ui.components.FilterDeckView
import com.example.ui.components.PresetsHistoryDeckView
import com.example.ui.components.SavePresetDialog
import com.example.ui.components.VideoPreviewViewport
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CinemaAmber
import com.example.ui.theme.CinemaCyan
import com.example.ui.theme.CinemaRed
import com.example.ui.theme.DeckSurface
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AiStatus
import com.example.viewmodel.CineAiViewModel
import com.example.viewmodel.EditorDeckTab

@Composable
fun CineStudioScreen(viewModel: CineAiViewModel) {
    val metadata by viewModel.videoMetadata.collectAsState()
    val timelineThumbnails by viewModel.timelineThumbnails.collectAsState()
    val currentParams by viewModel.currentParams.collectAsState()
    val isComparingOriginal by viewModel.isComparingOriginal.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val aiStatus by viewModel.aiStatus.collectAsState()
    val aiSummaryCardExpanded by viewModel.aiSummaryCardExpanded.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()
    val customPresets by viewModel.customPresets.collectAsState()
    val recentProjects by viewModel.recentProjects.collectAsState()
    val exportState by viewModel.exportState.collectAsState()
    val showExportDialog by viewModel.showExportDialog.collectAsState()
    val showSavePresetDialog by viewModel.showSavePresetDialog.collectAsState()

    // Android zero-permission Photo/Video Picker
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.loadVideo(uri)
        }
    }

    Scaffold(
        containerColor = ObsidianBg,
        topBar = {
            StudioTelemetryHeader(
                projectName = metadata?.title ?: "CineAI Studio",
                canUndo = canUndo,
                canRedo = canRedo,
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                onReset = { viewModel.resetToNeutral() },
                onExport = { viewModel.openExportDialog() },
                onPickVideo = {
                    videoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Live Video Viewport with Hold-to-Compare & Filmstrip Scrubber
            Box(modifier = Modifier.padding(horizontal = 12.dp)) {
                VideoPreviewViewport(
                    exoPlayer = viewModel.exoPlayer,
                    metadata = metadata,
                    timelineThumbnails = timelineThumbnails,
                    isComparingOriginal = isComparingOriginal,
                    aiStatus = aiStatus,
                    onHoldCompareChanged = { viewModel.setComparingOriginal(it) }
                )
            }

            // 2. Primary AI Auto Edit Action Banner
            Box(modifier = Modifier.padding(horizontal = 12.dp)) {
                Button(
                    onClick = { viewModel.runAiAutoEdit() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CinemaCyan,
                        contentColor = ObsidianBg
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("ai_auto_edit_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (aiStatus is AiStatus.Analyzing) "GEMINI ANALYZING SCENE..." else "AI AUTO EDIT",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // 3. AI Scene Summary Card (when Gemini returns analysis)
            if (aiStatus is AiStatus.Success) {
                Box(modifier = Modifier.padding(horizontal = 12.dp)) {
                    AiSceneSummaryCard(
                        analysis = (aiStatus as AiStatus.Success).analysis,
                        expanded = aiSummaryCardExpanded,
                        onToggleExpand = { viewModel.toggleAiSummaryCard() }
                    )
                }
            } else if (aiStatus is AiStatus.Error) {
                Box(modifier = Modifier.padding(horizontal = 12.dp)) {
                    Surface(
                        color = CinemaRed.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CinemaRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = (aiStatus as AiStatus.Error).message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = CinemaRed,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // 4. Editor Deck Tabs (Cinema Filters, 9-Axis Adjustments, Presets & History)
            DeckTabBar(
                selectedTab = selectedTab,
                onTabSelected = { viewModel.selectTab(it) }
            )

            // 5. Active Deck Content
            Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                when (selectedTab) {
                    EditorDeckTab.FILTERS -> {
                        FilterDeckView(
                            selectedFilter = currentParams.selectedFilter,
                            intensity = currentParams.filterIntensity,
                            onFilterSelected = { viewModel.selectFilter(it) },
                            onIntensityChanged = { viewModel.updateIntensity(it) }
                        )
                    }
                    EditorDeckTab.ADJUSTMENTS -> {
                        AdjustmentsDeckView(
                            params = currentParams,
                            onExposureChanged = { viewModel.updateExposure(it) },
                            onContrastChanged = { viewModel.updateContrast(it) },
                            onSaturationChanged = { viewModel.updateSaturation(it) },
                            onTemperatureChanged = { viewModel.updateTemperature(it) },
                            onTintChanged = { viewModel.updateTint(it) },
                            onHighlightsChanged = { viewModel.updateHighlights(it) },
                            onShadowsChanged = { viewModel.updateShadows(it) },
                            onSharpenChanged = { viewModel.updateSharpen(it) },
                            onVignetteChanged = { viewModel.updateVignette(it) },
                            onGestureEnd = { viewModel.onSliderGestureEnd() }
                        )
                    }
                    EditorDeckTab.PRESETS_HISTORY -> {
                        PresetsHistoryDeckView(
                            presets = customPresets,
                            recentProjects = recentProjects,
                            onSaveCurrentPreset = { viewModel.openSavePresetDialog() },
                            onApplyPreset = { viewModel.applyCustomPreset(it) },
                            onDeletePreset = { viewModel.deleteCustomPreset(it) },
                            onSelectRecentProject = { proj ->
                                viewModel.loadVideo(Uri.parse(proj.videoUri))
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Export Dialog
    if (showExportDialog) {
        ExportSheetDialog(
            metadata = metadata,
            exportState = exportState,
            onStartExport = { res, fps -> viewModel.startExport(res, fps) },
            onCancelExport = { viewModel.cancelExport() },
            onDismiss = { viewModel.dismissExportDialog() }
        )
    }

    // Save Preset Dialog
    if (showSavePresetDialog) {
        SavePresetDialog(
            onConfirm = { viewModel.saveCurrentAsCustomPreset(it) },
            onDismiss = { viewModel.dismissSavePresetDialog() }
        )
    }
}

@Composable
private fun StudioTelemetryHeader(
    projectName: String,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onReset: () -> Unit,
    onExport: () -> Unit,
    onPickVideo: () -> Unit
) {
    Surface(
        color = DeckSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Branding & Video Selector
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onPickVideo() }
            ) {
                Surface(
                    color = CinemaCyan.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = "Import Video",
                        tint = CinemaCyan,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = "CINEAI STUDIO",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = "IMPORT VIDEO",
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaCyan,
                        maxLines = 1
                    )
                }
            }

            // Undo, Redo, Reset & Export Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = onUndo,
                    enabled = canUndo,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("undo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Undo,
                        contentDescription = "Undo",
                        tint = if (canUndo) TextPrimary else TextSecondary.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onRedo,
                    enabled = canRedo,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("redo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Redo,
                        contentDescription = "Redo",
                        tint = if (canRedo) TextPrimary else TextSecondary.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onReset,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("reset_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Reset Look",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Button(
                    onClick = onExport,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CinemaAmber,
                        contentColor = ObsidianBg
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 12.dp,
                        vertical = 6.dp
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("export_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "EXPORT",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
private fun DeckTabBar(
    selectedTab: EditorDeckTab,
    onTabSelected: (EditorDeckTab) -> Unit
) {
    val tabIndex = selectedTab.ordinal.coerceIn(0, 2)
    TabRow(
        selectedTabIndex = tabIndex,
        containerColor = DeckSurface,
        contentColor = CinemaCyan,
        indicator = { tabPositions ->
            if (tabIndex < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[tabIndex]),
                    color = CinemaCyan,
                    height = 2.5.dp
                )
            }
        },
        divider = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(BorderSubtle)
            )
        }
    ) {
        Tab(
            selected = selectedTab == EditorDeckTab.FILTERS,
            onClick = { onTabSelected(EditorDeckTab.FILTERS) },
            text = { Text("CINEMA LOOKS", style = MaterialTheme.typography.labelSmall) },
            modifier = Modifier.testTag("tab_filters")
        )
        Tab(
            selected = selectedTab == EditorDeckTab.ADJUSTMENTS,
            onClick = { onTabSelected(EditorDeckTab.ADJUSTMENTS) },
            text = { Text("9-AXIS COLOR", style = MaterialTheme.typography.labelSmall) },
            modifier = Modifier.testTag("tab_adjustments")
        )
        Tab(
            selected = selectedTab == EditorDeckTab.PRESETS_HISTORY,
            onClick = { onTabSelected(EditorDeckTab.PRESETS_HISTORY) },
            text = { Text("PRESETS & HISTORY", style = MaterialTheme.typography.labelSmall) },
            modifier = Modifier.testTag("tab_presets")
        )
    }
}
