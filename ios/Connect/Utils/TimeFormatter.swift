import Foundation

enum TimeFormatter {
    static func formatRelativeTime(date: Date) -> String {
        let now = Date()
        let interval = now.timeIntervalSince(date)
        let seconds = Int(interval)
        let minutes = seconds / 60
        let hours = minutes / 60
        let days = hours / 24
        let weeks = days / 7
        let months = days / 30
        let years = days / 365

        switch true {
        case seconds < 60: return "just now"
        case minutes < 60: return "\(minutes) \(minutes == 1 ? "minute" : "minutes") ago"
        case hours < 24: return "\(hours) \(hours == 1 ? "hour" : "hours") ago"
        case days < 7: return "\(days) \(days == 1 ? "day" : "days") ago"
        case weeks < 4: return "\(weeks) \(weeks == 1 ? "week" : "weeks") ago"
        case months < 12: return "\(months) \(months == 1 ? "month" : "months") ago"
        default: return "\(years) \(years == 1 ? "year" : "years") ago"
        }
    }

    static func contactColorCategory(lastContactedDate: Date?, reminderFrequencyDays: Int) -> ContactColorCategory {
        guard let last = lastContactedDate else { return .red }
        let days = Calendar.current.dateComponents([.day], from: last, to: Date()).day ?? 0
        let greenThreshold = Double(reminderFrequencyDays)
        let yellowThreshold = Double(reminderFrequencyDays * 2)
        if Double(days) <= greenThreshold { return .green }
        if Double(days) <= yellowThreshold { return .yellow }
        return .red
    }

    static func formatTime(_ timeString: String?) -> String {
        guard let t = timeString, !t.isEmpty else { return "" }
        if t.count == 5 && t.contains(":") { return t }
        return t
    }
}
