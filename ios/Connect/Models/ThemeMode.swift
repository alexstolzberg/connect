import Foundation

enum ThemeMode: String, CaseIterable {
    case system
    case light
    case dark

    var displayName: String {
        switch self {
        case .system: return "System"
        case .light: return "Light"
        case .dark: return "Dark"
        }
    }
}

enum AllSortOrder: String, CaseIterable {
    case a_z = "A_Z"
    case dateAscending = "DATE_ASCENDING"
    case dateDescending = "DATE_DESCENDING"

    var displayName: String {
        switch self {
        case .a_z: return "A–Z"
        case .dateAscending: return "Date (soonest first)"
        case .dateDescending: return "Date (latest first)"
        }
    }
}
