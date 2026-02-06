import Foundation
import Combine

final class ConnectionDetailsViewModel: ObservableObject {
    private let store = ConnectionStore.shared
    private let prefs = Preferences.shared

    let connectionId: Int64
    @Published var connection: ScheduledConnection?
    @Published var isLoading = true
    @Published var errorMessage: String?
    @Published var deleteResult: DeleteResult?

    init(connectionId: Int64) {
        self.connectionId = connectionId
        load()
    }

    func load() {
        isLoading = true
        errorMessage = nil
        connection = store.connection(byId: connectionId)
        isLoading = false
        if connection == nil { errorMessage = "Connection not found" }
    }

    func markAsContacted() {
        guard let c = connection else { return }
        store.markAsContacted(c)
        if prefs.notificationsEnabled, let updated = store.connection(byId: c.id) {
            NotificationService.schedule(connection: updated, defaultReminderTime: prefs.defaultReminderTime)
        }
        load()
    }

    func deleteConnection() {
        guard let c = connection else { return }
        NotificationService.cancel(connectionId: c.id)
        store.delete(c)
        deleteResult = .success
    }

    func clearDeleteResult() { deleteResult = nil }
}

enum DeleteResult: Equatable {
    case success
    case error(String)
}
