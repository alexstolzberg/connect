import SwiftUI
import UIKit

struct ConnectionDetailsView: View {
    @StateObject private var viewModel: ConnectionDetailsViewModel
    @Environment(\.dismiss) private var dismiss
    var onEdit: (Int64) -> Void
    var onDelete: () -> Void
    var onShowSnackbar: (String) -> Void = { _ in }

    @State private var showFirstDeleteConfirm = false
    @State private var showSecondDeleteConfirm = false

    init(connectionId: Int64, onEdit: @escaping (Int64) -> Void, onDelete: @escaping () -> Void, onShowSnackbar: @escaping (String) -> Void) {
        _viewModel = StateObject(wrappedValue: ConnectionDetailsViewModel(connectionId: connectionId))
        self.onEdit = onEdit
        self.onDelete = onDelete
        self.onShowSnackbar = onShowSnackbar
    }

    var body: some View {
        Group {
            if viewModel.isLoading {
                ProgressView()
            } else if let conn = viewModel.connection {
                detailsContent(conn)
            } else {
                EmptyStateView(title: "Connection not found", systemImage: "person.slash", description: nil)
            }
        }
        .navigationTitle(viewModel.connection?.contactName ?? "Details")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear { viewModel.load() }
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                if viewModel.connection != nil {
                    Button("Edit") { onEdit(viewModel.connectionId) }
                }
            }
        }
        .onChange(of: viewModel.deleteResult, perform: { r in
            if case .success = r { onDelete(); dismiss() }
        })
        .alert("Delete connection?", isPresented: $showFirstDeleteConfirm) {
            Button("Cancel", role: .cancel) { showFirstDeleteConfirm = false }
            Button("Delete", role: .destructive) {
                showFirstDeleteConfirm = false
                showSecondDeleteConfirm = true
            }
        } message: {
            if let name = viewModel.connection?.contactName {
                Text("Are you sure you want to delete \(name)?")
            }
        }
        .alert("Delete permanently?", isPresented: $showSecondDeleteConfirm) {
            Button("Cancel", role: .cancel) { showSecondDeleteConfirm = false }
            Button("Delete", role: .destructive) {
                showSecondDeleteConfirm = false
                viewModel.deleteConnection()
            }
        } message: {
            if let name = viewModel.connection?.contactName {
                Text("This cannot be undone. Permanently delete \(name)?")
            }
        }
    }

    private func detailsContent(_ conn: ScheduledConnection) -> some View {
        List {
            Section {
                HStack {
                    Spacer()
                    connectionAvatar(conn)
                    Spacer()
                }
                .listRowBackground(Color.clear)
            }
            Section {
                if let phone = conn.contactPhoneNumber, !phone.isEmpty {
                    if let url = ContactHelper.callURL(phoneNumber: phone) {
                        Button { ContactHelper.open(url) } label: {
                            Label("Call", systemImage: "phone.fill")
                        }
                    }
                    if let url = ContactHelper.messageURL(phoneNumber: phone) {
                        Button { ContactHelper.open(url) } label: {
                            Label("Message", systemImage: "message.fill")
                        }
                    }
                    if let url = ContactHelper.faceTimeVideoURL(phoneNumber: phone) {
                        Button { ContactHelper.open(url) } label: {
                            Label("FaceTime", systemImage: "video.fill")
                        }
                    }
                    if let url = ContactHelper.faceTimeAudioURL(phoneNumber: phone) {
                        Button { ContactHelper.open(url) } label: {
                            Label("FaceTime Audio", systemImage: "video.badge.waveform.fill")
                        }
                    }
                }
                if let email = conn.contactEmail, !email.isEmpty,
                   let url = ContactHelper.emailURL(email: email) {
                    Button { ContactHelper.open(url) } label: {
                        Label("Email", systemImage: "envelope.fill")
                    }
                }
            }
            Section {
                if let phone = conn.contactPhoneNumber, !phone.isEmpty {
                    LabeledContent("Phone", value: PhoneNumberFormatter.format(phone))
                }
                if let email = conn.contactEmail, !email.isEmpty {
                    LabeledContent("Email", value: email)
                }
                if let bday = conn.birthday {
                    LabeledContent("Birthday", value: formatBirthday(bday))
                }
            }
            Section {
                if let last = conn.lastContactedDate {
                    Text("Last contacted: \(TimeFormatter.formatRelativeTime(date: last))")
                }
                Text("Next reminder: \(formatDate(conn.nextReminderDate))")
                if let time = conn.reminderTime, !time.isEmpty {
                    Text("Reminder time: \(formatReminderTime(time))")
                }
                Text("Frequency: every \(conn.reminderFrequencyDays) days")
            }
            if let notes = conn.notes, !notes.isEmpty {
                Section("Notes") { Text(notes) }
            }
            if conn.isDueToday {
                Section {
                    Button {
                        viewModel.markAsContacted()
                        onShowSnackbar("Marked as contacted")
                    } label: {
                        Label("Mark as Contacted", systemImage: "checkmark.circle")
                    }
                }
            }
            Section {
                Button(role: .destructive, action: { showFirstDeleteConfirm = true }) {
                    Label("Delete", systemImage: "trash")
                }
            }
        }
        .listStyle(.insetGrouped)
    }

    private func formatDate(_ d: Date) -> String {
        let f = DateFormatter()
        f.dateStyle = .medium
        return f.string(from: d)
    }

    private func formatBirthday(_ d: Date) -> String {
        let f = DateFormatter()
        f.dateFormat = "MMM d, yyyy"
        return f.string(from: d)
    }

    private func formatReminderTime(_ time: String) -> String {
        let parts = time.split(separator: ":")
        let h = Int(parts.first ?? "0") ?? 0
        let m = parts.count > 1 ? (Int(parts[1]) ?? 0) : 0
        let hour12 = h % 12 == 0 ? 12 : h % 12
        let amPm = h < 12 ? "AM" : "PM"
        return String(format: "%d:%02d %@", hour12, m, amPm)
    }

    @ViewBuilder
    private func connectionAvatar(_ conn: ScheduledConnection) -> some View {
        if let path = conn.contactPhotoUri, !path.isEmpty,
           let url = PhotoStorage.photoURL(for: path),
           let uiImage = UIImage(contentsOfFile: url.path) {
            Image(uiImage: uiImage)
                .resizable()
                .scaledToFill()
                .frame(width: Dimensions.avatarXLarge, height: Dimensions.avatarXLarge)
                .clipShape(Circle())
        } else {
            let color = conn.avatarColor.map { AvatarColors.colorFromInt($0) } ?? AvatarColors.colorForName(conn.contactName)
            let parts = conn.contactName.split(separator: " ")
            let initials = parts.count >= 2 && parts.first?.first != nil && parts.last?.first != nil
                ? "\(parts.first!.first!)\(parts.last!.first!)".uppercased()
                : String(conn.contactName.prefix(2)).uppercased()
            ZStack {
                Circle()
                    .fill(color)
                    .frame(width: Dimensions.avatarXLarge, height: Dimensions.avatarXLarge)
                Text(initials)
                    .font(.largeTitle)
                    .fontWeight(.bold)
                    .foregroundColor(.white)
            }
        }
    }
}
