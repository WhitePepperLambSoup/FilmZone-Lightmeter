# FilmZone 胶片专业测光表 (FilmZone Lightmeter)

[English](README.md) | [简体中文](README.zh-CN.md)

**一款专为胶片摄影师、电影摄影师和机械相机爱好者量身打造的高精度、具象化触觉测光表**。深度集成了安塞尔·亚当斯（Ansel Adams）经典分区曝光系统（Zone System）、40+ 款经典胶卷倒易律失效长曝光补偿计算、以及经典徕卡双影黄斑测距与实时景深（DoF）光学计算引擎。

---

## ✨ 核心特性

1. **安塞尔·亚当斯分区曝光系统（Zone System）** — 实时对画面进行 0 区至 X 区灰阶映射与动态范围分析。精准识别带纹理阴影（Zone III）、18% 中性灰（Zone V）与高光细节（Zone VII）。基于用户设定的目标分区位置，实时动态调整预览亮度，直观展示胶片成片明暗关系。
2. **40+ 款经典胶片数据库与倒易律失效计算** — 内置柯达（Portra 160/400/800, Tri-X 400, Gold 200, Ektar 100）、富士（Provia 100F, Velvia 50, Superia 400）、伊尔福（HP5 Plus, Delta 100/400/3200, FP4 Plus）等胶片光学曲线，自动计算夜景与长曝光下的倒易律失效补偿秒数与档位。
3. **徕卡双影黄斑测距与实时景深（DoF）计算** — 复刻经典徕卡/康泰时双影重合对焦框 UI。通过 7 帧滑动中值滤波过滤 Camera2 马达抖动，杜绝低反差白墙误判无穷远。根据当前画幅、焦距与实拍光圈，实时计算物距、超焦距（Hyperfocal）与前后景深清晰范围。
4. **机械触感旋钮与震动反馈** — 模拟机械胶片机手感，1/3 EV 步进无级精细调节。支持光圈范围（f/0.7 ~ f/64）、快门速度（1/8000s ~ 3600s / 1小时）与感光度（ISO 6 ~ 25600）。
5. **全画幅与中大画幅取景框线** — 精准等效视角模拟，支持 135 全画幅、120 中画幅（645, 6x6, 6x7, 6x9, 6x12, 6x17）、大画幅座机（4x5, 5x7, 8x10）及 XPan 宽幅电影比例（65:24）。
6. **多点测光与光比反差测量** — 支持多点采样记忆，实时计算高光与阴影间的光比与反差跨度（Stops）。
7. **100% 本地运算、无广告与隐私保护** — 无任何广告、无第三方追踪 SDK、完全离线运行。所有图像计算均在端侧芯片内存中实时完成，绝不拍摄、保存或上传任何用户图像。

---

## 🛠️ 技术架构

| 模块层级 | 所用技术 |
|---|---|
| **操作系统** | Android 7.0+ (API 24 ~ API 36) |
| **界面层** | Jetpack Compose + Material 3（高反差暗黑专业摄影美学） |
| **相机通道** | CameraX + Camera2 Interop（YUV_420_888 内存亮度流分析） |
| **光学算法** | 自研实时光度学矩阵分析、分区灰度映射与景深公式计算引擎 |
| **架构规范** | 现代 Android 架构（MVVM + Kotlin Coroutines + StateFlow） |
| **国际化** | 内置 10 种语言（简体中文、繁体中文、英语、日语、德语、法语、韩语等） |

---

## 🚀 源码编译与运行

### 环境要求
- Android Studio Ladybug / Koala 或更高版本
- JDK 17 / JDK 21（推荐使用 Android Studio 内置 JBR）
- Android SDK (compileSdk 36, minSdk 24)

### 编译命令
```bash
# 1. 克隆代码仓库
git clone https://github.com/WhitePepperLambSoup/FilmZone-Light-Meter.git
cd FilmZone-Light-Meter

# 2. 编译调试版 APK
./gradlew assembleDebug

# 3. 编译 Google Play 官方上架包 (.aab)
./gradlew bundleRelease
```

---

## 🔒 隐私政策

FilmZone 胶片专业测光表是一款完全离线的纯本地工具，**绝不收集、保存或上传任何用户数据与图像**。  
详情请查看项目内 [PRIVACY_POLICY.md](PRIVACY_POLICY.md) 或在线访问 [GitHub Gist 隐私声明](https://gist.github.com/WhitePepperLambSoup/d0ad397f6b4f1a48dbaa164eb7afbce9)。

---

## 📄 开源许可证

本项目基于 MIT 许可证开源 - 详见 [LICENSE](LICENSE) 文件。
