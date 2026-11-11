<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Fitness Tracker App

This project contains everything you need to run the **Fitness Tracker** (MM Fitness) app locally.

- **GitHub Repository**: [https://github.com/omsoni21/fitnesstracker](https://github.com/omsoni21/fitnesstracker)
- **Download APK**: [https://github.com/omsoni21/fitnesstracker/raw/main/apk/app-debug.apk](https://github.com/omsoni21/fitnesstracker/raw/main/apk/app-debug.apk)
- **View your app in AI Studio**: [https://ai.studio/apps/5ef7b5e7-5d5d-4b5d-9148-d0d1da2bf209](https://ai.studio/apps/5ef7b5e7-5d5d-4b5d-9148-d0d1da2bf209)

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)

1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device
7. If you have already published your app in AI Studio, please [request upload key reset](https://support.google.com/googleplay/android-developer/answer/9842756#zippy=%2Crequest-an-upload-key-reset) in Google Play Console.

## APK Location
After building with `./gradlew assembleDebug`, the generated APK is located at:
`app/build/outputs/apk/debug/app-debug.apk`

