# Connect

A contacts-based reminder app for Android built with Kotlin and Jetpack Compose. Connect helps you stay in touch with your contacts by scheduling reminders to reach out via call, message, or email.

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
- `CAMERA`: To take photos for contact avatars (optional - only requested when taking a photo)
- `POST_NOTIFICATIONS`: To show reminder notifications (Android 13+)
- `SCHEDULE_EXACT_ALARM`: To schedule exact alarm notifications
- `USE_EXACT_ALARM`: To use exact alarm scheduling

## Recent Updates

### Version 1.3.13 (Latest)
- ⏰ Default reminder time: Settings → Default reminder time (default 10:00 AM); used when a connection has no reminder time
- ⏰ Reminder time on Add/Edit: Per-connection reminder time with time picker; new connections start with default
- 🕐 Clock icon for Default reminder time in Settings
- 🧭 Tab switching: Fade transitions between Inbox/All/Settings (no horizontal swipe feel)
- 🎨 Preferred method pills: Grey border when disabled (Call/Message without phone, Email without email)
- 🧪 SettingsViewModelTest: Stub getDefaultReminderTime() for init

### Version 1.3.12
- 🎬 Slide transitions: Add/Edit screen slides up from bottom (modal-style) and down on dismiss/save; other screens use horizontal slide
- 📍 Snackbars pinned to bottom (above bottom nav when visible)
- ⌨️ Keyboard: Scroll gets extra bottom padding when keyboard is up (imePadding + adjustResize)
- 📋 Duplicate dialog shows which fields matched (Name, Phone, Email)
- 🌙 Dark mode: Single `isConnectDarkTheme()` from theme (no luminance/brightness); card text/icons use `ConnectionColors.OnCardDark`; snooze icon explicit tint
- 🎨 Connection card colors: Opaque dark-theme backgrounds; `OnCardDark` for all card content in dark mode
- 🧪 Tests updated: SettingsViewModelTest mocks notificationPreferences/connectionRepository; ConnectionRepositoryTest mocks 3-arg scheduleNotification

### Version 1.3.11
- 🔔 Push notifications: Right time (uses reminder time of day), tap opens contact details, message "This is a reminder to connect with [name]"
- 🔔 Notification title "It's time to connect!" and bell icon
- 🔔 Notifications off by default; first-launch prompt to enable (with permission request on Android 13+)
- 🔔 Settings toggle to turn reminder notifications on/off; permission handled accordingly
- 📝 Snackbar confirmations: Connection added, Connection updated, Connection deleted, Marked as contacted, Reminder snoozed
- 🎨 FAB and bottom nav use MaterialTheme.colorScheme.primary (match PillButtons)
- 🎨 Mark as Contacted button on connection details uses primary state
- 📄 Privacy policy updated for in-app notifications control

### Version 1.3.9
- ✅ Comprehensive test suite: Added unit tests for ViewModels, Repositories, DAOs, utilities, mappers, and domain models
- ✅ Test infrastructure: Added MockK, Turbine, Coroutines Test, Robolectric, and Room testing dependencies
- ✅ Test coverage: Over 50+ test cases covering core functionality, validation, data transformations, and business logic
- 🐛 Fixed phone validation to correctly count digits (excluding + sign)
- 🐛 Fixed email validation to use platform-independent regex for JVM compatibility

### Version 1.3.8
- 🎨 Color standardization: Unified all accent colors (FAB, bottom nav, buttons, pills) to use the same blue color from frequency pills
- 🎨 Updated dark theme primary color to match light theme for consistent blue across all themes
- 🎨 Improved visual consistency throughout the app

### Version 1.3.7
- 📸 Added camera permission handling for taking contact photos
- 🎨 Improved avatar initials: Shows one letter for single names, two letters (first + last) when both names are present
- 🎨 Avatar color selector: All colors now displayed in a grid layout (6 columns) for easier selection
- 🎨 Avatar colors: Random color assigned initially, only changes when user explicitly selects a color
- 🐛 Fixed scroll issue: First card in list no longer hidden under toolbar
- 🐛 Fixed Floating Action Button visibility: Moved FAB to MainScreen Scaffold to avoid nested Scaffold issues
- 🧹 Removed edit icon overlay from avatar (temporarily removed for future redesign)

### Version 1.3.6
- 🎨 New app icon: Broken cable design with gradient background and spark effect
- 🎨 Updated splash screen to display app icon with matching gradient background
- 🧹 Code cleanup: Removed unused imports, debug code, and unnecessary files
- 🐛 Fixed vector drawable compilation errors

### Version 1.3.5
- 📬 Renamed "Today" tab to "Inbox" for better clarity
- 📝 Added notes display on connection cards using DataRow format
- 🎨 Improved section spacing in Inbox view for better visual organization
- 🏗️ Refactored TodayScreen to InboxScreen with updated naming throughout codebase
- ✨ Inbox sections now properly organized: Past Due, Today, Upcoming

### Version 1.3.4
- 🎨 Major dark theme color improvements for better contrast and readability
- 🌑 Updated dark mode backgrounds: very dark green (0x0C3301), light yellow (d4d272), light red (d18a82)
- ✨ Conditional text colors in dark mode: white text on dark backgrounds, dark text on light backgrounds
- 🎯 Improved checkmark button: icon-only with increased padding for better touch target
- 🎨 Enhanced outline colors for dark mode cards with lighter, more visible borders
- ♿ Better accessibility with improved text contrast ratios

### Version 1.3.3
- 🎯 Reorganized "Today" view into an inbox-style layout with sections (Past Due, Today, Upcoming)
- ✨ "Today" view now includes overdue items (not just items due today)
- 📋 Added section headers to organize connections by urgency
- 🎨 Improved UX with clearer organization of what needs attention
- 📱 Updated query to show connections due in the next 7 days

### Version 1.3.2
- 🏗️ Refactored ConnectionItemActions into reusable DataRow composable
- 🏗️ Created Dimensions.kt for centralized spacing constants (xsmall, small, medium, large, xlarge)
- 🏗️ Centralized connection status colors in ConnectionColors object
- 🎨 Improved code maintainability with consistent spacing and color usage
- ✨ Added DataRow composable with preview support for unified data row layout

### Version 1.3.1
- 🏗️ Refactored ConnectionItem into separate composable file for better code organization
- 🐛 Fixed weight modifier issue in ConnectionItem layout
- 🐛 Fixed FlowColumnScopeInstance compilation error
- 🎨 Improved code maintainability by extracting reusable components

### Version 1.4
- 🐛 Fixed issue where connections marked as contacted didn't immediately appear in "Upcoming" section
- 🐛 Added forced recomposition to ensure UI updates immediately when marking connections as contacted
- 🎨 Improved state management for better UI responsiveness

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
