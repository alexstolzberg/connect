import Foundation
import Combine

final class Preferences: ObservableObject {
    static let shared = Preferences()

    private let defaults = UserDefaults.standard

    private enum Keys {
        static let themeMode = "theme_mode"
        static let allSortOrder = "all_sort_order"
        static let notificationsEnabled = "notifications_enabled"
        static let defaultReminderTime = "default_reminder_time"
    }

    @Published var themeMode: ThemeMode {
        didSet {
            defaults.set(themeMode.rawValue, forKey: Keys.themeMode)
        }
    }

    @Published var allSortOrder: AllSortOrder {
        didSet {
            defaults.set(allSortOrder.rawValue, forKey: Keys.allSortOrder)
        }
    }

    @Published var notificationsEnabled: Bool {
        didSet {
            defaults.set(notificationsEnabled, forKey: Keys.notificationsEnabled)
        }
    }

    @Published var defaultReminderTime: String {
        didSet {
            defaults.set(defaultReminderTime, forKey: Keys.defaultReminderTime)
        }
    }

    private init() {
        let themeRaw = defaults.string(forKey: Keys.themeMode) ?? ThemeMode.system.rawValue
        themeMode = ThemeMode(rawValue: themeRaw) ?? .system
        let sortRaw = defaults.string(forKey: Keys.allSortOrder) ?? AllSortOrder.dateAscending.rawValue
        allSortOrder = AllSortOrder(rawValue: sortRaw) ?? .dateAscending
        notificationsEnabled = defaults.bool(forKey: Keys.notificationsEnabled)
        defaultReminderTime = defaults.string(forKey: Keys.defaultReminderTime) ?? "10:00"
    }
}
