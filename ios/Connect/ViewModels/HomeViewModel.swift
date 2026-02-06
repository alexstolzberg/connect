import Foundation
import Combine

final class HomeViewModel: ObservableObject {
    private let store = ConnectionStore.shared
    private let prefs = Preferences.shared
    private var cancellables = Set<AnyCancellable>()

    @Published var searchQuery = ""
    @Published var allConnections: [ScheduledConnection] = []
    @Published var todayConnections: [ScheduledConnection] = []
    @Published var inboxSections: [InboxSection] = []
    @Published var allSortOrder: AllSortOrder = .dateAscending

    init() {
        prefs.$allSortOrder.assign(to: &$allSortOrder)
        Publishers.CombineLatest3(store.$connections, $searchQuery, prefs.$allSortOrder)
            .map { [weak self] conns, query, sortOrder in
                guard let self = self else { return ([], [], []) }
                let active = conns.filter { $0.isActive }
                let filtered = query.isEmpty ? active : active.filter {
                    $0.contactName.localizedCaseInsensitiveContains(query) ||
                    ($0.contactPhoneNumber?.localizedCaseInsensitiveContains(query) ?? false) ||
                    ($0.contactEmail?.localizedCaseInsensitiveContains(query) ?? false)
                }
                let sorted: [ScheduledConnection]
                switch sortOrder {
                case .a_z: sorted = filtered.sorted { $0.contactName.localizedCaseInsensitiveCompare($1.contactName) == .orderedAscending }
                case .dateAscending: sorted = filtered.sorted { $0.nextReminderDate < $1.nextReminderDate }
                case .dateDescending: sorted = filtered.sorted { $0.nextReminderDate > $1.nextReminderDate }
                }
                let today = self.store.todayConnections()
                let todayFiltered = query.isEmpty ? today : today.filter {
                    $0.contactName.localizedCaseInsensitiveContains(query) ||
                    ($0.contactPhoneNumber?.localizedCaseInsensitiveContains(query) ?? false) ||
                    ($0.contactEmail?.localizedCaseInsensitiveContains(query) ?? false)
                }
                let sections = self.organizeSections(todayFiltered)
                return (sorted, todayFiltered, sections)
            }
            .receive(on: DispatchQueue.main)
            .sink { [weak self] (all: [ScheduledConnection], today: [ScheduledConnection], sections: [InboxSection]) in
                self?.allConnections = all
                self?.todayConnections = today
                self?.inboxSections = sections
            }
            .store(in: &cancellables)
    }

    func setSearchQuery(_ q: String) { searchQuery = q }
    func setAllSortOrder(_ order: AllSortOrder) { prefs.allSortOrder = order }
    func refresh() { store.refresh() }

    /// Recompute inbox/all lists from current store (in-memory). Use when returning to the list so new items show immediately.
    func syncFromStore() {
        let conns = store.connections.filter { $0.isActive }
        let query = searchQuery
        let filtered = query.isEmpty ? conns : conns.filter {
            $0.contactName.localizedCaseInsensitiveContains(query) ||
            ($0.contactPhoneNumber?.localizedCaseInsensitiveContains(query) ?? false) ||
            ($0.contactEmail?.localizedCaseInsensitiveContains(query) ?? false)
        }
        let sortOrder = prefs.allSortOrder
        let sorted: [ScheduledConnection]
        switch sortOrder {
        case .a_z: sorted = filtered.sorted { $0.contactName.localizedCaseInsensitiveCompare($1.contactName) == .orderedAscending }
        case .dateAscending: sorted = filtered.sorted { $0.nextReminderDate < $1.nextReminderDate }
        case .dateDescending: sorted = filtered.sorted { $0.nextReminderDate > $1.nextReminderDate }
        }
        allConnections = sorted
        let today = store.todayConnections()
        let todayFiltered = query.isEmpty ? today : today.filter {
            $0.contactName.localizedCaseInsensitiveContains(query) ||
            ($0.contactPhoneNumber?.localizedCaseInsensitiveContains(query) ?? false) ||
            ($0.contactEmail?.localizedCaseInsensitiveContains(query) ?? false)
        }
        todayConnections = todayFiltered
        inboxSections = organizeSections(todayFiltered)
    }

    func markAsContacted(_ connection: ScheduledConnection) {
        store.markAsContacted(connection)
        if prefs.notificationsEnabled {
            if let updated = store.connection(byId: connection.id) {
                NotificationService.schedule(connection: updated, defaultReminderTime: prefs.defaultReminderTime)
            }
        }
    }

    func snooze(_ connection: ScheduledConnection, until date: Date) {
        store.snooze(connection, until: date)
        if prefs.notificationsEnabled, let updated = store.connection(byId: connection.id) {
            NotificationService.schedule(connection: updated, defaultReminderTime: prefs.defaultReminderTime)
        }
    }

    func delete(_ connection: ScheduledConnection) {
        NotificationService.cancel(connectionId: connection.id)
        store.delete(connection)
    }

    private func organizeSections(_ connections: [ScheduledConnection]) -> [InboxSection] {
        let cal = Calendar.current
        let todayStart = cal.startOfDay(for: Date())
        let tomorrowStart = cal.date(byAdding: .day, value: 1, to: todayStart)!
        var pastDue: [ScheduledConnection] = []
        var today: [ScheduledConnection] = []
        var upcoming: [ScheduledConnection] = []
        for c in connections {
            let reminderStart = cal.startOfDay(for: c.nextReminderDate)
            if reminderStart < todayStart { pastDue.append(c) }
            else if reminderStart < tomorrowStart { today.append(c) }
            else { upcoming.append(c) }
        }
        var result: [InboxSection] = []
        if !pastDue.isEmpty { result.append(InboxSection(id: "past", title: "Past Due", connections: pastDue)) }
        if !today.isEmpty { result.append(InboxSection(id: "today", title: "Today", connections: today)) }
        if !upcoming.isEmpty { result.append(InboxSection(id: "upcoming", title: "Upcoming", connections: upcoming)) }
        return result
    }
}
