# Kaze Player

> 風 — A lean, functional local music player for Android.

Kaze Player is a lightweight Android music player focused on clean design, reliable playback, and a distraction-free experience. Built with modern Android tooling.

## Features

- **Local music playback** — Scans your device's music library via MediaStore
- **Library browsing** — Browse by Songs, Albums, and Artists
- **Search** — Quick search across your entire library
- **Now Playing** — Full-screen player with album art and progress
- **Queue management** — View and reorder your playback queue
- **Shuffle & Repeat** — Standard playback modes (shuffle now resolves tracks by id, so the "now playing" highlight stays correct while shuffling)
- **Playback speed** — 0.5x–2x, with a default speed saved in settings
- **Sleep timer** — Auto-pause after 15/30/45/60/90 minutes
- **Favorites** — Tap the heart on any song; saved list with its own screen and home shortcut
- **User playlists** — Create your own playlists; add any song via its context menu, rename or delete them. Persisted as JSON via DataStore (no database, stays lean)
- **Song context menu** — Long-press the overflow on any song for *Play next*, *Add to queue*, *Add to playlist*, and favorite toggle (inspired by Salt Player)
- **Real settings** — Dynamic color, theme mode (System / Light / Dark / Black), and default speed — all persisted via DataStore
- **Background playback** — Media3 MediaSessionService for notification controls
- **Lyrics** — LRC format lyrics support (auto-loaded from adjacent .lrc files)
- **Material 3** — Dynamic color support with Material You
- **Edge-to-edge** — Full immersive UI

## Tech Stack

| Component | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Playback | Media3 (ExoPlayer) |
| Navigation | Navigation Compose (type-safe) |
| Images | Coil |
| Settings | DataStore Preferences |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 36 (Android 16) |
| ABIs | armeabi-v7a, arm64-v8a |
| APK size | < 10 MB (release, R8 shrunk + resource shrinker) |

## Getting Started

### Prerequisites

- Android Studio (latest stable)
- JDK 17+
- Android SDK with API 36 (Android 16)

### Build

1. Clone the repository:
   ```bash
   git clone https://github.com/0Sakura721/Kaze-Player.git
   cd Kaze-Player
   ```

2. Open in Android Studio, or build from command line:
   ```bash
   ./gradlew assembleDebug
   ```

3. Install on device:
   ```bash
   ./gradlew installDebug
   ```

### Permissions

On first launch, Kaze Player will request:
- **Music access** (`READ_MEDIA_AUDIO` on Android 13+ / `READ_EXTERNAL_STORAGE` on older versions)
- **Notifications** (for playback controls on Android 13+)

## Project Structure

```
app/src/main/java/com/kaze/player/
├── KazeApplication.kt          # Application class
├── MainActivity.kt             # Single-activity entry point
├── data/
│   ├── model/                  # Song, Album, Artist, Playlist
│   ├── repository/             # MusicRepository (MediaStore scanning)
│   ├── playlist/               # PlaylistRepository (DataStore + JSON)
│   ├── favorites/              # FavoritesRepository (DataStore)
│   └── lyrics/                 # LRC parser
├── player/
│   ├── PlayerService.kt        # Media3 MediaSessionService
│   └── PlayerManager.kt        # Player state management
├── ui/
│   ├── theme/                  # Material 3 theme, colors, typography
│   ├── navigation/             # Type-safe navigation
│   ├── components/             # Reusable Compose components
│   └── screens/                # All app screens
├── viewmodel/                  # ViewModels
└── util/                       # Extensions, permissions
```

## Design Philosophy

Kaze Player follows three principles:

1. **Lean** — No unnecessary dependencies. No DI framework. No database (uses MediaStore directly).
2. **Functional** — Every feature works. No half-baked UI.
3. **Clean** — Minimal, readable code that's easy to maintain.

## Inspiration

Kaze Player draws inspiration from:
- [Salt Player](https://github.com/Moriafly/SaltPlayerSource) — Android music player architecture
- [Folia](https://github.com/chthollyphile/folia-major) — Lyrics-focused experience
- [Mineradio](https://github.com/XxHuberrr/Mineradio) — Immersive player design

## Supported Formats

MP3, FLAC, OGG, M4A, WAV, AAC (via ExoPlayer)

## License

MIT License — see [LICENSE](LICENSE) for details.
