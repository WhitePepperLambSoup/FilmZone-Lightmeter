# FilmZone Lightmeter (胶片专业测光表)

[English](README.md) | [简体中文](README.zh-CN.md)

**A professional, tactile lightmeter and exposure utility for Android**, tailored for analog film photographers, cinematographers, and vintage camera lovers. Integrates Ansel Adams' classic Zone System, 40+ film profiles with reciprocity failure compensation, and a Leica-style rangefinder with real-time Depth-of-Field (DoF) calculation.

---

## ✨ Highlights

1. **Ansel Adams Zone System Integration** — Real-time dynamic range analysis (Zone 0 through Zone X) mapping the camera stream to natural gray levels. Pinpoint textured shadows (Zone III), 18% middle gray (Zone V), and textured highlights (Zone VII). Real-time exposure preview shader adapts smoothly based on user zone placement.
2. **40+ Analog Film Profiles & Reciprocity Failure** — Built-in database covering Kodak (Portra 160/400/800, Tri-X 400, Gold 200, Ektar 100), Fujifilm (Provia 100F, Velvia 50, Superia 400), Ilford (HP5 Plus, Delta 100/400/3200, FP4 Plus), and more. Calculates exact reciprocity failure compensation and long-exposure time additions.
3. **Leica Rangefinder Coincidence Reticle & Live DoF** — Precision split-image coincidence box with 7-frame rolling median filtering on Camera2 autofocus diopters. Eliminates low-contrast false infinity anomalies. Dynamically computes Hyperfocal distance ($H$) and Depth-of-Field near/far limits based on film format, focal length, and active aperture.
4. **Physical Tactile Controls & Sound** — Smooth rotary dials with hardware haptic vibration feedback for 1/3 EV step increments on Aperture (f/0.7 ~ f/64), Shutter Speed (1/8000s ~ 3600s), and ISO (6 ~ 25600).
5. **Multi-Format Viewfinder Framelines** — Accurate optical crop and aspect ratio framelines for 135 Full Frame, 120 Medium Format (6x4.5, 6x6, 6x7, 6x9, 6x12, 6x17), Large Format (4x5, 5x7, 8x10), and Cinema/Panoramic (XPan 65:24, 16:9, 2.39:1).
6. **Multi-Spot Evaluative Contrast Metering** — Multi-spot memory with live delta EV and contrast range measurement between key shadows and highlights.
7. **100% Offline, Ad-Free & Privacy First** — Zero advertisements, zero third-party tracking SDKs, and zero internet requests. All image processing and optical analysis execute in-memory on the device.

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| **Platform** | Android 7.0+ (API 24 ~ API 36) |
| **UI Framework** | Jetpack Compose + Material 3 (Dark High-Contrast Aesthetic) |
| **Camera Pipeline** | CameraX + Camera2 Interop (YUV_420_888 In-Memory Luminance Stream) |
| **Optical Math** | Custom Real-Time Evaluative Photometric & DoF Optical Math Engine |
| **Architecture** | Modern Android Architecture (MVVM + Kotlin Coroutines + StateFlow) |
| **Localization** | 10 Built-in Languages (Simplified/Traditional Chinese, English, Japanese, German, etc.) |

---

## 🚀 Getting Started & Build Instructions

### Prerequisites
- Android Studio Ladybug / Koala or newer
- JDK 17 / JDK 21 (bundled with Android Studio JBR)
- Android SDK (compileSdk 36, minSdk 24)

### Clone & Compile
```bash
# 1. Clone repository
git clone https://github.com/WhitePepperLambSoup/FilmZone-Lightmeter.git
cd FilmZone-Lightmeter

# 2. Build Debug APK
./gradlew assembleDebug

# 3. Build Google Play Release Bundle (.aab)
./gradlew bundleRelease
```

---

## 🔒 Privacy Policy

FilmZone Lightmeter operates strictly offline and does **not** collect, store, or transmit any user data, images, or telemetry.  
View our full [Privacy Policy](PRIVACY_POLICY.md) or online via [GitHub Gist](https://gist.github.com/WhitePepperLambSoup/d0ad397f6b4f1a48dbaa164eb7afbce9).

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
