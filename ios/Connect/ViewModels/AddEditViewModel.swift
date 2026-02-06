import Foundation
import Combine

final class AddEditViewModel: ObservableObject {
    private let store = ConnectionStore.shared
    private let prefs = Preferences.shared

    let connectionId: Int64?
    @Published var contactName = ""
    @Published var contactPhoneNumber: String?
    @Published var contactEmail: String?
    @Published var reminderFrequencyDays = 7
    /// When user selects "Custom" frequency, this holds the day count (1–365).
    @Published var customFrequencyDays = 7
    @Published var preferredMethod: ConnectionMethod = .both
    @Published var reminderTime: String?
    @Published var notes: String?
    @Published var birthday: Date?
    @Published var promptOnBirthday = true
    @Published var nextReminderDate = Date()
    @Published var contactPhotoUri: String?
    @Published var avatarColor: Int?
    @Published var contactId: String?
    @Published var saveResult: SaveResult?
    @Published var duplicateCandidates: [ScheduledConnection]?
    /// Set after inserting a new connection so the UI can navigate to it.
    private(set) var lastInsertedId: Int64?

    /// Human-readable description of which fields matched (e.g. "Name, Phone") when duplicateCandidates is set.
    var duplicateMatchDescription: String? {
        guard let dupes = duplicateCandidates, let first = dupes.first else { return nil }
        let name = contactName.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        let phoneNorm = contactPhoneNumber?.replacingOccurrences(of: "[^0-9]", with: "", options: .regularExpression)
        let emailNorm = contactEmail?.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        var parts: [String] = []
        if !name.isEmpty && first.contactName.trimmingCharacters(in: .whitespaces).lowercased() == name { parts.append("Name") }
        if let p = phoneNorm, !p.isEmpty, first.contactPhoneNumber?.replacingOccurrences(of: "[^0-9]", with: "", options: .regularExpression) == p { parts.append("Phone") }
        if let e = emailNorm, !e.isEmpty, first.contactEmail?.trimmingCharacters(in: .whitespaces).lowercased() == e { parts.append("Email") }
        return parts.isEmpty ? nil : parts.joined(separator: ", ")
    }

    init(connectionId: Int64?) {
        self.connectionId = connectionId
        if let id = connectionId, id > 0, let c = store.connection(byId: id) {
            contactName = c.contactName
            contactPhoneNumber = c.contactPhoneNumber
            contactEmail = c.contactEmail
            let days = c.reminderFrequencyDays
            reminderFrequencyDays = [1, 7, 14, 30].contains(days) ? days : 0
            customFrequencyDays = [1, 7, 14, 30].contains(days) ? 7 : days
            preferredMethod = c.preferredMethod
            reminderTime = c.reminderTime
            notes = c.notes
            birthday = c.birthday
            promptOnBirthday = c.promptOnBirthday
            nextReminderDate = c.nextReminderDate
            contactPhotoUri = c.contactPhotoUri
            avatarColor = c.avatarColor
            contactId = c.contactId
        } else {
            reminderTime = prefs.defaultReminderTime
            avatarColor = AvatarColors.argbValues[Int.random(in: 0..<AvatarColors.argbValues.count)]
        }
    }

    func updateContactPhotoUri(_ path: String?) { contactPhotoUri = path }
    func updateAvatarColor(_ int: Int?) { avatarColor = int }

    var defaultReminderTime: String { prefs.defaultReminderTime }

    /// Whether preferred method is valid given current phone/email (call/message/faceTime need phone, email needs email, both needs at least one).
    func isPreferredMethodValid(hasPhone: Bool, hasEmail: Bool) -> Bool {
        switch preferredMethod {
        case .call, .message, .faceTime: return hasPhone
        case .email: return hasEmail
        case .both: return hasPhone || hasEmail
        }
    }

    /// If current preferred method is invalid (e.g. Email selected but no email), reset to a valid choice.
    func ensurePreferredMethodValid() {
        let hasPhone = (contactPhoneNumber?.trimmingCharacters(in: .whitespaces)).map { !$0.isEmpty } ?? false
        let hasEmail = (contactEmail?.trimmingCharacters(in: .whitespaces)).map { !$0.isEmpty } ?? false
        guard !isPreferredMethodValid(hasPhone: hasPhone, hasEmail: hasEmail) else { return }
        if hasPhone { preferredMethod = .call }
        else if hasEmail { preferredMethod = .email }
        else { preferredMethod = .both }
    }

    func save() {
        let name = contactName.trimmingCharacters(in: .whitespacesAndNewlines)
        let phone = contactPhoneNumber?.trimmingCharacters(in: .whitespaces).nilIfEmpty
        let email = contactEmail?.trimmingCharacters(in: .whitespaces).nilIfEmpty
        if name.isEmpty || (phone == nil && email == nil) {
            saveResult = .error("Please provide at least a name and either a phone number or email")
            return
        }
        if let p = phone, !ValidationUtils.isValidPhone(p) {
            saveResult = .error("Please enter a valid phone number")
            return
        }
        if let e = email, !ValidationUtils.isValidEmail(e) {
            saveResult = .error("Please enter a valid email address")
            return
        }
        switch preferredMethod {
        case .call, .message, .faceTime:
            if phone == nil { saveResult = .error("Please enter a phone number to use Call, Message, or FaceTime"); return }
        case .email:
            if email == nil { saveResult = .error("Please enter an email to use Email"); return }
        case .both:
            if phone == nil && email == nil { saveResult = .error("Please enter at least a phone or email"); return }
        }
        let duplicates = store.findPotentialDuplicates(name: name, phone: phone, email: email, excludeId: connectionId)
        if !duplicates.isEmpty {
            duplicateCandidates = duplicates
            return
        }
        performSave(name: name, phone: phone, email: email)
    }

    func saveAnyway() {
        duplicateCandidates = nil
        let name = contactName.trimmingCharacters(in: .whitespacesAndNewlines)
        let phone = contactPhoneNumber?.trimmingCharacters(in: .whitespaces).nilIfEmpty
        let email = contactEmail?.trimmingCharacters(in: .whitespaces).nilIfEmpty
        performSave(name: name, phone: phone, email: email)
    }

    func clearDuplicateCandidates() { duplicateCandidates = nil }
    func clearSaveResult() { saveResult = nil }

    /// Fill form from a contact picked from the device (name, phone, email).
    func applyPickedContact(name: String, phone: String?, email: String?) {
        contactName = name
        contactPhoneNumber = phone
        contactEmail = email
    }

    private func performSave(name: String, phone: String?, email: String?) {
        let days = reminderFrequencyDays == 0 ? min(365, max(1, customFrequencyDays)) : reminderFrequencyDays
        var conn = ScheduledConnection(
            id: connectionId ?? 0,
            contactName: name,
            contactPhoneNumber: phone,
            contactEmail: email,
            contactPhotoUri: contactPhotoUri,
            avatarColor: avatarColor,
            contactId: contactId,
            reminderFrequencyDays: days,
            preferredMethod: preferredMethod,
            reminderTime: reminderTime,
            nextReminderDate: nextReminderDate,
            notes: notes?.nilIfEmpty,
            birthday: birthday,
            promptOnBirthday: promptOnBirthday,
            isActive: true
        )
        if let id = connectionId, id > 0 {
            conn.id = id
            store.update(conn)
            if prefs.notificationsEnabled {
                NotificationService.schedule(connection: conn, defaultReminderTime: prefs.defaultReminderTime)
            }
        } else {
            let insertedId = store.insert(conn)
            lastInsertedId = insertedId
            if let photoPath = contactPhotoUri, !photoPath.isEmpty {
                PhotoStorage.replacePhotoAfterInsert(oldPath: photoPath, newConnectionId: insertedId)
                let newPath = "connection_photos/\(insertedId).jpg"
                if var updated = store.connection(byId: insertedId) {
                    updated.contactPhotoUri = newPath
                    store.update(updated)
                }
            }
            if prefs.notificationsEnabled, let c = store.connection(byId: insertedId) {
                NotificationService.schedule(connection: c, defaultReminderTime: prefs.defaultReminderTime, showIfDueNow: false)
            }
        }
        saveResult = .success
    }
}

enum SaveResult: Equatable {
    case success
    case error(String)
}

private extension String {
    var nilIfEmpty: String? { isEmpty ? nil : self }
}
