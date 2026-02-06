import Foundation
import UserNotifications

enum NotificationService {
    static let connectionIdKey = "connectionId"
    static let connectionNameKey = "connectionName"

    static func requestAuthorization(completion: @escaping (Bool) -> Void) {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { granted, _ in
            DispatchQueue.main.async { completion(granted) }
        }
    }

    static func alarmDate(for connection: ScheduledConnection, defaultTime: String?) -> Date {
        let cal = Calendar.current
        var comps = cal.dateComponents([.year, .month, .day], from: connection.nextReminderDate)
        let timeStr = connection.reminderTime?.trimmingCharacters(in: .whitespaces).nilIfEmpty ?? defaultTime?.trimmingCharacters(in: .whitespaces).nilIfEmpty ?? "10:00"
        let parts = timeStr.split(separator: ":")
        comps.hour = min(Int(parts.first.map(String.init) ?? "10") ?? 10, 23)
        comps.minute = parts.count > 1 ? min(Int(parts[1]) ?? 0, 59) : 0
        comps.second = 0
        return cal.date(from: comps) ?? connection.nextReminderDate
    }

    static func schedule(connection: ScheduledConnection, defaultReminderTime: String?, showIfDueNow: Bool = true) {
        UNUserNotificationCenter.current().removePendingNotificationRequests(withIdentifiers: ["\(connection.id)"])
        let alarmDate = alarmDate(for: connection, defaultTime: defaultReminderTime)
        if alarmDate <= Date() {
            if showIfDueNow {
                showNow(connection: connection)
            }
            return
        }
        let content = UNMutableNotificationContent()
        content.title = "Connect"
        content.body = "Reminder: reach out to \(connection.contactName)"
        content.sound = .default
        content.userInfo = [connectionIdKey: connection.id, connectionNameKey: connection.contactName]
        let trigger = UNCalendarNotificationTrigger(dateMatching: Calendar.current.dateComponents([.year, .month, .day, .hour, .minute], from: alarmDate), repeats: false)
        let request = UNNotificationRequest(identifier: "\(connection.id)", content: content, trigger: trigger)
        UNUserNotificationCenter.current().add(request)
    }

    static func showNow(connection: ScheduledConnection) {
        let content = UNMutableNotificationContent()
        content.title = "Connect"
        content.body = "Reminder: reach out to \(connection.contactName)"
        content.sound = .default
        content.userInfo = [connectionIdKey: connection.id, connectionNameKey: connection.contactName]
        let request = UNNotificationRequest(identifier: "immediate-\(connection.id)", content: content, trigger: UNTimeIntervalNotificationTrigger(timeInterval: 0.1, repeats: false))
        UNUserNotificationCenter.current().add(request)
    }

    static func cancel(connectionId: Int64) {
        UNUserNotificationCenter.current().removePendingNotificationRequests(withIdentifiers: ["\(connectionId)"])
        UNUserNotificationCenter.current().removeDeliveredNotifications(withIdentifiers: ["\(connectionId)", "immediate-\(connectionId)"])
    }
}

private extension String {
    var nilIfEmpty: String? { isEmpty ? nil : self }
}
