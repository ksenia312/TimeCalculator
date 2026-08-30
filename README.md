# Time Calculator

An Android app for planning your day and managing your time effectively. Build morning
(and any-time) routines, break them into timed tasks, and get notified so you stay on
schedule.

## Features

- **Routines & tasks** — create routines, split them into ordered tasks, and let the app
  calculate timings for your day.
- **Scheduling & reminders** — routine alarms and notifications (with exact-alarm
  permission handling) keep you on track.
- **Onboarding & authentication** — guided onboarding plus email/password sign-up and
  login backed by Supabase.
- **Settings** — customize app behavior to fit your workflow.

## Tech stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3, Navigation 3
- **DI:** Koin
- **Local storage:** Room
- **Backend:** Supabase (Auth + Postgrest) via Ktor
- **Async:** Kotlin Coroutines, kotlinx-datetime / serialization

## Architecture

The project follows a modular clean-architecture setup:

- `app` — application host, `MainActivity`, navigation wiring, scheduling.
- `domain` — models and business logic (routines, tasks, auth, schedules).
- `data` — repositories and data sources (Room, Supabase).
- `di` — Koin dependency-injection modules.
- `shared` — shared resources and utilities.
- `feature/*` — self-contained feature modules: `home`, `landing`, `onboarding`,
  `auth`, `routineslist`, `routineeditor`, `routinescreen`, `taskslist`, `taskeditor`,
  `settings`.

> Requires Android SDK 30+ (targets SDK 36).