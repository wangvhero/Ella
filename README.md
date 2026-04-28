# Ella Music

一款简洁优雅的本地音乐播放器，基于 Jetpack Compose 和 Miuix UI 构建。

## 功能特性

- 🎵 本地音乐扫描与播放（基于 ExoPlayer/Media3）
- 📝 逐字歌词显示（支持增强 LRC 格式）
- 📖 内嵌歌词读取（基于 JAudioTagger）
- 🔊 ReplayGain 音量均衡
- 💬 状态栏歌词（Lyricon 词幕模块）
- 📱 魅族 Ticker 歌词通知
- 🎨 亮色/暗色/跟随系统主题
- 📁 文件夹浏览模式
- 🔍 歌曲搜索
- 📀 专辑浏览

## 构建

```bash
git clone https://github.com/Kifranei/Ella.git
cd Ella
./gradlew assembleDebug
```

## 依赖

| 库 | 用途 |
|---|---|
| [Miuix](https://github.com/miuix-kmp/miuix) | MIUI/HyperOS 风格 UI 组件 |
| [ExoPlayer (Media3)](https://github.com/androidx/media) | 音频解码与播放 |
| [Lyricon](https://github.com/proify/lyricon) | 状态栏歌词扩展模块 |
| [JAudioTagger](https://github.com/ijabergern/jaudiotagger) | 音频标签读写 |
| [Coil](https://github.com/coil-kt/coil) | 图片加载 |
| [Backdrop](https://github.com/niclas/AndroidLiquidGlass) | 液态玻璃效果 |

## 致谢

- **Mimo-V2.5-Pro** — 主要开发
- Miuix — UI 组件库
- ExoPlayer — 媒体播放引擎
- Lyricon — 词幕适配
- JAudioTagger — 标签解析
- Coil — 图片加载

## 许可证

```
Copyright © 2026 Ella Music. All rights reserved.
```
