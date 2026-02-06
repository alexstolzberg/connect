import Foundation
import Combine

final class ConnectionStore: ObservableObject {
    static let shared = ConnectionStore()

    @Published private(set) var connections: [ScheduledConnection] = []
    private let fileURL: URL
    private let queue = DispatchQueue(label: "com.stolz.Connect.store")

    private init() {
        let dir = FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask).first!
        let connectDir = dir.appendingPathComponent("Connect", isDirectory: true)
        try? FileManager.default.createDirectory(at: connectDir, withIntermediateDirectories: true)
        fileURL = connectDir.appendingPathComponent("connections.json")
        load()
    }

    private func load() {
        queue.async { [weak self] in
            guard let self = self else { return }
            do {
                let data = try Data(contentsOf: self.fileURL)
                let decoder = JSONDecoder()
                decoder.dateDecodingStrategy = .iso8601
                let decoded = try decoder.decode([ScheduledConnection].self, from: data)
                DispatchQueue.main.async { self.connections = decoded.filter { $0.isActive } }
            } catch {
                DispatchQueue.main.async { self.connections = [] }
            }
        }
    }

    private func save(_ list: [ScheduledConnection]) {
        let encoder = JSONEncoder()
        encoder.dateEncodingStrategy = .iso8601
        queue.async { [weak self] in
            guard let self = self else { return }
            do {
                let data = try encoder.encode(list)
                try data.write(to: self.fileURL)
            } catch { }
        }
    }

    var allActive: [ScheduledConnection] {
        connections.filter { $0.isActive }
    }

    func todayConnections() -> [ScheduledConnection] {
        let now = Date()
        let calendar = Calendar.current
        let todayStart = calendar.startOfDay(for: now)
        let sevenDaysLater = calendar.date(byAdding: .day, value: 7, to: todayStart)!
        return connections.filter { conn in
            let reminderStart = calendar.startOfDay(for: conn.nextReminderDate)
            if reminderStart <= sevenDaysLater { return true }
            if let bday = conn.birthday {
                let bdayComponents = calendar.dateComponents([.month, .day], from: bday)
                let year = calendar.component(.year, from: now)
                guard let bdayThisYear = calendar.date(from: DateComponents(year: year, month: bdayComponents.month, day: bdayComponents.day)) else { return false }
                if bdayThisYear >= todayStart && bdayThisYear <= sevenDaysLater { return true }
            }
            return false
        }.sorted { $0.nextReminderDate < $1.nextReminderDate }
    }

    func connection(byId id: Int64) -> ScheduledConnection? {
        connections.first { $0.id == id }
    }

    func insert(_ connection: ScheduledConnection) -> Int64 {
        var list = allActive
        let nextId = (list.map(\.id).max() ?? 0) + 1
        var c = connection
        c.id = nextId
        c.isActive = true
        list.append(c)
        let all = connections.filter { !$0.isActive } + list
        setConnectionsOnMain(all)
        save(all)
        return nextId
    }

    func update(_ connection: ScheduledConnection) {
        var list = connections
        if let i = list.firstIndex(where: { $0.id == connection.id }) {
            list[i] = connection
            setConnectionsOnMain(list)
            save(list)
        }
    }

    func delete(_ connection: ScheduledConnection) {
        var list = connections
        list.removeAll { $0.id == connection.id }
        setConnectionsOnMain(list)
        save(list)
    }

    /// Update connections on main thread synchronously so navigation to details sees the new data.
    private func setConnectionsOnMain(_ list: [ScheduledConnection]) {
        if Thread.isMainThread {
            connections = list
        } else {
            DispatchQueue.main.sync { connections = list }
        }
    }

    func markAsContacted(_ connection: ScheduledConnection) {
        let now = Date()
        let next = Calendar.current.date(byAdding: .day, value: connection.reminderFrequencyDays, to: now)!
        var c = connection
        c.lastContactedDate = now
        c.nextReminderDate = next
        update(c)
    }

    func snooze(_ connection: ScheduledConnection, until date: Date) {
        var c = connection
        c.nextReminderDate = date
        update(c)
    }

    func findPotentialDuplicates(name: String?, phone: String?, email: String?, excludeId: Int64?) -> [ScheduledConnection] {
        let nameNorm = name?.trimmingCharacters(in: .whitespacesAndNewlines).lowercased().nilIfEmpty
        let phoneNorm = phone?.replacingOccurrences(of: "[^0-9]", with: "", options: .regularExpression).nilIfEmpty
        let emailNorm = email?.trimmingCharacters(in: .whitespacesAndNewlines).lowercased().nilIfEmpty
        if nameNorm == nil && phoneNorm == nil && emailNorm == nil { return [] }
        return allActive.filter { existing in
            if let ex = excludeId, existing.id == ex { return false }
            if let n = nameNorm, existing.contactName.trimmingCharacters(in: .whitespaces).lowercased() == n { return true }
            if let p = phoneNorm, existing.contactPhoneNumber?.replacingOccurrences(of: "[^0-9]", with: "", options: .regularExpression) == p { return true }
            if let e = emailNorm, existing.contactEmail?.trimmingCharacters(in: .whitespaces).lowercased() == e { return true }
            return false
        }
    }

    func refresh() {
        load()
    }
}

private extension String {
    var nilIfEmpty: String? { isEmpty ? nil : self }
}
