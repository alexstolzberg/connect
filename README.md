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
- `POST_NOTIFICATIONS`: To show reminder notifications (Android 13+)
- `SCHEDULE_EXACT_ALARM`: To schedule exact alarm notifications
- `USE_EXACT_ALARM`: To use exact alarm scheduling

## Recent Updates

### Version 1.3.1 (Latest)
- 🏗️ Refactored ConnectionItem into separate composable file for better code organization
- 🐛 Fixed weight modifier issue in ConnectionItem layout
- 🐛 Fixed FlowColumnScopeInstance compilation error
- 🎨 Improved code maintainability by extracting reusable components

### Version 1.3
- ✨ Dark mode support with theme selection (System Default, Light, Dark)
- ✨ Settings screen replacing About tab
- ✨ Search functionality on Today and All tabs (search by name, phone, or email)
- ✨ About screen accessible from Settings
- 🐛 Fixed birthday date picker timezone issue (dates now display correctly)
- 🐛 Fixed various icon reference issues in Settings screen
- 🎨 Improved navigation structure with Settings as main tab

### Version 1.2
- ✨ Push notifications when reminders are due
- ✨ Contact photo import from device contacts
- ✨ Deep link to view contacts in phone's Contacts app
- ✨ Add custom connections to phone's Contacts app
- ✨ Birthday display with date formatting on connection cards
- ✨ Phone number auto-formatting with dashes (XXX-XXX-XXXX)
- ✨ Phone number input validation (digits only)
- ✨ Email validation on input fields
- ✨ Pull-to-refresh on Today and All tabs
- 🎨 Color-coded card backgrounds (red/yellow/green based on contact status)
- 🎨 Improved card layout with aligned action icons
- 🎨 Animated checkmark with bounce effect when marking as contacted
- 🎨 Animated navigation bar indicator
- 🎨 Save button moved to toolbar with validation
- 🎨 Headers for Phone and Email fields on cards
- 🐛 Fixed clipping issues under toolbar
- 🐛 Fixed custom days input field handling
- 🐛 Fixed icon alignment on connection cards
- 📱 Database schema updated to version 4

### Version 1.1
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
- Birthday reminders and notifications
- Statistics and analytics
- Export/import connections
- Custom reminder patterns
- Contact groups/categories
- Reminder time picker (set specific time for notifications)

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
