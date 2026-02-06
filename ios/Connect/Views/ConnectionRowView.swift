import SwiftUI
import UIKit

struct ConnectionRowView: View {
    let connection: ScheduledConnection
    let onTap: () -> Void
    let onSnooze: () -> Void
    let onMarkComplete: () -> Void
    let onCall: (() -> Void)?
    let onMessage: (() -> Void)?
    let onEmail: (() -> Void)?

    @Environment(\.colorScheme) private var colorScheme
    private var isDark: Bool { colorScheme == .dark }

    private var isSnoozed: Bool {
        !connection.isPastDue && !connection.isDueToday && connection.nextReminderDate > Date()
    }

    private var colorCategory: ContactColorCategory {
        let base = TimeFormatter.contactColorCategory(lastContactedDate: connection.lastContactedDate, reminderFrequencyDays: connection.reminderFrequencyDays)
        return isSnoozed ? .green : base
    }

    private var statusColor: Color {
        switch colorCategory {
        case .green: return ConnectColors.greenIndicator
        case .yellow: return ConnectColors.yellowIndicator
        case .red: return ConnectColors.redIndicator
        }
    }

    private var cardBackground: Color {
        switch colorCategory {
        case .green: return isDark ? ConnectColors.greenBackgroundDark : ConnectColors.greenBackgroundLight
        case .yellow: return isDark ? ConnectColors.yellowBackgroundDark : ConnectColors.yellowBackgroundLight
        case .red: return isDark ? ConnectColors.redBackgroundDark : ConnectColors.redBackgroundLight
        }
    }

    private var cardBorder: Color {
        if isDark {
            switch colorCategory {
            case .green: return ConnectColors.greenOutlineDark
            case .yellow: return ConnectColors.yellowOutlineDark
            case .red: return ConnectColors.redOutlineDark
            }
        }
        return statusColor.opacity(0.6)
    }

    private var textColor: Color {
        isDark ? ConnectColors.onCardDark : .primary
    }

    private var initials: String {
        let parts = connection.contactName.split(separator: " ")
        if parts.count >= 2, let f = parts.first?.first, let l = parts.last?.first {
            return "\(f)\(l)".uppercased()
        }
        return String(connection.contactName.prefix(2)).uppercased()
    }

    private var avatarColor: Color {
        if let int = connection.avatarColor { return AvatarColors.colorFromInt(int) }
        return AvatarColors.colorForName(connection.contactName)
    }

    var body: some View {
        Button(action: onTap) {
            HStack(alignment: .top, spacing: 0) {
                RoundedRectangle(cornerRadius: 2)
                    .fill(statusColor)
                    .frame(width: 4)
                    .padding(.leading, 0)
                VStack(alignment: .leading, spacing: Dimensions.xsmall) {
                    HStack {
                        avatarView
                        VStack(alignment: .leading, spacing: 2) {
                            Text(connection.contactName)
                                .font(.headline)
                                .foregroundColor(textColor)
                        }
                        Spacer(minLength: 0)
                    }
                    if let phone = connection.contactPhoneNumber, !phone.isEmpty {
                        HStack(spacing: 4) {
                            Image(systemName: "phone.fill")
                                .font(.caption)
                            Text(PhoneNumberFormatter.format(phone))
                                .font(.subheadline)
                        }
                        .foregroundColor(isDark ? ConnectColors.onCardDark.opacity(0.8) : Color.secondary)
                    }
                    if let bday = connection.birthday {
                        HStack(spacing: 4) {
                            Image(systemName: "gift")
                                .font(.caption)
                            Text(birthdayDisplay(bday))
                                .font(.caption)
                        }
                        .foregroundColor(isDark ? ConnectColors.onCardDark.opacity(0.8) : Color.secondary)
                    }
                    if connection.contactPhoneNumber != nil || connection.contactEmail != nil {
                        HStack(spacing: Dimensions.small) {
                            if let phone = connection.contactPhoneNumber, !phone.isEmpty {
                                Button { ContactHelper.makeCall(phoneNumber: phone) } label: {
                                    Image(systemName: "phone.fill")
                                        .font(.system(size: Dimensions.iconSmall))
                                }
                                .buttonStyle(.borderless)
                                Button { ContactHelper.sendMessage(phoneNumber: phone) } label: {
                                    Image(systemName: "message.fill")
                                        .font(.system(size: Dimensions.iconSmall))
                                }
                                .buttonStyle(.borderless)
                                Button { ContactHelper.faceTimeVideo(phoneNumber: phone) } label: {
                                    Image(systemName: "video.fill")
                                        .font(.system(size: Dimensions.iconSmall))
                                }
                                .buttonStyle(.borderless)
                                Button { ContactHelper.faceTimeAudio(phoneNumber: phone) } label: {
                                    Image(systemName: "phone.badge.waveform")
                                        .font(.system(size: Dimensions.iconSmall))
                                }
                                .buttonStyle(.borderless)
                            }
                            if let email = connection.contactEmail, !email.isEmpty {
                                Button { ContactHelper.sendEmail(email: email) } label: {
                                    Image(systemName: "envelope.fill")
                                        .font(.system(size: Dimensions.iconSmall))
                                }
                                .buttonStyle(.borderless)
                            }
                        }
                        .tint(ConnectColors.primary)
                    }
                    lastContactedRow
                    HStack(spacing: Dimensions.medium) {
                        HStack(spacing: Dimensions.xsmall) {
                            Text("Next: \(formatDate(connection.nextReminderDate))")
                                .font(.caption)
                                .foregroundColor(isDark ? ConnectColors.onCardDark.opacity(0.8) : Color.secondary)
                            if isSnoozed {
                                Image(systemName: "clock.arrow.circlepath")
                                    .font(.caption)
                                Text("Snoozed")
                                    .font(.caption)
                            }
                        }
                        .foregroundColor(isDark ? ConnectColors.onCardDark.opacity(0.8) : Color.secondary)
                        Spacer()
                        if connection.isDueToday || colorCategory == .green {
                            Button(action: onSnooze) {
                                Image(systemName: "clock.arrow.circlepath")
                                    .font(.system(size: Dimensions.iconSmall))
                            }
                            .buttonStyle(.borderless)
                            Button(action: onMarkComplete) {
                                Image(systemName: "checkmark.circle")
                                    .font(.system(size: Dimensions.iconSmall))
                            }
                            .buttonStyle(.borderless)
                        }
                    }
                    .tint(ConnectColors.primary)
                }
                .padding(Dimensions.cardPadding)
            }
            .background(cardBackground)
            .overlay(
                RoundedRectangle(cornerRadius: Dimensions.radiusMedium)
                    .strokeBorder(cardBorder, lineWidth: Dimensions.borderThin)
            )
            .clipShape(RoundedRectangle(cornerRadius: Dimensions.radiusMedium))
        }
        .buttonStyle(.plain)
    }

    /// Dedicated row for "Last contacted" on the card (matches Android ConnectionItemMetadata).
    @ViewBuilder
    private var lastContactedRow: some View {
        HStack(spacing: Dimensions.xsmall) {
            Text("Last contacted")
                .font(.caption)
                .foregroundColor(isDark ? ConnectColors.onCardDark.opacity(0.8) : Color.secondary)
            if let last = connection.lastContactedDate {
                Text(TimeFormatter.formatRelativeTime(date: last))
                    .font(.caption)
                    .foregroundColor(isDark ? ConnectColors.onCardDark.opacity(0.9) : Color.primary)
            } else {
                Text("Never contacted")
                    .font(.caption)
                    .italic()
                    .foregroundColor(isDark ? ConnectColors.redIndicator.opacity(0.9) : ConnectColors.redIndicator)
            }
            Spacer(minLength: 0)
        }
    }

    @ViewBuilder
    private var avatarView: some View {
        if let photoPath = connection.contactPhotoUri, !photoPath.isEmpty,
           let url = PhotoStorage.photoURL(for: photoPath),
           let uiImage = UIImage(contentsOfFile: url.path) {
            Image(uiImage: uiImage)
                .resizable()
                .scaledToFill()
                .frame(width: Dimensions.avatarMedium, height: Dimensions.avatarMedium)
                .clipShape(Circle())
        } else {
            ZStack {
                Circle()
                    .fill(avatarColor)
                    .frame(width: Dimensions.avatarMedium, height: Dimensions.avatarMedium)
                Text(initials)
                    .font(.subheadline)
                    .fontWeight(.semibold)
                    .foregroundColor(.white)
            }
        }
    }

    private func formatDate(_ d: Date) -> String {
        let f = DateFormatter()
        f.dateStyle = .short
        return f.string(from: d)
    }

    private func birthdayDisplay(_ d: Date) -> String {
        let cal = Calendar.current
        let today = Date()
        if cal.component(.month, from: d) == cal.component(.month, from: today),
           cal.component(.day, from: d) == cal.component(.day, from: today) {
            return "Birthday: Today!"
        }
        let f = DateFormatter()
        f.dateFormat = "MMM d"
        return "Birthday: \(f.string(from: d))"
    }
}
