# SleepAnalyzerLiteAndroid

> 💤 Android sleep tracking app using real-time heart rate sensors (Polar Verity Sense, Polar OH1+, etc.).

SleepAnalyzerLiteAndroid is an open-source app that collects heart rate data during sleep and visualizes hypnograms, sleep phases, and long-term statistics.

It integrates with BLE sensors from **Polar** to provide detailed insight into sleep architecture and heart performance overnight.

---

## 📲 App Screenshots & Features

### 🟢 Tracking Mode
Live visualization of heart rate stream, sensor status, and circular hypnogram clock.
<div align="center">
    <img src="Screenshots/sa_tracking.png" alt="tracking" width="300"/>
</div>

### 📊 Report Mode
Displays day-by-day analysis: min/avg/max heart rate and stacked hypnogram durations.
<div align="center">
    <img src="Screenshots/sa_report.png" alt="report" width="300"/>
</div>

### 📁 Archive Mode
Scroll through previous nights with visual summaries of sleep structure.
<div align="center">
    <img src="Screenshots/sa_archive.png" alt="archive" width="300"/>
</div>

### 🔍 Archive Detail View
Sleep phase timeline + heart rate chart for selected night.
<div align="center">
    <img src="Screenshots/sa_hypnogram.png" alt="hypnogram" width="300"/>
</div>

### 🧪 Debug Mode
Visual overlays of sleep phase predictions and adjustable quantization parameters.
<div align="center">
    <img src="Screenshots/sa_hypnogram_debug_mode.png" alt="hypnogram debug mode" width="300"/>
</div>

---

## 🛠 Technologies
- Jetpack Compose UI
- Dagger Hilt
- [Polar BLE SDK](https://github.com/polarofficial/polar-ble-sdk/tree/master)
- Custom circular hypnogram rendering
- Binary sleep computation module via `HypnogramComputation.aar`

---

## 📄 License
MIT © Igor Gun
