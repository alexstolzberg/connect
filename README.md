# Connect

A contacts-based reminder app for Android built with Kotlin and Jetpack Compose. Connect helps you stay in touch with your contacts by scheduling reminders to reach out via call or message.

## Features

- **Contact Management**: Pick contacts from your device or add custom contacts manually
- **Scheduled Reminders**: Set up reminders with customizable frequencies (daily, weekly, monthly, or custom days)
- **Smart Notifications**: View connections due today and all scheduled connections in separate tabs
- **Visual Indicators**: Color-coded contacts based on last contact time:
  - 🟢 Green: Recently contacted (within 50% of reminder frequency)
  - 🟡 Yellow: Medium time since contact (within 100% of reminder frequency)
  - 🔴 Red: Long time since contact (over 100% of reminder frequency or never contacted)
- **Quick Actions**: Mark as contacted, call, or message directly from the connection list
- **Relative Time Display**: See when you last contacted someone in human-readable format (e.g., "2 days ago", "3 weeks ago")
- **Birthday Tracking**: Optional birthday field for future birthday reminder features
- **Connection Details**: View full connection details, edit, or delete connections

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material 3
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
- `READ_CONTACTS`: To access your device contacts
- `CALL_PHONE`: To make calls directly from the app
- `SEND_SMS`: To send messages directly from the app

## Future Plans

- iOS version using Compose Multiplatform
- Birthday reminders
- Search and filter functionality
- Statistics and analytics
- Export/import connections
- Custom reminder patterns

## Development

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17 or later
- Android SDK 24+

### Building

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Run the app on an emulator or device

## License

This project is private and proprietary.
