# Exercist

An Android workout tracking app built with Kotlin, Jetpack Compose, and Firebase.

## Features

- Log workouts with exercises, sets, reps, and weight
- Track workout history and session details
- Create and reuse workout templates
- Google Sign-In authentication

## Tech Stack

- Kotlin + Jetpack Compose
- Firebase Authentication (Google Sign-In)
- Cloud Firestore
- Hilt (dependency injection)
- Navigation Compose

---

## Firebase Setup (required before building)

`google-services.json` is intentionally excluded from this repository. Download it from the Firebase Console and place it at `app/google-services.json`.

1. Open the **exercist-cb7d9** project in the [Firebase Console](https://console.firebase.google.com)
2. Go to **Project Settings** (gear icon) → **Your apps**
3. Select the Android app (`com.bossinc.exercist`)
4. Click **Download google-services.json**
5. Place it at `app/google-services.json`

---

## Building the App

1. Clone the repo
2. Follow the Firebase setup above and place `google-services.json` at `app/google-services.json`
3. Open the project in Android Studio
4. Run on a device or emulator (API 28+)

## Running Tests

```bash
./gradlew :app:testDebugUnitTest
```
