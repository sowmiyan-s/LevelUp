# Level Up — Daily Work Logger, Focus Shield & AI Coach

**Level Up** is a Kotlin & Jetpack Compose Android application built around a bio-tech emerald & mint design system. It combines an interactive focus timer, distraction shield mode, 24-hour work timeline, 4-tier consistency calendar, and an AI Focus Coach powered by **Firebase AI Logic** (`gemini-3.8-flash`).

---

## Key Features

- **Hero AI Focus Coach (`Firebase.ai`)**: Live streaming focus briefings, a structured JSON **AI Day Plan** generator with 1-tap goal creation, and an interactive **Ask Coach** chat sheet.
- **Interactive Focus Ring Dial**: Drag-to-adjust duration dial (`5m–120m`), category presets, live `JetBrains Mono` timer, and quick **Stop & Log** workflow.
- **Focus Shield Mode**: Configurable list of distraction apps locked behind an immersive Focus Shield overlay with an Emergency Unlock confirmation dialog.
- **Essential & Optional Bonus Goals**: Daily completion ratio tracking ($R$) that unlocks **Super Day** status when all Essential and Optional goals are completed.
- **24-Hour Time-Block Timeline**: Chronological session log with `#tags` and an expandable/editable **Sub-Notes & Deliverables** drawer.
- **Globalized 4-Tier Calendar & Analytics**: Monthly consistency grid (Super Day, Target Achieved, Partial Progress, Needs Attention, Streak Freeze), 7-day focus velocity chart, and category breakdown.
- **Google Sign-In & Cloud Sync**: Authenticated via Jetpack `CredentialManager` and synced in real time with Cloud Firestore.

---

## Tech Stack

- **Language & UI**: Kotlin, Jetpack Compose, Material Design 3, Navigation Compose
- **Architecture**: MVVM (`ViewModel` + `StateFlow`), Repository pattern, Kotlin Coroutines & Flow
- **Backend & AI**: Firebase Authentication (Google Sign-In), Cloud Firestore (Enterprise named database), Firebase AI Logic (`gemini-3.8-flash`), Firebase App Check
- **Build**: Gradle Kotlin DSL (`compileSdk = 36`, `minSdk = 24`, `targetSdk = 36`, Java 11)

---

## How to Run the App

### 1. Run in Google AI Studio
The project is pre-provisioned for AI Studio:
1. Open the app preview in the **Streaming Android Emulator**.
2. Sign in with your Google account on the welcome screen to start syncing goals, sessions, and AI briefings.

### 2. Run Locally in Android Studio
#### Prerequisites
- **Android Studio** (supporting AGP 9.x / Gradle 9.x)
- **JDK 11** or higher
- **Android SDK 36** (`minSdk 24`)
- An **Android Emulator or physical device with Google Play Services** (required for Google Sign-In via Credential Manager)

#### Setup & Launch
1. **Open the Project**:
   Launch Android Studio and select **File > Open...**, then choose the root project directory.
2. **Verify Firebase Configuration**:
   - Ensure `app/google-services.json` is present in the `app/` module.
   - Ensure `app/src/main/res/values/firebase_applet_config.xml` defines `firestore_database_id` (and `recaptcha_site_key` for release builds).
   - If building on a different machine with a new debug keystore, add your local debug SHA-1 and SHA-256 fingerprints to your Android app in the Firebase Console so Google Sign-In succeeds.
   - No `GEMINI_API_KEY` is needed — Gemini requests are authenticated automatically via Firebase AI Logic and App Check.
3. **Sync & Run**:
   - Wait for Gradle sync to finish.
   - Select an Android emulator/device with Google Play Services and click **Run (`Shift + F10`)**.

### 3. Build & Test from the Command Line

> Use `gradle` directly (or the Gradle installation in your environment).

- **Compile Kotlin & Debug APK**:
  ```bash
  gradle :app:assembleDebug
  ```
- **Install Debug APK on a Connected Device/Emulator**:
  ```bash
  gradle :app:installDebug
  ```
- **Run Local JVM Unit Tests**:
  ```bash
  gradle :app:testDebugUnitTest
  ```

### 4. Run Firestore Security Rules Tests (Optional)
Requires **Firebase CLI** and **Node.js**:
1. Start the local Firestore emulator on port `8085`:
   ```bash
   firebase emulators:start --only firestore --project demo-no-project
   ```
2. In another terminal, execute the rules test suite:
   ```bash
   FIRESTORE_EMULATOR_HOST="127.0.0.1:8085" node --test firestore.test.js
   ```
