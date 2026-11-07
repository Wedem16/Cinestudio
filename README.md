CineAI Studio
CineAI Studio is a native Android cinema color grading and AI video processing studio built with Kotlin, Jetpack Compose, Firebase AI Logic (gemini-3.8-flash), AndroidX Media3 (ExoPlayer + Transformer + custom OpenGL ES shaders), and Room SQLite.
Features
AI Auto Edit (gemini-3.8-flash): Extracts representative keyframes locally via MediaMetadataRetriever and uses Firebase AI Logic with structured JSON output to detect scene type, lighting conditions, dominant color palette, and exposure balance, automatically applying a matched cinema preset and 9-axis color grade with a detailed colorist rationale.
7 Signature Cinema Filters:
Beach Cinematic — Ocean blue, tropical colors, bright natural highlights, teal shadows
City Night — Neon colors, night cinematic contrast, controlled highlights, natural skin tones
Nature Cinematic — Natural greens, detailed clouds, balanced contrast, realistic colors
Sony FX3 Inspired — S-Cinetone inspired cinema color, natural skin tones, smooth highlight roll-off, detailed shadows
Golden Hour — Warm sunlight, golden highlights, soft cinematic contrast
Cloudy Cinematic — Soft moody colors, detailed clouds, natural mountain greens
Clean Travel Film — Clear blue skies, natural greenery, crisp realistic details
Real-Time GPU Grading & 9-Axis Controls:
Master Filter Intensity slider (0–100%)
Exposure, Contrast, Saturation, Temperature (Kelvin), Tint (Green/Magenta), Highlights, Shadows, 5-tap Spatial Convolution Sharpen, and Radial Optical Vignette
Hold to Compare button for instant original vs. graded preview
Non-destructive Undo / Redo / Reset history stack
Local Room persistence for custom user presets and recent project sessions
Hardware MP4 Render & Export (Media3 Transformer):
Source Match, 720p HD, 1080p FHD, and 4K Cinema UHD (3840 × 2160)
30 FPS and 60 FPS support with original audio preservation
Live progress indicator, export cancellation, automatic save to Movies/CineAI Studio, and system share sheet integration
Tech Stack
UI: Jetpack Compose, Material Design 3, Custom Local Fonts (Space Grotesk & JetBrains Mono)
AI: Firebase AI Logic (com.google.firebase:firebase-ai) + Firebase App Check
Video Engine: AndroidX Media3 (media3-exoplayer, media3-transformer, media3-effect, media3-ui, media3-common)
Local Storage: AndroidX Room (room-runtime, room-ktx, KSP)
