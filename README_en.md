# Ella Music

A clean and elegant local music player built with Jetpack Compose and Miuix UI.

## Features

- 🎵 Local music scanning and playback (powered by ExoPlayer/Media3)
- 📝 Word-level lyrics display (enhanced LRC format)
- 📖 Embedded lyrics reading (powered by JAudioTagger)
- 🔊 ReplayGain volume normalization
- 💬 Status bar lyrics (Lyricon module)
- 📱 Meizu Ticker lyrics notification
- 🎨 Light / Dark / System theme
- 📁 Folder browsing mode
- 🔍 Song search
- 📀 Album browsing

## Build

```bash
git clone https://github.com/Kifranei/Ella.git
cd Ella
./gradlew assembleDebug
```

## Dependencies

| Library | Purpose |
|---|---|
| [Miuix](https://github.com/miuix-kmp/miuix) | MIUI/HyperOS style UI components |
| [ExoPlayer (Media3)](https://github.com/androidx/media) | Audio decoding and playback |
| [Lyricon](https://github.com/proify/lyricon) | Status bar lyrics extension module |
| [JAudioTagger](https://github.com/ijabergern/jaudiotagger) | Audio tag reading/writing |
| [Coil](https://github.com/coil-kt/coil) | Image loading |
| [Backdrop](https://github.com/niclas/AndroidLiquidGlass) | Liquid glass effect |

## Credits

- **Mimo-V2.5-Pro** — Lead developer
- Miuix — UI component library
- ExoPlayer — Media playback engine
- Lyricon — Lyrics overlay adaptation
- JAudioTagger — Tag parsing
- Coil — Image loading

## License

```
Copyright © 2026 Ella Music. All rights reserved.
```
