# CineAI Studio

CineAI Studio is a native Android cinema color grading and video editing studio that pairs Gemini multimodal scene intelligence (via Firebase AI Logic) with hardware-accelerated AndroidX Media3 video processing (`ExoPlayer` + `Transformer` + `GlEffect` / `GlShaderProgram`) to analyze, grade, compare, and export professional MP4 footage locally on the device.

### User Review & Critical Decisions

> [!IMPORTANT]
> The following product and UX decisions were confirmed during our interactive discovery session and govern the implementation of CineAI Studio:

- **Confirmed Decision 1 — Before & After Comparison**: Uses a dedicated **Press-and-Hold Compare button** overlay on the video preview viewport (`COMPARE` pill badge with live `ORIGINAL` / `GRADED` status indicator) that temporarily bypasses the active GL color grading shader pipeline while held, allowing instant zero-clutter visual A/B inspection of the full frame.
- **Confirmed Decision 2 — AI Scene Analysis Presentation**: Displays an expandable **AI Scene Summary Card** after tapping **AI AUTO EDIT**, detailing detected scene type, lighting condition, ambient color palette, exposure assessment, matched cinema preset, and a plain-English colorist rationale explaining why specific grading adjustments were applied.
- **Confirmed Decision 3 — Local Persistence Strategy**: Persists **custom user color presets** and **recent project editing sessions** locally on the device using Room SQLite so creators can revisit recent clips, restore non-destructive parameter stacks, and reuse custom looks without uploading raw video files to the cloud.
- **Confirmed Decision 4 — Firebase AI Logic Provisioning**: Uses `gemini-3.8-flash` via **Firebase AI Logic** (`Firebase.ai`) with structured JSON output (`Schema.obj`) and App Check attestation to analyze representative frames extracted locally via `MediaMetadataRetriever`.

---

### 1. Overview & Core Concept

- **What It Does**: Enables mobile filmmakers and content creators to import local video clips (or load a built-in procedural cinema test clip for instant testing), run **AI Auto Edit** to have Gemini inspect keyframes and automatically dial in a 9-axis color grade plus a matched cinema LUT preset, fine-tune looks in real time using custom GLSL shader effects in ExoPlayer, and render/export hardware-encoded MP4 videos at 720p, 1080p, or 4K (30/60 FPS) via AndroidX Media3 `Transformer`.
- **Target Audience / Persona**: Mobile cinematographers, travel filmmakers, vloggers, and creators inspired by dedicated cinema cameras (such as Sony FX3, RED, and Blackmagic OS) who want authentic color grading controls rather than novelty social stickers.
- **Key Value**: Bridges the gap between complex manual color grading and opaque one-tap filters—Gemini acts as an intelligent digital colorist that inspects actual frame exposure, skin tones, and lighting to set transparent, non-destructive grading sliders that render locally with full audio preservation.

---

### 2. User Experience & Visual Design

- **Key User Flows**:
  1. **Studio Launch & Video Selection**: User opens CineAI Studio to a dark cinema workspace showing an import hero viewport, a quick-start **"Load Sample Cinema Clip"** option (generates a rich local MP4 test clip on-device so every feature works even on an empty emulator gallery), and a **Recent Projects** strip from local storage. Tapping **Import Video** launches the zero-permission Android Photo Picker (`PickVisualMedia(VideoOnly)`).
  2. **Live Preview & Playback**: Once loaded, the video plays in a large adaptive viewport backed by `ExoPlayer` with play/pause, scrubbing timeline with extracted frame thumbnails, resolution/FPS telemetry badges (`1080p • 60 FPS • REC.709`), and audio mute toggle.
  3. **AI Auto Edit Flow**: User taps the glowing cyan **"AI AUTO EDIT"** button. The app extracts 3 representative keyframes (start, middle, end) locally via `MediaMetadataRetriever`, sends them to `gemini-3.8-flash` with a strict JSON schema, and displays an animated scanning HUD over the viewport. Upon completion, the **AI Scene Summary Card** reveals detected environment, lighting, mood, recommended preset, and grading rationale, while smoothly animating all color parameters and filter intensity into place.
  4. **Cinema Filter Collection & Intensity**: User switches to the **Filters** deck featuring the 7 signature cinema looks (*Beach Cinematic*, *City Night*, *Nature Cinematic*, *Sony FX3 Inspired*, *Golden Hour*, *Cloudy Cinematic*, *Clean Travel Film*) plus saved custom user presets. Each filter card shows a live-graded thumbnail preview, descriptive tags, and a master **Filter Intensity Slider (0–100%)** that interpolates all look matrices and tone curves proportionally.
  5. **9-Axis Colorist Adjustments & History**: User switches to the **Adjust** deck to fine-tune **Exposure**, **Contrast**, **Saturation**, **Temperature**, **Tint**, **Highlights**, **Shadows**, **Sharpen**, and **Vignette**. Every completed slider gesture pushes a state snapshot onto the non-destructive **Undo / Redo / Reset** stack. Users can also tap **"Save Custom Preset"** to store their current grade locally.
  6. **Press-and-Hold A/B Comparison**: At any time, holding the **Hold to Compare** button on the preview corner switches the live ExoPlayer GL effect pipeline to 0% grade (`ORIGINAL` badge highlighted) and restores the full grade immediately upon release (`GRADED` badge).
  7. **MP4 Hardware Export & Sharing**: Tapping **Export** opens the Cinema Render Sheet where the user selects target resolution (**Original**, **720p**, **1080p**, or **4K** gated by source/device capability) and frame rate (**30 FPS** or **60 FPS**). Media3 `Transformer` renders the video with the custom GLSL color grading shader and original audio track, displaying a real-time percentage progress bar, elapsed time, and **Cancel Export** button. Completed exports can be saved directly to the phone gallery (`MediaStore.Videos`) or shared via the Android system share sheet (`FileProvider`).

- **Visual Identity & Theme**:
  - *Aesthetic Direction*: **Pro Cinema Camera OS** (inspired by Sony CineAlta Venice/FX3 HUDs, DaVinci Resolve Micro Panel, and Blackmagic Camera). Ultra-clean dark surfaces, precision technical telemetry badges, tactile tick-mark sliders, and luminous electric-cyan accents.
  - *Color Palette & Mood*:
    - `--background`: `#090B0E` (Deep Obsidian Studio Void)
    - `--surface`: `#12161C` (Anodized Matte Aluminum Deck)
    - `--surface-elevated`: `#1A2029` (Elevated Control Module & Card Surface)
    - `--primary` / `--accent`: `#00E5FF` (Electric Cinema Cyan for AI Auto Edit, active sliders, and telemetry highlights)
    - `--secondary`: `#FFB300` (Amber Tally / Cinema REC & Mastering Indicator)
    - `--text-primary`: `#F3F6FA` (Crisp Studio White)
    - `--text-secondary`: `#8E9BAE` (Muted Technical Slate)
    - `--border-subtle`: `#252D3A` (1dp Precision Bezel Hairlines)
  - *Typography & Hierarchy*:
    - **Display & Section Headers**: `Space Grotesk` (bundled locally via `font-util`) — geometric, modern engineering character for studio headers, preset titles, and modal headers.
    - **Body & Controls**: `Inter` / `Plus Jakarta Sans` — clean, high-legibility labels for adjustment tools and AI scene rationale.
    - **Telemetry & Timecodes**: `JetBrains Mono` — tabular monospaced figures for timecodes (`00:01:24:12`), parameter readouts (`+1.25 EV`, `85%`), resolution badges, and export progress percentages.
  - *Component Styling & Layout*:
    - Top Studio Telemetry Bar with project metadata, Undo (`↶`), Redo (`↷`), Reset, Save Preset, and high-contrast Cyan `EXPORT` action pill.
    - Dominant 16:9 / adaptive Video Preview Viewport framed by a 1dp `#252D3A` bezel, top-left technical badges, top-right **Hold to Compare** pill button, and collapsible **AI Scene Summary Card** overlay.
    - Bottom Control Deck divided into quick-switch mode tabs (**AI Studio**, **Cinema Filters**, **Color Wheels & Sliders**, **Presets & History**) with generous 48dp+ touch targets and precision zero-centered horizontal dial sliders.
  - *Interactive Feedback & Motion*:
    - Spring-physics transitions (`spring(stiffness = Spring.StiffnessMediumLow)`) when expanding the AI Scene Summary Card or switching tool decks.
    - Animated cyan laser scanline across the video frame while Gemini analyzes keyframes.
    - Haptic-style visual tick highlights when a parameter slider crosses the `0.0` neutral center point or when pressing and holding the A/B Compare button.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Unified Custom GLSL Shader (`GlEffect` / `BaseGlShaderProgram`) for Both Live Preview and Final Export**
  - *Chosen Approach*: Implement a single custom AndroidX Media3 `GlEffect` (`CineColorGradeEffect`) backed by a GLSL fragment shader that computes the selected Cinema Filter LUT approximation + Filter Intensity (`0–100%`) + all 9 manual color grading parameters (Exposure in EV stops, Contrast around mid-gray, luma-preserving Saturation, Kelvin Temperature, Magenta/Green Tint, Luminance-masked Highlights & Shadows, 5-tap Spatial Convolution Sharpen, and Radial Smoothstep Vignette). Attach this exact `GlEffect` to both `ExoPlayer.setVideoEffects(...)` and `EditedMediaItem.Builder.setEffects(...)` in Media3 `Transformer`.
  - *Why*: Guarantees **100% pixel-identical parity** between what the user sees in the interactive preview player and the final exported MP4 video, while maintaining 60fps GPU-accelerated rendering and preserving the original audio stream untouched.
  - *Alternatives Considered*: Applying a Compose `ColorFilter` overlay on top of `AndroidView(PlayerView)` was rejected because `PlayerView` uses a hardware surface and Compose overlays would not bake into the Media3 `Transformer` MP4 export or support spatial sharpening/vignette accurately.

- **Decision 2: Local Keyframe Extraction + Gemini 3.8 Flash Structured JSON Analysis**
  - *Chosen Approach*: Extract 3 downscaled representative `Bitmap` frames (at 15%, 50%, and 85% of video duration, max 768px long edge) on `Dispatchers.IO` using `MediaMetadataRetriever`, then pass them as multimodal inputs to `Firebase.ai.generativeModel("gemini-3.8-flash")` configured with `responseMimeType = "application/json"` and a strict `Schema.obj` schema returning scene classification, lighting analysis, recommended preset ID, filter intensity, all 9 slider values (`-1.0` to `+1.0`), and colorist reasoning.
  - *Why*: Keeps payload size well under 1MB for fast 2–4 second inference, never uploads the user's raw video file, and guarantees deterministic JSON parsing into our grading state machine.
  - *Alternatives Considered*: Uploading the entire video file was rejected due to user privacy constraints ("Never upload the entire video without user consent") and high latency on mobile networks.

- **Decision 3: Built-in Procedural Cinema Sample Video Generator Alongside Gallery Photo Picker**
  - *Chosen Approach*: Use Android's zero-permission `ActivityResultContracts.PickVisualMedia(PickVisualMedia.VideoOnly)` for gallery uploads, and include a one-tap **"Load Sample Cinema Clip"** generator that synthesizes a real 5-second 1080p MP4 video with audio using `MediaCodec` + `MediaMuxer` on-device if the user wants to test AI Auto Edit and Media3 export immediately on a fresh emulator.
  - *Why*: Streaming Android emulators often start with an empty gallery; providing both real gallery upload and an on-device synthesized MP4 clip ensures the user can immediately verify real frame extraction, Gemini AI grading, GL shader preview, and MP4 export without friction.

---

### 4. Technical Architecture & Data Strategy *(Technical Reference)*

- **Architecture & Component Diagram**:

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                        CINEAI STUDIO — UI LAYER (COMPOSE)                    │
├──────────────────────┬───────────────────────────┬───────────────────────────┤
│  Top Telemetry Bar   │   Live Cinema Viewport    │    Bottom Grading Deck    │
│  • Project Title     │   • ExoPlayer Surface     │   • AI Auto Edit Panel    │
│  • Undo / Redo / Rst │   • Hold-to-Compare Pill  │   • 7 Cinema Filter Cards │
│  • Save Custom Look  │   • AI Scene Summary Card │   • Intensity (0–100%)    │
│  • Export Trigger    │   • Timeline & Scrubber   │   • 9-Axis Color Sliders  │
└──────────┬───────────┴─────────────┬─────────────┴─────────────┬─────────────┘
           │                         │                           │
           ▼                         ▼                           ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                     EDITOR VIEWMODEL & UNDO/REDO STATE MACHINE               │
│  • Single Source of Truth: StateFlow<EditorUiState>                          │
│  • Grading History Stack: Undo / Redo / Reset non-destructive parameter log  │
│  • Live Effect Dispatcher: Rebuilds Effects list on parameter or A/B toggle  │
└──────────┬─────────────────────────┬───────────────────────────┬─────────────┘
           │                         │                           │
           ▼                         ▼                           ▼
┌──────────────────────┐  ┌─────────────────────────┐  ┌───────────────────────┐
│  AI COLORIST ENGINE  │  │  MEDIA3 VIDEO PIPELINE  │  │  LOCAL ROOM DATABASE  │
│  • FrameExtractor    │  │  • ExoPlayer Preview    │  │  • CustomPresetEntity │
│    (MetadataRetr.)   │  │  • CineColorGradeEffect │  │  • RecentProjectEntity│
│  • Firebase.ai       │  │    (Custom GLSL Shader) │  │  • Reactive Flow DAOs │
│    (gemini-3.8-flash)│  │  • Media3 Transformer   │  │    for instant preset │
│  • Schema.obj JSON   │  │    (720p/1080p/4K,      │  │    & session recall   │
│    Grading Parser    │  │     30/60fps, Audio)    │  │                       │
└──────────────────────┘  └─────────────────────────┘  └───────────────────────┘
```

- **Data Model & State**:
  - `ColorGradeParams`: Immutable data model containing:
    - `selectedFilterId`: Enum/ID for `NONE`, `BEACH_CINEMATIC`, `CITY_NIGHT`, `NATURE_CINEMATIC`, `SONY_FX3`, `GOLDEN_HOUR`, `CLOUDY_CINEMATIC`, `CLEAN_TRAVEL`, or custom preset ID.
    - `filterIntensity`: `Float` (`0.0f` to `1.0f`, displayed as `0–100%`).
    - 9 adjustment parameters (`Float` in `-1.0f..1.0f`, `0.0f` neutral): `exposure`, `contrast`, `saturation`, `temperature`, `tint`, `highlights`, `shadows`, `sharpen`, `vignette`.
  - `AiSceneAnalysis`: Structured result from Gemini containing `sceneType`, `lightingCondition`, `environment`, `dominantColors`, `exposureAssessment`, `recommendedFilterId`, `recommendedIntensity`, `recommendedParams`, and `coloristRationale`.
  - `VideoMetadata`: Extracted source video properties (`uri`, `title`, `width`, `height`, `durationMs`, `frameRate`, `bitrate`, `hasAudio`, `is4kSupported`).
  - `ExportConfig` & `ExportState`: Target resolution (`ORIGINAL`, `RES_720P`, `RES_1080P`, `RES_4K`), target frame rate (`FPS_ORIGINAL`, `FPS_30`, `FPS_60`), and sealed export progress (`Idle`, `Exporting(progressPercent, elapsedMs)`, `Completed(outputFile, savedGalleryUri)`, `Cancelled`, `Error(reason)`).
  - Room Entities:
    - `CustomPresetEntity`: Stores user-saved presets (`id`, `name`, `baseFilterId`, `filterIntensity`, and the 9 serialized slider values, `createdAt`).
    - `RecentProjectEntity`: Stores recent editing sessions (`id`, `videoUriString`, `videoTitle`, `durationMs`, `resolutionLabel`, `appliedFilterName`, serialized `ColorGradeParams`, ` aiSummaryText`, `lastEditedAt`).

- **Interactive Component & State Mapping**:
  - **Gallery Upload & Sample Clip Generator**: Selecting a video via `PickVisualMedia` (or generating the sample clip) triggers `loadVideo(uri)`, which extracts `VideoMetadata` and timeline thumbnail strip on `Dispatchers.IO`, initializes `ExoPlayer` with the source `MediaItem`, resets the Undo/Redo history stack, and persists a `RecentProjectEntity` entry.
  - **AI Auto Edit Handler**: Tapping `AI AUTO EDIT` transitions `aiState` to `Analyzing`, extracts 3 representative `Bitmap` frames via `MediaMetadataRetriever.getFrameAtTime(...)`, invokes `Firebase.ai.generativeModel("gemini-3.8-flash")` within `withTimeout(60_000L)`, parses the strict JSON response into `AiSceneAnalysis`, pushes the previous grade to the `undoStack`, applies the recommended filter + 9-axis parameters to `ExoPlayer`, and reveals the `AiSceneSummaryCard`.
  - **Filter & Slider Mutations**: Selecting any of the 7 Cinema Filters or dragging the `0–100%` Intensity / 9-axis adjustment sliders updates `currentParams` immediately (triggering `exoPlayer.setVideoEffects(listOf(CineColorGradeEffect(effectiveParams)))` for real-time GPU preview) and commits a snapshot to `undoStack` on `onValueChangeFinished`.
  - **Hold-to-Compare Gesture**: Using `pointerInput` (`detectTapGestures(onPress = { setComparingOriginal(true); tryAwaitRelease(); setComparingOriginal(false) })`), holding the compare button swaps `ExoPlayer`'s effect list to a neutral bypass effect while pressed and restores the active `CineColorGradeEffect` on release.
  - **Undo / Redo / Reset**: Tapping Undo pops the last `ColorGradeParams` from `undoStack` onto `redoStack` and updates the live player; Reset pushes the current non-neutral grade to `undoStack` and restores default neutral `0.0f` values.
  - **Media3 Transformer Export**: Tapping `Start Export` builds an `EditedMediaItem` with `setRemoveAudio(false)`, attaches `CineColorGradeEffect(currentParams)` plus optional `Presentation.createForHeight(...)` (for 720p/1080p/4K) and frame rate control, starts `Transformer.start(editedMediaItem, outputFilePath)`, and polls `Transformer.getProgress(ProgressHolder)` every 100ms on the main looper until `onCompleted` or `onError`. Tapping `Cancel` invokes `transformer.cancel()` and deletes the partial temporary file. Tapping `Save to Gallery` copies the MP4 into `MediaStore.Video.Media.EXTERNAL_CONTENT_URI` via `ContentResolver`, and `Share` launches `Intent.ACTION_SEND` with a `FileProvider` URI.
