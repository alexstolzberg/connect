# Connect (iOS)

iOS version of Connect — full port of the Android app in the same repo.

## Open in Xcode

1. Open **`Connect.xcodeproj`** in Xcode (double-click from Finder or `File → Open` and choose this folder).
2. Select a simulator or device and run (⌘R).

## Structure

- **Connect/** — App source:
  - **Models/** — `ScheduledConnection`, `ConnectionMethod`, `ThemeMode`, `AllSortOrder`
  - **Utils/** — `TimeFormatter`, `ValidationUtils`
  - **Persistence/** — `ConnectionStore` (JSON file), `Preferences` (UserDefaults)
  - **Services/** — `NotificationService`, `ContactHelper` (call/message/email)
  - **ViewModels/** — Home, AddEdit, ConnectionDetails, Settings
  - **Theme/** — `DesignSystem` (Dimensions, ConnectColors)
  - **Views/** — Splash, MainTabView, Inbox, All, AddEdit, ConnectionDetails, Settings, About, SnoozeSheet, ConnectionRow

## Features (ported from Android)

- **Inbox** — Past Due / Today / Upcoming; search; pull to refresh; mark as contacted; snooze
- **All** — Full list with sort (A–Z, date soonest/latest); search
- **Add/Edit** — Name, phone, email, frequency, preferred method, next reminder date, notes; duplicate check
- **Connection details** — Call, Message, Email; mark as contacted; delete
- **Settings** — Theme (system/light/dark), notifications toggle, default reminder time; About
- **Notifications** — Local reminders (when enabled) at reminder time
- **Persistence** — Connections in Application Support; preferences in UserDefaults

## Design

`Theme/DesignSystem.swift` and `ConnectColors` align with Android’s Dimensions and ConnectionColors. Adjust as needed for future UI polish.

## Version

- **Marketing version:** 1.3.16 (in Xcode project build settings)
- Aligned with Android `versionName` in root [app/build.gradle.kts](../app/build.gradle.kts). Keep both in sync when releasing.
