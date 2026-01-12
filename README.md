# Connect

A contacts-based reminder app for Android built with Kotlin and Jetpack Compose. Connect helps you stay in touch with your contacts by scheduling reminders to reach out via call, message, or email.

## Features

### Core Functionality
- **Contact Management**: Pick contacts from your device or add custom contacts manually
- **Scheduled Reminders**: Set up reminders with customizable frequencies (daily, weekly, monthly, or custom days)
- **Multiple Contact Methods**: Support for phone calls, SMS messages, and email
- **Smart Notifications**: View connections due today and all scheduled connections in separate tabs
- **Quick Actions**: Mark as contacted, call, message, or email directly from the connection list

### Visual Features
- **Visual Indicators**: Color-coded contacts based on last contact time:
  - 🟢 Green: Recently contacted (within 50% of reminder frequency)
  - 🟡 Yellow: Medium time since contact (within 100% of reminder frequency)
  - 🔴 Red: Long time since contact (over 100% of reminder frequency or never contacted)
- **Profile Pictures**: Display contact photos from your device contacts (with placeholder for contacts without photos)
- **Relative Time Display**: See when you last contacted someone in human-readable format (e.g., "2 days ago", "3 weeks ago")
- **Conditional Icons**: Only show contact method buttons (call/message/email) when that method is available

### Additional Features
- **Birthday Tracking**: Optional birthday field for future birthday reminder features
- **Connection Details**: View full connection details, edit, or delete connections
- **About Screen**: Built-in help and explanation of the app's purpose
- **Splash Screen**: Beautiful welcome screen on app launch
- **Scrollable Forms**: Easy-to-use scrollable add/edit screens

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
- `READ_CONTACTS`: To access your device contacts and import contact information
- `CALL_PHONE`: To make calls directly from the app
- `SEND_SMS`: To send messages directly from the app (optional - messages can be sent via system apps without this permission)

## Recent Updates

### Version 1.1 (Latest)
- ✨ Added splash screen with app branding
- ✨ Added About tab with app explanation and usage guide
- ✨ Email support as a contact method
- ✨ Profile picture display in connection items
- ✨ Conditional contact method icons (only show available methods)
- ✨ Scrollable add/edit connection screen
- 🐛 Fixed nullable field handling throughout the app
- 🎨 Improved UI formatting and visual hierarchy
- 📱 Database schema updated to version 3

### Version 1.0
- Initial release with core reminder functionality
- Color-coded contact indicators
- Relative time formatting
- Mark as contacted feature
- Birthday field support

## Future Plans

- iOS version using Compose Multiplatform
- Push notifications for reminders
- Birthday reminders and notifications
- Search and filter functionality
- Statistics and analytics
- Export/import connections
- Custom reminder patterns
- Contact groups/categories

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
