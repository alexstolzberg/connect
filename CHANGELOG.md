# Changelog

Android and iOS share the same version number from **1.3.16** onward. Entries are tagged **[Android]** or **[iOS]** when they apply to one platform; untagged entries apply to Android (historical releases were Android-only).

---

## 1.3.16

- **[iOS]** Snooze indicator on cards: clock icon and "Snoozed" label when a connection is snoozed.
- **[iOS]** Inbox and All lists refresh immediately after marking as contacted or snoozing (no need to switch tabs).
- **[iOS]** Accent color matches Android: dark blue (ConnectPrimary #6366F1) in `ConnectColors.primary` and AccentColor asset.
- **[iOS]** Splash screen: white rounded square (280pt) with icon and text; gradient background retained.
- **[iOS]** Last contacted row on connection card (dedicated row like Android, with relative time or "Never contacted").
- **[iOS]** Snooze and complete actions shown on green cards so users can snooze further or mark as contacted early.
- **[iOS]** Preferred method must match contact info: picker only shows valid options (Call/Message/FaceTime require phone, Email requires email); selection auto-corrects when phone/email is cleared; save validation unchanged.
- **[Android]** Preferred method validation: selection auto-resets when it becomes invalid (e.g. Email selected then email cleared); same validation on save and when loading a connection.
- **[iOS]** Inbox no longer empty on first load: store load started at app launch; delayed sync fallback when list is still empty.

---

## 1.3.15

- 🎨 **Design system**: Standardized spacing (Dimensions), shapes, and typography; reusable building blocks: ConnectCard, ConnectButton, ConnectIcon, EmptyState, SectionHeader; app migrated to use them throughout.
- 🃏 **Connection cards**: Status bar on the left (green/yellow/red); Snooze and Mark-complete actions in a bottom row (no overlap with name); Next/Last reminder and Last contacted aligned with other rows using same label/value pattern.
- 📋 **All pane sort**: Sort by A–Z, Date (soonest first), or Date (latest first); choice remembered across sessions (Sort icon in All screen app bar).
- 📐 **Data row spacing**: Consistent spacing between data rows (Phone, Email, Birthday, Notes, Next/Last) and between label and value for easier reading.
- 📳 **Haptic feedback**: Light vibration on key actions (mark complete, snooze, sort options, tab switch, FAB, Mark as Contacted).
- 📍 **Snackbars**: Anchored at bottom of screen (above nav bar when visible) instead of center.
- 🧹 **Deprecations**: Replaced `Divider` with `HorizontalDivider`; use AutoMirrored icons (ArrowBack, Send, List, Sort) where applicable.

## 1.3.14

- 🏷️ Connection details: Non-clickable pill shows "From contacts" or "Manually added" (View Contact / Search / Add actions removed).
- 🧹 Removed unused ContactHelper code: openContactInPhone, openContactsWithSearch, addContactToPhone, canOpenContactInPhone.
- 🧹 Cleanup: Duplicate imports removed in ConnectionDetailsScreen and AddEditScreen.

## 1.3.13

- ⏰ Default reminder time: Settings → Default reminder time (default 10:00 AM); used when a connection has no reminder time.
- ⏰ Reminder time on Add/Edit: Per-connection reminder time with time picker; new connections start with default.
- 🕐 Clock icon for Default reminder time in Settings.
- 🧭 Tab switching: Fade transitions between Inbox/All/Settings (no horizontal swipe feel).
- 🎨 Preferred method pills: Grey border when disabled (Call/Message without phone, Email without email).
- 🧪 SettingsViewModelTest: Stub getDefaultReminderTime() for init.

## 1.3.12

- 🎬 Slide transitions: Add/Edit screen slides up from bottom (modal-style) and down on dismiss/save; other screens use horizontal slide.
- 📍 Snackbars pinned to bottom (above bottom nav when visible).
- ⌨️ Keyboard: Scroll gets extra bottom padding when keyboard is up (imePadding + adjustResize).
- 📋 Duplicate dialog shows which fields matched (Name, Phone, Email).
- 🌙 Dark mode: Single `isConnectDarkTheme()` from theme (no luminance/brightness); card text/icons use `ConnectionColors.OnCardDark`; snooze icon explicit tint.
- 🎨 Connection card colors: Opaque dark-theme backgrounds; `OnCardDark` for all card content in dark mode.
- 🧪 Tests updated: SettingsViewModelTest mocks notificationPreferences/connectionRepository; ConnectionRepositoryTest mocks 3-arg scheduleNotification.

## 1.3.11

- 🔔 Push notifications: Right time (uses reminder time of day), tap opens contact details, message "This is a reminder to connect with [name]".
- 🔔 Notification title "It's time to connect!" and bell icon.
- 🔔 Notifications off by default; first-launch prompt to enable (with permission request on Android 13+).
- 🔔 Settings toggle to turn reminder notifications on/off; permission handled accordingly.
- 📝 Snackbar confirmations: Connection added, Connection updated, Connection deleted, Marked as contacted, Reminder snoozed.
- 🎨 FAB and bottom nav use MaterialTheme.colorScheme.primary (match PillButtons).
- 🎨 Mark as Contacted button on connection details uses primary state.
- 📄 Privacy policy updated for in-app notifications control.

## 1.3.9

- ✅ Comprehensive test suite: Added unit tests for ViewModels, Repositories, DAOs, utilities, mappers, and domain models.
- ✅ Test infrastructure: Added MockK, Turbine, Coroutines Test, Robolectric, and Room testing dependencies.
- ✅ Test coverage: Over 50+ test cases covering core functionality, validation, data transformations, and business logic.
- 🐛 Fixed phone validation to correctly count digits (excluding + sign).
- 🐛 Fixed email validation to use platform-independent regex for JVM compatibility.

## 1.3.8

- 🎨 Color standardization: Unified all accent colors (FAB, bottom nav, buttons, pills) to use the same blue color from frequency pills.
- 🎨 Updated dark theme primary color to match light theme for consistent blue across all themes.
- 🎨 Improved visual consistency throughout the app.

## 1.3.7

- 📸 Added camera permission handling for taking contact photos.
- 🎨 Improved avatar initials: Shows one letter for single names, two letters (first + last) when both names are present.
- 🎨 Avatar color selector: All colors now displayed in a grid layout (6 columns) for easier selection.
- 🎨 Avatar colors: Random color assigned initially, only changes when user explicitly selects a color.
- 🐛 Fixed scroll issue: First card in list no longer hidden under toolbar.
- 🐛 Fixed Floating Action Button visibility: Moved FAB to MainScreen Scaffold to avoid nested Scaffold issues.
- 🧹 Removed edit icon overlay from avatar (temporarily removed for future redesign).

## 1.3.6

- 🎨 New app icon: Broken cable design with gradient background and spark effect.
- 🎨 Updated splash screen to display app icon with matching gradient background.
- 🧹 Code cleanup: Removed unused imports, debug code, and unnecessary files.
- 🐛 Fixed vector drawable compilation errors.

## 1.3.5

- 📬 Renamed "Today" tab to "Inbox" for better clarity.
- 📝 Added notes display on connection cards using DataRow format.
- 🎨 Improved section spacing in Inbox view for better visual organization.
- 🏗️ Refactored TodayScreen to InboxScreen with updated naming throughout codebase.
- ✨ Inbox sections now properly organized: Past Due, Today, Upcoming.

## 1.3.4

- 🎨 Major dark theme color improvements for better contrast and readability.
- 🌑 Updated dark mode backgrounds: very dark green (0x0C3301), light yellow (d4d272), light red (d18a82).
- ✨ Conditional text colors in dark mode: white text on dark backgrounds, dark text on light backgrounds.
- 🎯 Improved checkmark button: icon-only with increased padding for better touch target.
- 🎨 Enhanced outline colors for dark mode cards with lighter, more visible borders.
- ♿ Better accessibility with improved text contrast ratios.

## 1.3.3

- 🎯 Reorganized "Today" view into an inbox-style layout with sections (Past Due, Today, Upcoming).
- ✨ "Today" view now includes overdue items (not just items due today).
- 📋 Added section headers to organize connections by urgency.
- 🎨 Improved UX with clearer organization of what needs attention.
- 📱 Updated query to show connections due in the next 7 days.

## 1.3.2

- 🏗️ Refactored ConnectionItemActions into reusable DataRow composable.
- 🏗️ Created Dimensions.kt for centralized spacing constants (xsmall, small, medium, large, xlarge).
- 🏗️ Centralized connection status colors in ConnectionColors object.
- 🎨 Improved code maintainability with consistent spacing and color usage.
- ✨ Added DataRow composable with preview support for unified data row layout.

## 1.3.1

- 🏗️ Refactored ConnectionItem into separate composable file for better code organization.
- 🐛 Fixed weight modifier issue in ConnectionItem layout.
- 🐛 Fixed FlowColumnScopeInstance compilation error.
- 🎨 Improved code maintainability by extracting reusable components.

## 1.4

- 🐛 Fixed issue where connections marked as contacted didn't immediately appear in "Upcoming" section.
- 🐛 Added forced recomposition to ensure UI updates immediately when marking connections as contacted.
- 🎨 Improved state management for better UI responsiveness.

## 1.3

- ✨ Dark mode support with theme selection (System Default, Light, Dark).
- ✨ Settings screen replacing About tab.
- ✨ Search functionality on Today and All tabs (search by name, phone, or email).
- ✨ About screen accessible from Settings.
- 🐛 Fixed birthday date picker timezone issue (dates now display correctly).
- 🐛 Fixed various icon reference issues in Settings screen.
- 🎨 Improved navigation structure with Settings as main tab.

## 1.2

- ✨ Push notifications when reminders are due.
- ✨ Contact photo import from device contacts.
- ✨ Deep link to view contacts in phone's Contacts app.
- ✨ Add custom connections to phone's Contacts app.
- ✨ Birthday display with date formatting on connection cards.
- ✨ Phone number auto-formatting with dashes (XXX-XXX-XXXX).
- ✨ Phone number input validation (digits only).
- ✨ Email validation on input fields.
- ✨ Pull-to-refresh on Today and All tabs.
- 🎨 Color-coded card backgrounds (red/yellow/green based on contact status).
- 🎨 Improved card layout with aligned action icons.
- 🎨 Animated checkmark with bounce effect when marking as contacted.
- 🎨 Animated navigation bar indicator.
- 🎨 Save button moved to toolbar with validation.
- 🎨 Headers for Phone and Email fields on cards.
- 🐛 Fixed clipping issues under toolbar.
- 🐛 Fixed custom days input field handling.
- 🐛 Fixed icon alignment on connection cards.
- 📱 Database schema updated to version 4.

## 1.1

- ✨ Added splash screen with app branding.
- ✨ Added About tab with app explanation and usage guide.
- ✨ Email support as a contact method.
- ✨ Profile picture display in connection items.
- ✨ Conditional contact method icons (only show available methods).
- ✨ Scrollable add/edit connection screen.
- 🐛 Fixed nullable field handling throughout the app.
- 🎨 Improved UI formatting and visual hierarchy.
- 📱 Database schema updated to version 3.

## 1.0

- Initial release with core reminder functionality.
- Color-coded contact indicators.
- Relative time formatting.
- Mark as contacted feature.
- Birthday field support.
