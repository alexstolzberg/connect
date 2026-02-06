import Foundation

struct ScheduledConnection: Identifiable, Codable, Equatable {
    var id: Int64
    var contactName: String
    var contactPhoneNumber: String?
    var contactEmail: String?
    var contactPhotoUri: String?
    var avatarColor: Int?
    var contactId: String?
    var reminderFrequencyDays: Int
    var preferredMethod: ConnectionMethod
    var reminderTime: String?
    var lastContactedDate: Date?
    var nextReminderDate: Date
    var notes: String?
    var birthday: Date?
    var promptOnBirthday: Bool
    var createdAt: Date
    var isActive: Bool

    init(
        id: Int64 = 0,
        contactName: String,
        contactPhoneNumber: String? = nil,
        contactEmail: String? = nil,
        contactPhotoUri: String? = nil,
        avatarColor: Int? = nil,
        contactId: String? = nil,
        reminderFrequencyDays: Int,
        preferredMethod: ConnectionMethod,
        reminderTime: String? = nil,
        lastContactedDate: Date? = nil,
        nextReminderDate: Date,
        notes: String? = nil,
        birthday: Date? = nil,
        promptOnBirthday: Bool = true,
        createdAt: Date = Date(),
        isActive: Bool = true
    ) {
        self.id = id
        self.contactName = contactName
        self.contactPhoneNumber = contactPhoneNumber
        self.contactEmail = contactEmail
        self.contactPhotoUri = contactPhotoUri
        self.avatarColor = avatarColor
        self.contactId = contactId
        self.reminderFrequencyDays = reminderFrequencyDays
        self.preferredMethod = preferredMethod
        self.reminderTime = reminderTime
        self.lastContactedDate = lastContactedDate
        self.nextReminderDate = nextReminderDate
        self.notes = notes
        self.birthday = birthday
        self.promptOnBirthday = promptOnBirthday
        self.createdAt = createdAt
        self.isActive = isActive
    }

    var isDueToday: Bool {
        Calendar.current.isDateInToday(nextReminderDate) || nextReminderDate < Calendar.current.startOfDay(for: Date())
    }

    var isPastDue: Bool {
        nextReminderDate < Calendar.current.startOfDay(for: Date())
    }

    enum CodingKeys: String, CodingKey {
        case id, contactName, contactPhoneNumber, contactEmail, contactPhotoUri, avatarColor, contactId
        case reminderFrequencyDays, preferredMethod, reminderTime, lastContactedDate, nextReminderDate
        case notes, birthday, promptOnBirthday, createdAt, isActive
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        id = try c.decode(Int64.self, forKey: .id)
        contactName = try c.decode(String.self, forKey: .contactName)
        contactPhoneNumber = try c.decodeIfPresent(String.self, forKey: .contactPhoneNumber)
        contactEmail = try c.decodeIfPresent(String.self, forKey: .contactEmail)
        contactPhotoUri = try c.decodeIfPresent(String.self, forKey: .contactPhotoUri)
        avatarColor = try c.decodeIfPresent(Int.self, forKey: .avatarColor)
        contactId = try c.decodeIfPresent(String.self, forKey: .contactId)
        reminderFrequencyDays = try c.decode(Int.self, forKey: .reminderFrequencyDays)
        let methodRaw = try c.decode(String.self, forKey: .preferredMethod)
        preferredMethod = ConnectionMethod(rawValue: methodRaw) ?? .both
        reminderTime = try c.decodeIfPresent(String.self, forKey: .reminderTime)
        lastContactedDate = try c.decodeIfPresent(Date.self, forKey: .lastContactedDate)
        nextReminderDate = try c.decode(Date.self, forKey: .nextReminderDate)
        notes = try c.decodeIfPresent(String.self, forKey: .notes)
        birthday = try c.decodeIfPresent(Date.self, forKey: .birthday)
        promptOnBirthday = try c.decodeIfPresent(Bool.self, forKey: .promptOnBirthday) ?? true
        createdAt = try c.decodeIfPresent(Date.self, forKey: .createdAt) ?? Date()
        isActive = try c.decodeIfPresent(Bool.self, forKey: .isActive) ?? true
    }

    func encode(to encoder: Encoder) throws {
        var c = encoder.container(keyedBy: CodingKeys.self)
        try c.encode(id, forKey: .id)
        try c.encode(contactName, forKey: .contactName)
        try c.encodeIfPresent(contactPhoneNumber, forKey: .contactPhoneNumber)
        try c.encodeIfPresent(contactEmail, forKey: .contactEmail)
        try c.encodeIfPresent(contactPhotoUri, forKey: .contactPhotoUri)
        try c.encodeIfPresent(avatarColor, forKey: .avatarColor)
        try c.encodeIfPresent(contactId, forKey: .contactId)
        try c.encode(reminderFrequencyDays, forKey: .reminderFrequencyDays)
        try c.encode(preferredMethod.rawValue, forKey: .preferredMethod)
        try c.encodeIfPresent(reminderTime, forKey: .reminderTime)
        try c.encodeIfPresent(lastContactedDate, forKey: .lastContactedDate)
        try c.encode(nextReminderDate, forKey: .nextReminderDate)
        try c.encodeIfPresent(notes, forKey: .notes)
        try c.encodeIfPresent(birthday, forKey: .birthday)
        try c.encode(promptOnBirthday, forKey: .promptOnBirthday)
        try c.encode(createdAt, forKey: .createdAt)
        try c.encode(isActive, forKey: .isActive)
    }
}

enum ConnectionMethod: String, Codable, CaseIterable {
    case call
    case message
    case email
    case both
    case faceTime

    var displayName: String {
        switch self {
        case .call: return "Call"
        case .message: return "Message"
        case .email: return "Email"
        case .both: return "Call & Message"
        case .faceTime: return "FaceTime"
        }
    }
}

enum ContactColorCategory {
    case green
    case yellow
    case red
}

struct InboxSection: Identifiable {
    let id: String
    let title: String
    let connections: [ScheduledConnection]
}
