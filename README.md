
# AlexLiveStreamRTMP

本專案示範如何在 Android 上使用 **Jetpack Compose + pedroSG94 RTMP Library** 實作：

* RTMP 推流
* Camera2 OpenGL 預覽
* 無黑邊全螢幕預覽
* Hilt DI
* Compose UI 控制列
* 完整狀態管理（MVI / StateFlow）

---

## ✨ 功能特色

* RTMP 推流：支援任何 RTMP Server（SRS、Nginx-RTMP、Wowza）
* Camera2 + OpenGlView 預覽（保持解析度、支援濾鏡）
* 無黑邊滿版預覽（AspectRatioMode.FILL）
* 音訊 + 視訊編碼（AAC + H.264）
* 狀態管理（connecting / streaming / preview）
* Jetpack Compose UI
* Hilt 注入、ViewModel、StateFlow
* 支援 Android 8+

---

## 📦 專案結構

```
app/
 ├── MainActivity.kt              # 推流邏輯、Camera2 管理、ConnectChecker
 ├── StreamViewModel.kt           # 狀態管理（URL / Streaming / Preview）
 ├── StreamScreen.kt              # Compose UI + OpenGlView + 控制列
 ├── App.kt                       # Hilt Application
 ├── ui/theme/*                   # Material3 Theme
 └── AndroidManifest.xml          # 權限 + Activity 註冊
```

---

## 🛠️ 使用技術

* **Kotlin**
* **Jetpack Compose**
* **RtmpCamera2 / OpenGlView**（Pedro RTMP library）
* **Hilt**
* **StateFlow / ViewModel**
* **Material 3**
* **Camera2 + OpenGL**

---
## 📦 Dependencies

在 `build.gradle.kts` 中加入以下依賴：

```kotlin
dependencies {
    // --- Hilt ---
    implementation("com.google.dagger:hilt-android:2.57.2")
    ksp("com.google.dagger:hilt-compiler:2.57.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")

    // --- RTMP ---
    implementation("com.github.pedroSG94:rtmp-rtsp-stream-client-java:2.6.6")
}
```

---
## 📱 權限設定

AndroidManifest 已包含所需推流權限：


```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

---

## 🎬 MainActivity — 推流 / ConnectChecker

MainActivity 建立 RtmpCamera2 與 OpenGlView，接收預覽：


### ➤ 連線成功 / 失敗 / 推流狀態回傳

```kotlin
rtmpCamera2 = RtmpCamera2(
    view,
    object : ConnectChecker {
        override fun onConnectionStarted(url: String) {
            viewModel.onStreamConnecting()
        }

        override fun onConnectionSuccess() {
            viewModel.onStreamStarted()
        }

        override fun onConnectionFailed(reason: String) {
            viewModel.onStreamError(reason)
            rtmpCamera2?.stopStream()
        }

        override fun onDisconnect() {
            viewModel.onStreamStopped()
        }

        override fun onAuthError() {
            viewModel.onStreamError("Auth error")
        }

        override fun onAuthSuccess() {}
    }
)
```

---

## 🧩 StreamViewModel — 狀態管理（StateFlow）

完整管理推流/預覽狀態：


```kotlin
data class UiState(
    val rtmpUrl: String = "rtmp://192.168.0.140:1935/live/test",
    val isPreviewOn: Boolean = false,
    val isStreaming: Boolean = false,
    val isConnecting: Boolean = false,
    val errorMessage: String? = null
)
```

狀態更新使用 reducer：

```kotlin
private inline fun reduce(block: UiState.() -> UiState) {
    _uiState.update { current -> current.block() }
}
```

---

## 🎨 StreamScreen — Compose UI + OpenGlView 全螢幕無黑邊

UI 使用 Box 疊加：

* 底層：OpenGlView（滿版）
* 中間：控制列（RTMP URL + Start/Stop）
* 上層：錯誤訊息浮層

📌 **重要：使用 `AspectRatioMode.Fill` 去除黑邊**


```kotlin
AndroidView(
    modifier = Modifier.fillMaxSize(),
    factory = { context ->
        OpenGlView(context).apply {
            setAspectRatioMode(AspectRatioMode.Fill)
        }.also(onAttachOpenGlView)
    }
)
```

---

## ▶️ 推流流程

### START：

1. 檢查 URL
2. 檢查 Surface 是否 ready
3. prepareAudio() + prepareVideo()
4. startStream(url)
5. 更新 ViewModel（connecting → streaming）

### STOP：

1. stopStream()
2. stopPreview()
3. ViewModel 更新 isStreaming = false

---

## 💻 如何使用

### 1. 安裝 Server（SRS 最簡單）

```
git clone https://github.com/ossrs/srs
docker run --rm -it -p 1935:1935 -p 1985:1985 -p 8080:8080 ossrs/srs:latest
```

你的 RTMP URL 例如：

```
rtmp://<你的IP>:1935/live/test
```

### 2. 安裝 App

授權相機/麥克風後即可使用。

### 3. START

可看到相機畫面 → 推流中。

---

## 📌 待辦功能（Roadmap）

* [ ] Auto reconnect 推流
* [ ] 錄影本地 MP4
* [ ] 加入濾鏡
* [ ] 支援多解析度切換
* [ ] 加入前鏡頭/後鏡頭切換
* [ ] 推流統計（FPS / Bitrate / Drop Frames）

---

## 👤 Author

**Alex Yang**  
Android Engineer
🌐 [github.com/m9939418](https://github.com/m9939418)

