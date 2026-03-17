# Exercist — Exercise Tracking App

## Overview
A mobile app for logging weight training workouts, tracking progress over time, and building consistent exercise habits.

---

## Core Features

### 1. Workout Logging
- Log exercises by type
- Record sets, reps, weight, duration, and mood (feeling strong, weak, tired)
- Timer/stopwatch for rest periods and timed exercises

### 2. Exercise Library
- Searchable database of exercises with muscle group tags
- Custom exercise creation
- Exercise detail view with instructions and target muscles

### 3. Workout Templates
- Create reusable workout routines
- Start a workout from a template
- Duplicate and modify existing templates

### 4. Progress Tracking
- Personal records (PRs) automatically tracked per exercise
- Volume and frequency charts over time
- Body weight log (optional)
- Workout history and calendar view

---

## Data Model

- **User**: profile, preferences, body weight history
- **Exercise**: name, category, muscle groups, instructions
- **WorkoutTemplate**: name, ordered list of exercise configs
- **WorkoutSession**: date, duration, notes, list of exercise entries
- **ExerciseEntry**: exercise reference, sets (reps/weight/duration)
- **PersonalRecord**: exercise reference, value, date achieved

---

## Screens

1. **Home** — today's plan, recent activity, quick-start workout
2. **Log Workout** — active workout session with live entry
3. **History** — calendar and list of past sessions
4. **Exercises** — browse/search the exercise library
5. **Templates** — saved workout routines
7. **Profile/Settings** — goals, notifications

---

## Tech Stack (Android)

- **Language**: C++
- **UI**: Jetpack Compose
- **Architecture**: MVVM + Repository pattern
- **Database**: Firebase Firestore (cloud NoSQL, offline cache included)
- **Auth**: Firebase Authentication (Google Sign-In or email/password)
- **DI**: Hilt
- **Navigation**: Compose Navigation
- **Charts**: Vico or MPAndroidChart

### Firestore Notes
- Each user's data lives under `users/{userId}/` — workouts, templates, and PRs are subcollections
- Firestore's offline persistence means the app works without a connection and syncs when back online
- Exercise library can be a shared top-level collection (`exercises/`) readable by all users

---

## Milestones

1. Project setup, Firebase config, basic navigation, auth flow
2. Exercise library (browse, search, custom)
3. Workout logging (session flow, sets/reps entry)
4. Workout templates
5. History and calendar view
6. Progress charts and PRs
7. Goals and notifications
8. Polish, onboarding, settings
