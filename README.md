# Timer

An Android screen-time control app that tracks how long you use each app and blocks apps once you reach your limits.

> **Note:** This is a self-directed learning project. I designed the app idea myself and built it to learn Kotlin and modern Android development. The code is not production-ready (see [Known limitations](#known-limitations)).

## Features

- **Daily limit:** set a total daily screen-time budget. When it runs out, apps are blocked.
- **App categories**
  - **Leech:** an app with its own individual time limit.
  - **Joker:** an app that is never blocked.
  - **NoCount:** an app whose usage does not count toward the daily total.
- **Synergy rules:** if every app in a group reaches its individual limit, a penalty is subtracted from the daily limit.
- **Coupons:** temporarily unlock everything for 15, 20 or 30 minutes. Two coupons are generated each week, and more can be redeemed through a signed deep link (`timer://coupon?duration=..&nonce=..&sig=..`) verified with HMAC-SHA256 and protected against replay with a nonce.
- **Block screen:** opening a blocked app shows a "time's up" screen with the option to burn a coupon.

## How it works

Two background services share their state through `SharedPreferences`:

- `UsageTrackingService` is a foreground service that runs every 60 seconds. It reads real usage from `UsageStatsManager`, computes the remaining time and stores whether apps should be blocked.
- `TimerAccessibilityService` listens for window changes to detect which app is opening. If that app should be blocked, it launches `BlockedActivity` on top of it.

## Tech stack

- Kotlin
- Jetpack Compose + Material 3
- MVVM with the Repository pattern
- Room (SQLite)
- DataStore (settings)
- Hilt (dependency injection)
- Kotlin Coroutines and Flow
- Navigation Compose
- Gradle (Kotlin DSL)

## Project structure

```
app/src/main/
├── AndroidManifest.xml
├── res/                          # strings, theme, icons, accessibility service config
└── java/com/timer/app/
    ├── TimerApplication.kt       # app entry point (Hilt setup)
    ├── MainActivity.kt           # main screen + deep link handling
    ├── presentation/             # Compose screens, ViewModels, navigation, theme
    ├── domain/model/             # plain data models
    ├── data/                     # Room entities/DAOs, DataStore, repositories
    ├── service/                  # tracking service, accessibility service, boot receiver
    └── di/                       # Hilt modules
```

## Getting started

1. Clone the repository.
2. Open it in Android Studio.
3. Let Gradle sync, then run the `app` module on a device or emulator (min SDK 26).
4. Grant the permissions the app asks for:
   - **Usage access** (to read app usage)
   - **Accessibility service** (to detect and block apps)
   - **Notifications** (for the tracking notification)

## Known limitations

- Usage is refreshed every 60 seconds, so blocking can lag by up to a minute.
- The coupon HMAC secret has a placeholder default in `SettingsDataStore.kt`. Because HMAC is symmetric, a real secret embedded in the app could be extracted from the APK.
- There are no automated tests yet.
