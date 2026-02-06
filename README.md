# Connect

A contacts-based reminder app for staying in touch. **Android** (Kotlin, Jetpack Compose) and **iOS** (Swift, SwiftUI) live in this repo. Connect helps you stay in touch with your contacts by scheduling reminders to reach out via call, message, or email.

## Features

### Core Functionality
- **Contact Management**: Pick contacts from your device or add custom contacts manually
- **Scheduled Reminders**: Set up reminders with customizable frequencies (daily, weekly, monthly, or custom days)
- **Multiple Contact Methods**: Support for phone calls, SMS messages, and email
- **Inbox View**: View connections organized by urgency (Past Due, Today, Upcoming) in the Inbox tab
- **All Connections**: View all scheduled connections in the All tab
- **Quick Actions**: Mark as contacted, call, message, or email directly from the connection list

### Visual Features
- **Visual Indicators**: Color-coded contacts based on last contact time:
  - 🟢 Green: Contacted within your reminder frequency (not overdue)
  - 🟡 Yellow: Overdue by up to one reminder period
  - 🔴 Red: Overdue by more than one reminder period or never contacted
- **Profile Pictures**: Display contact photos from your device contacts (with placeholder for contacts without photos)
- **Relative Time Display**: See when you last contacted someone in human-readable format (e.g., "2 days ago", "3 weeks ago")
- **Action Icons**: Call, message, and email icons aligned with their respective contact information rows
- **Animated Interactions**: Smooth animations for checkmark completion and navigation bar transitions
- **Phone Number Formatting**: Automatic formatting with dashes (XXX-XXX-XXXX) for better readability

### Additional Features
- **Birthday Tracking**: Display birthdays on connection cards with optional prompt on birthday
- **Connection Details**: View full connection details, edit, or delete connections
- **Contact Integration**: Deep link to view imported contacts in phone's Contacts app, or add custom connections to Contacts
- **Input Validation**: Phone number and email validation with error messages
- **Pull-to-Refresh**: Refresh connection lists by pulling down on Today or All tabs
- **Search Functionality**: Search connections by name, phone number, or email across Today and All tabs
- **Dark Mode**: Choose between System Default, Light, or Dark theme in Settings
- **Settings Screen**: Centralized settings with theme selection and app information
- **About Screen**: Built-in help and explanation of the app's purpose (accessible from Settings)
- **Splash Screen**: Beautiful welcome screen on app launch
- **Scrollable Forms**: Easy-to-use scrollable add/edit screens with toolbar save button

## Tech Stack

**Android:** Kotlin, Jetpack Compose, Material 3, Room, Hilt.  
**iOS:** Swift, SwiftUI (in `ios/` — see [ios/README.md](ios/README.md)).

- **Language**: Kotlin (Android) / Swift (iOS)
- **UI**: Jetpack Compose with Material 3 (Android) / SwiftUI (iOS)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Hilt
- **Database**: Room Persistence Library
- **Navigation**: Navigation Compose
- **Coroutines & Flow**: For asynchronous operations

## Architecture

The app follows a clean architecture pattern with clear separation of concerns:

- **Data Layer**: Room entities, DAOs, repositories, and mappers
- **Domain Layer**: Business models and logic
- **UI Layer**: Compose screens, ViewModels, and navigation

## Permissions

The app requires the following permissions:
- `READ_CONTACTS`: To access your device contacts and import contact information
- `CALL_PHONE`: To make calls directly from the app
- `SEND_SMS`: To send messages directly from the app (optional - messages can be sent via system apps without this permission)
- `CAMERA`: To take photos for contact avatars (optional - only requested when taking a photo)
- `POST_NOTIFICATIONS`: To show reminder notifications (Android 13+)
- `SCHEDULE_EXACT_ALARM`: To schedule exact alarm notifications
- `USE_EXACT_ALARM`: To use exact alarm scheduling

## Recent Updates

**Version 1.3.16** (Android & iOS — versions aligned)

- **[iOS]** Snooze indicator on cards; Inbox/All refresh after contact or snooze; accent color matches Android; white rounded splash; Last contacted row; snooze/complete on green cards; preferred method must match phone/email; Inbox loads on first launch.
- **[Android]** Preferred method auto-resets when invalid (e.g. Email selected then email cleared).

Full release history: **[CHANGELOG.md](CHANGELOG.md)**.

## Future Plans

- Birthday reminders and notifications
- Statistics and analytics
- Export/import connections
- Custom reminder patterns
- Contact groups/categories
- Reminder time picker (set specific time for notifications)

## Development

### Android

**Prerequisites:** Android Studio Hedgehog or later, JDK 17+, Android SDK 24+.

1. Clone the repository.
2. Open the project in Android Studio.
3. Sync Gradle files.
4. Run the app on an emulator or device.

Version: `versionName` in [app/build.gradle.kts](app/build.gradle.kts) (keep in sync with iOS).

### iOS

**Prerequisites:** Xcode 14+ (iOS 16+ deployment).

1. Clone the repository.
2. Open [ios/Connect.xcodeproj](ios/Connect.xcodeproj) in Xcode.
3. Select a simulator or device and run (⌘R).

See [ios/README.md](ios/README.md) for structure and details. Version: **MARKETING_VERSION** in the Xcode project (keep in sync with Android `versionName`).

## License

This project is private and proprietary.
