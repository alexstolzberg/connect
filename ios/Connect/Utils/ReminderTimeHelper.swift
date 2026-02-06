import Foundation

enum ReminderTimeHelper {
    private static let formatter: DateFormatter = {
        let f = DateFormatter()
        f.dateFormat = "HH:mm"
        f.locale = Locale(identifier: "en_US_POSIX")
        return f
    }()

    static func date(from timeString: String?) -> Date? {
        guard let s = timeString?.trimmingCharacters(in: .whitespaces), !s.isEmpty else { return nil }
        let parts = s.split(separator: ":")
        let hour = min(max(Int(parts.first.map(String.init) ?? "10") ?? 10, 0), 23)
        let minute = parts.count > 1 ? min(max(Int(parts[1]) ?? 0, 0), 59) : 0
        return Calendar.current.date(bySettingHour: hour, minute: minute, second: 0, of: Date())
    }

    static func string(from date: Date) -> String {
        let cal = Calendar.current
        let h = cal.component(.hour, from: date)
        let m = cal.component(.minute, from: date)
        return String(format: "%02d:%02d", h, m)
    }
}
