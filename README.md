# 🏋️ MM Fitness Tracker (PulseFit)

An advanced, modern, AI-powered Android fitness and activity tracking application built with **Jetpack Compose**, **Material 3**, and **Google Gemini AI**.

[![Android Studio](https://img.shields.io/badge/Android%20Studio-2024.1+-brightgreen.svg)](https://developer.android.com/studio)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-1.7+-purple.svg)](https://developer.android.com/jetpack/compose)
[![APK Download](https://img.shields.io/badge/Download-APK%20v1.0-orange.svg)](./apk/mm-fitness-v1.0.apk)

---

## 📲 Direct APK Download

Download and install the ready-to-run Android APK directly on your phone:
- 📥 **[Download MM Fitness APK (v1.0)](./apk/mm-fitness-v1.0.apk)**

---

## 📱 App Screenshots

<div align="center">
  <table>
    <tr>
      <td align="center"><b>Home Dashboard</b></td>
      <td align="center"><b>Workout Routines</b></td>
      <td align="center"><b>Live Activity & Pedometer</b></td>
    </tr>
    <tr>
      <td><img src="./screenshots/home_screen.png" width="250"/></td>
      <td><img src="./screenshots/workouts_screen.png" width="250"/></td>
      <td><img src="./screenshots/activity_screen.png" width="250"/></td>
    </tr>
    <tr>
      <td align="center"><b>Analytics & Reports</b></td>
      <td align="center"><b>Settings & Preferences</b></td>
      <td align="center"><b>AI Recommendations</b></td>
    </tr>
    <tr>
      <td><img src="./screenshots/reports_screen.png" width="250"/></td>
      <td><img src="./screenshots/settings_screen.png" width="250"/></td>
      <td><img src="./screenshots/home_screen.png" width="250"/></td>
    </tr>
  </table>
</div>

---

## ✨ Features

- 🏃 **Real-time Pedometer & Step Tracker:** Physical accelerometer motion filtering with live step counter, stride distance, estimated calorie burn, and active time metrics.
- 💪 **Workout Routines & Plans:** Specialized muscle group targeting (Chest, Back, Legs, Arms, Cardio) with difficulty filters (Beginner, Intermediate, Advanced).
- 🤖 **AI-Powered Fitness Coaching:** Integrated Google Gemini AI (`firebase-ai`) for personalized workout insights, recovery tips, and goal suggestions.
- 📊 **Comprehensive Reports:** Monthly workout consistency calendars, daily activity charts, progress tracking, and goal achievement percentages.
- ⚙️ **Customizable Settings:** Daily step goal adjustments, user profile management, dark modern UI theme, and unit preference configuration.

---

## 🛠️ Tech Stack & Architecture

- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 Design
- **Language:** Kotlin 100%
- **Architecture:** Clean Architecture + MVVM + Unidirectional Data Flow (UDF)
- **Async Programming:** Kotlin Coroutines & Flow
- **Local Storage:** Room Database & DataStore Preferences
- **Networking:** Retrofit2 & Moshi Codegen
- **AI Integration:** Firebase AI / Google Gemini API
- **Build System:** Gradle Kotlin DSL (`build.gradle.kts`) with Version Catalog (`libs.versions.toml`)

---

## 🚀 Getting Started (Build from Source)

### Prerequisites
- [Android Studio Ladybug (2024.2.1+) or newer](https://developer.android.com/studio)
- JDK 11 or JDK 17
- Android SDK 35/36

### Setup Steps
1. **Clone the repository:**
   ```bash
   git clone https://github.com/omsoni21/fitnesstracker.git
   cd fitnesstracker
   ```

2. **Open in Android Studio:**
   - Launch Android Studio and click **Open**.
   - Select the `fitnesstracker` directory.

3. **Configure Environment Variables:**
   - Copy `.env.example` to `.env`:
     ```bash
     cp .env.example .env
     ```
   - Add your Gemini API key to `.env` if using AI coaching features:
     ```env
     GEMINI_API_KEY=your_api_key_here
     ```

4. **Run the Project:**
   - Select your emulator or physical Android device.
   - Click the **Run** button (▶️) or press `Shift + F10`.

---

## 📦 Generating APK

To generate a debug APK locally via terminal:
```bash
./gradlew :app:assembleDebug
```
The generated APK will be placed at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🔗 Repository

- **GitHub Repository:** [https://github.com/omsoni21/fitnesstracker.git](https://github.com/omsoni21/fitnesstracker.git)

---
Developed with ❤️ by Om Soni
