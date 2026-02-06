import Foundation
import Combine

final class SettingsViewModel: ObservableObject {
    let prefs = Preferences.shared

    @Published var themeMode: ThemeMode { didSet { prefs.themeMode = themeMode } }
    @Published var notificationsEnabled: Bool { didSet { prefs.notificationsEnabled = notificationsEnabled } }
    @Published var defaultReminderTime: String { didSet { prefs.defaultReminderTime = defaultReminderTime } }

    init() {
        themeMode = prefs.themeMode
        notificationsEnabled = prefs.notificationsEnabled
        defaultReminderTime = prefs.defaultReminderTime
    }

    func setTheme(_ mode: ThemeMode) { themeMode = mode }
    func setNotifications(_ enabled: Bool) { notificationsEnabled = enabled }
    func setDefaultReminderTime(_ time: String) { defaultReminderTime = time }
}
