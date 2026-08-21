# Privacy Policy for FilmZone Lightmeter

**Effective Date:** August 22, 2026  
**Last Updated:** August 22, 2026  

FilmZone Lightmeter ("we", "our", or "the App") is developed and maintained as a professional exposure and light metering utility tool for analog film and digital photography. We are fully committed to protecting your personal privacy. This Privacy Policy explains how our App operates and our commitment to not collecting your personal data.

---

## 1. Zero Personal Data Collection

**FilmZone Lightmeter does NOT collect, store, track, transmit, or share any personal information or device identifiers.**

- We do NOT require user account creation, registration, or login.
- We do NOT integrate any third-party advertising SDKs, tracking pixels, or data broker libraries.
- We do NOT record, upload, or sell your device identifiers (e.g., IMEI, Android ID, IP address, GPS location).

---

## 2. Device Permissions and How They Are Used

The App requests the following minimal Android system permissions strictly for on-device, real-time photographic measurement functionality:

### A. Camera (`android.permission.CAMERA`)
- **Purpose**: Used exclusively to provide a real-time camera viewfinder, compute real-time optical luminance values (EV100 / Lux), analyze scene dynamic range (Ansel Adams Zone System), and calculate autofocus subject distance for depth-of-field estimation.
- **Data Handling**: All image frames from Camera2 / CameraX are processed **in-memory locally on your device** in real-time. **No photos or videos are captured, saved to disk, or transmitted across the internet.**

### B. Vibration (`android.permission.VIBRATE`)
- **Purpose**: Provides tactile haptic feedback when turning rotary adjustment dials (Aperture, Shutter Speed, ISO), tapping spot metering points, or switching film profiles.
- **Data Handling**: Haptic feedback runs purely locally on hardware and involves no data collection.

---

## 3. Local Storage & Preferences

The App stores your user settings (such as selected Film Stock, ISO, Custom Calibration Offsets, Aperture/Shutter priorities, and UI preferences) locally on your device using standard Android `SharedPreferences`.

- This data remains **100% on your device**.
- We have no access to your locally saved settings.
- Uninstalling the App or clearing app data will delete all locally stored preferences.

---

## 4. Third-Party Services and Analytics

FilmZone Lightmeter does NOT use any third-party analytics services (such as Firebase Analytics, Google Analytics, Facebook SDK, or Flurry). The App functions fully offline without requiring an active internet connection.

---

## 5. Children's Privacy (COPPA Compliance)

Our App does not address anyone under the age of 13, nor do we knowingly collect personally identifiable information from children. Because the App collects no data from any user, it is fully compliant with the Children's Online Privacy Protection Act (COPPA).

---

## 6. GDPR & CCPA Compliance

Because FilmZone Lightmeter does not collect, process, or transmit personal data, there is no personal information to access, rectify, delete, or export under the General Data Protection Regulation (GDPR) or the California Consumer Privacy Act (CCPA).

---

## 7. Changes to This Privacy Policy

We may update our Privacy Policy from time to time to reflect potential new features. Any updates will be posted on this page with a revised "Last Updated" date.

---

## 8. Contact Us

If you have any questions, suggestions, or concerns regarding this Privacy Policy, please feel free to reach out:

- **Developer / Team**: FilmZone Team
- **Email**: support@filmzone.app

---

# 隐私权政策（中文版）

**生效日期：** 2026年8月22日  
**最近更新：** 2026年8月22日  

FilmZone 胶片专业测光表（以下简称“本应用”）是一款专为胶片及数码摄影师打造的专业测光工具。我们高度重视用户的隐私安全。本隐私政策旨在向您说明本应用如何使用设备权限以及我们绝不收集用户个人信息的承诺。

### 1. 绝不收集个人信息
本应用**不收集、不存储、不跟踪、不上报、不共享**任何用户的个人身份信息、设备识别码或网络数据。本应用无需注册登录，无任何广告 SDK，无任何第三方数据统计分析组件。

### 2. 系统权限使用说明
- **相机权限 (`CAMERA`)**：仅用于实时取景、场景亮度计算（EV 值/照度）、亚当斯分区曝光分析以及镜头物距与景深测算。**所有取景画面仅在手机本地芯片内存中实时处理，绝不拍摄、保存或上传任何图像与视频。**
- **震动权限 (`VIBRATE`)**：仅用于模拟机械齿轮旋钮调节、档位切换时的触感马达震动反馈。

### 3. 数据本地化存储
用户的个性化设置（如 ISO、胶片库预设、测光标定值等）均仅保存在手机本地沙盒（SharedPreferences）中，离线可用，绝不上传至任何云端服务器。
