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

`google-services.json` is intentionally excluded from this repository. You must either obtain the project's config file or connect a Firebase project of your own.

### Option A — Get the existing config file

If you are a contributor with access to the Firebase project, download `google-services.json` from the [Firebase Console](https://console.firebase.google.com):

1. Open the **exercist-cb7d9** project
2. Go to **Project Settings** (gear icon) → **Your apps**
3. Select the Android app (`com.bossinc.exercist`)
4. Click **Download google-services.json**
5. Place it at `app/google-services.json`

### Option B — Create your own Firebase project

Follow these steps to wire the app to a new Firebase project.

#### 1. Create the Firebase project

1. Go to [console.firebase.google.com](https://console.firebase.google.com) and click **Add project**
2. Name it anything (e.g. `exercist-dev`)
3. Disable Google Analytics if you don't need it, then click **Create project**

#### 2. Add the Android app

1. In the project overview, click the **Android** icon to add an app
2. Set the package name to `com.bossinc.exercist`
3. (Optional) Enter a nickname like `Exercist`
4. Click **Register app**
5. Download the `google-services.json` file
6. Place it at `app/google-services.json`
7. Skip the remaining steps in the wizard (Gradle setup is already done)

#### 3. Enable Authentication

1. In the Firebase Console, go to **Authentication** → **Sign-in method**
2. Click **Google** and toggle it **Enabled**
3. Set a support email address
4. Click **Save**

#### 4. Create the Firestore database

1. Go to **Firestore Database** → **Create database**
2. Choose **Start in production mode** (you will set rules in the next step)
3. Pick the region closest to your users and click **Enable**

#### 5. Set Firestore Security Rules

In **Firestore Database** → **Rules**, replace the default rules with:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Each user's workouts and templates are private
    match /users/{userId}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }

    // Exercises are readable by any signed-in user, writable by their creator
    match /exercises/{exerciseId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update, delete: if request.auth != null
                            && request.auth.uid == resource.data.createdBy;
    }
  }
}
```

Click **Publish**.

#### 6. (Recommended) Restrict the API key

The API key in `google-services.json` should be restricted to prevent unauthorized use:

1. Go to [console.cloud.google.com](https://console.cloud.google.com) → **APIs & Services** → **Credentials**
2. Find the API key used by the Android app (named something like *Android key (auto created by Firebase)*)
3. Under **Application restrictions**, choose **Android apps**
4. Add your package name (`com.bossinc.exercist`) and your debug signing certificate SHA-1:
   ```
   ./gradlew signingReport
   ```
5. Click **Save**

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
