import SwiftUI
import UIKit
import PhotosUI

struct AddEditView: View {
    @StateObject private var viewModel: AddEditViewModel
    @Environment(\.dismiss) private var dismiss
    var onSave: () -> Void
    var onViewConnection: (Int64) -> Void

    @State private var showContactPicker = false
    @State private var showContactsPermissionAlert = false
    @State private var showPhotoOptions = false
    @State private var showCamera = false
    @State private var selectedPhotoItem: PhotosPickerItem?

    init(connectionId: Int64?, onSave: @escaping () -> Void, onViewConnection: @escaping (Int64) -> Void) {
        _viewModel = StateObject(wrappedValue: AddEditViewModel(connectionId: connectionId))
        self.onSave = onSave
        self.onViewConnection = onViewConnection
    }

    private var avatarColor: Color {
        if let int = viewModel.avatarColor { return AvatarColors.colorFromInt(int) }
        return AvatarColors.colorForName(viewModel.contactName)
    }

    private var initials: String {
        let parts = viewModel.contactName.split(separator: " ")
        if parts.count >= 2, let f = parts.first?.first, let l = parts.last?.first {
            return "\(f)\(l)".uppercased()
        }
        return String(viewModel.contactName.prefix(2)).uppercased()
    }

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    HStack {
                        Spacer()
                        Button {
                            showPhotoOptions = true
                        } label: {
                            ZStack(alignment: .bottomTrailing) {
                                if let path = viewModel.contactPhotoUri, !path.isEmpty,
                                   let url = PhotoStorage.photoURL(for: path),
                                   let uiImage = UIImage(contentsOfFile: url.path) {
                                    Image(uiImage: uiImage)
                                        .resizable()
                                        .scaledToFill()
                                        .frame(width: Dimensions.avatarXLarge, height: Dimensions.avatarXLarge)
                                        .clipShape(Circle())
                                } else {
                                    ZStack {
                                        Circle()
                                            .fill(avatarColor)
                                            .frame(width: Dimensions.avatarXLarge, height: Dimensions.avatarXLarge)
                                        Text(initials)
                                            .font(.title)
                                            .fontWeight(.bold)
                                            .foregroundColor(.white)
                                    }
                                }
                                Image(systemName: "pencil.circle.fill")
                                    .font(.title2)
                                    .foregroundColor(.white)
                                    .background(Circle().fill(ConnectColors.primary))
                                    .offset(x: 4, y: 4)
                            }
                        }
                        .buttonStyle(.plain)
                        Spacer()
                    }
                    .listRowBackground(Color.clear)
                    .listRowInsets(EdgeInsets(top: Dimensions.medium, leading: 0, bottom: Dimensions.medium, trailing: 0))
                }
                Section("Contact") {
                    Button {
                        showContactPicker = true
                    } label: {
                        Label("Pick from contacts", systemImage: "person.crop.circle.badge.plus")
                    }
                    TextField("Name", text: $viewModel.contactName)
                    TextField("Phone", text: Binding(
                        get: { viewModel.contactPhoneNumber ?? "" },
                        set: {
                            let formatted = PhoneNumberFormatter.format($0)
                            viewModel.contactPhoneNumber = formatted.isEmpty ? nil : formatted
                        }
                    ))
                        .keyboardType(.phonePad)
                    if let err = ValidationUtils.phoneError(viewModel.contactPhoneNumber) {
                        Text(err).font(.caption).foregroundStyle(.red)
                    }
                    TextField("Email", text: Binding(get: { viewModel.contactEmail ?? "" }, set: { viewModel.contactEmail = $0.isEmpty ? nil : $0 }))
                        .keyboardType(.emailAddress)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
                    if let err = ValidationUtils.emailError(viewModel.contactEmail) {
                        Text(err).font(.caption).foregroundStyle(.red)
                    }
                }
                Section {
                    Picker("Frequency", selection: $viewModel.reminderFrequencyDays) {
                        Text("Daily").tag(1)
                        Text("Weekly").tag(7)
                        Text("Biweekly").tag(14)
                        Text("Monthly").tag(30)
                        Text("Custom").tag(0)
                    }
                    if viewModel.reminderFrequencyDays == 0 {
                        Stepper("Every \(viewModel.customFrequencyDays) days", value: $viewModel.customFrequencyDays, in: 1...365)
                    }
                    Picker("Preferred method", selection: $viewModel.preferredMethod) {
                        ForEach(preferredMethodOptions, id: \.self) { Text($0.displayName).tag($0) }
                    }
                    .onChange(of: viewModel.contactPhoneNumber) { _ in viewModel.ensurePreferredMethodValid() }
                    .onChange(of: viewModel.contactEmail) { _ in viewModel.ensurePreferredMethodValid() }
                    .onAppear { viewModel.ensurePreferredMethodValid() }
                    DatePicker("Next reminder", selection: $viewModel.nextReminderDate, displayedComponents: .date)
                    reminderTimePicker
                } header: {
                    Text("Reminder")
                } footer: {
                    Text("Frequency defaults to weekly. Set a reminder time for when you want to be notified.")
                }
                Section("Birthday") {
                    DatePicker("Birthday", selection: Binding(
                        get: { viewModel.birthday ?? Calendar.current.date(bySettingHour: 12, minute: 0, second: 0, of: Date()) ?? Date() },
                        set: { viewModel.birthday = $0 }
                    ), displayedComponents: .date)
                    Toggle("Prompt on birthday", isOn: $viewModel.promptOnBirthday)
                }
                Section {
                    TextField("Notes", text: Binding(get: { viewModel.notes ?? "" }, set: { viewModel.notes = $0.isEmpty ? nil : $0 }), axis: .vertical)
                        .lineLimit(3...6)
                }
            }
            .navigationTitle(viewModel.connectionId == nil ? "New connection" : "Edit connection")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button("Cancel") { dismiss() } }
                ToolbarItem(placement: .confirmationAction) { Button("Save") { save() } }
            }
            .alert("Save failed", isPresented: Binding(
                get: { viewModel.saveResult?.errorMessage != nil },
                set: { if !$0 { viewModel.clearSaveResult() } }
            )) {
                Button("OK") { viewModel.clearSaveResult() }
            } message: {
                Text(viewModel.saveResult?.errorMessage ?? "")
            }
            .alert("Possible duplicate", isPresented: Binding(
                get: { viewModel.duplicateCandidates != nil },
                set: { if !$0 { viewModel.clearDuplicateCandidates() } }
            )) {
                Button(viewModel.connectionId == nil ? "Add anyway" : "Save anyway") { viewModel.saveAnyway() }
                Button("Cancel", role: .cancel) { viewModel.clearDuplicateCandidates() }
            } message: {
                if let match = viewModel.duplicateMatchDescription, !match.isEmpty {
                    Text("A connection may already exist. Matches: \(match).")
                } else {
                    Text("A connection with this name, phone, or email may already exist.")
                }
            }
            .sheet(isPresented: $showContactPicker) {
                ContactPickerView(
                    onSelect: { picked in
                        viewModel.applyPickedContact(name: picked.name, phone: picked.phone, email: picked.email)
                        showContactPicker = false
                    },
                    onCancel: { showContactPicker = false },
                    onPermissionDenied: { showContactsPermissionAlert = true; showContactPicker = false }
                )
            }
            .alert("Contacts access", isPresented: $showContactsPermissionAlert) {
                Button("Open Settings") {
                    if let url = URL(string: UIApplication.openSettingsURLString) {
                        UIApplication.shared.open(url)
                    }
                    showContactsPermissionAlert = false
                }
                Button("OK", role: .cancel) { showContactsPermissionAlert = false }
            } message: {
                Text("Connect needs access to your contacts to let you pick someone. You can enable it in Settings.")
            }
            .sheet(isPresented: $showPhotoOptions) {
                photoOptionsSheet
            }
            .sheet(isPresented: $showCamera) {
                CameraPickerView { image in
                    applyPickedImage(image)
                    showCamera = false
                }
            }
            .onChange(of: selectedPhotoItem) { _ in
                Task {
                    if let item = selectedPhotoItem,
                       let data = try? await item.loadTransferable(type: Data.self),
                       let image = UIImage(data: data) {
                        await MainActor.run {
                            applyPickedImage(image)
                            showPhotoOptions = false
                        }
                    }
                    await MainActor.run { selectedPhotoItem = nil }
                }
            }
        }
        .onChange(of: viewModel.saveResult, perform: { result in
            if result != nil { handleSaveResult() }
        })
    }

    private var reminderTimePicker: some View {
        let binding = Binding<Date>(
            get: {
                ReminderTimeHelper.date(from: viewModel.reminderTime ?? viewModel.defaultReminderTime) ?? Date()
            },
            set: { viewModel.reminderTime = ReminderTimeHelper.string(from: $0) }
        )
        return DatePicker("Reminder time", selection: binding, displayedComponents: .hourAndMinute)
    }

    /// Only show preferred methods that are valid: call/message/faceTime require phone, email requires email, both requires at least one.
    private var preferredMethodOptions: [ConnectionMethod] {
        let hasPhone = (viewModel.contactPhoneNumber?.trimmingCharacters(in: .whitespaces)).map { !$0.isEmpty } ?? false
        let hasEmail = (viewModel.contactEmail?.trimmingCharacters(in: .whitespaces)).map { !$0.isEmpty } ?? false
        let isPhoneDevice = UIDevice.current.userInterfaceIdiom == .phone
        var options: [ConnectionMethod] = []
        if hasPhone {
            options.append(contentsOf: [.call, .message])
            if isPhoneDevice { options.append(.faceTime) }
        }
        if hasEmail { options.append(.email) }
        if hasPhone || hasEmail { options.append(.both) }
        if options.isEmpty { options = [.both] }
        if viewModel.preferredMethod == .faceTime, !options.contains(.faceTime), isPhoneDevice {
            options.append(.faceTime)
        }
        return options
    }

    private func applyPickedImage(_ image: UIImage) {
        if let id = viewModel.connectionId, id > 0 {
            if let path = PhotoStorage.savePhoto(image, connectionId: id) {
                viewModel.updateContactPhotoUri(path)
            }
        } else {
            if let path = PhotoStorage.savePhotoForNewConnection(image) {
                viewModel.updateContactPhotoUri(path)
            }
        }
    }

    @ViewBuilder
    private var photoOptionsSheet: some View {
        NavigationStack {
            List {
                Section {
                    PhotosPicker(selection: $selectedPhotoItem, matching: .images) {
                        Label("Choose from Gallery", systemImage: "photo")
                    }
                    Button {
                        showPhotoOptions = false
                        showCamera = true
                    } label: {
                        Label("Take Photo", systemImage: "camera")
                    }
                    if viewModel.contactPhotoUri != nil {
                        Button(role: .destructive) {
                            PhotoStorage.deletePhoto(at: viewModel.contactPhotoUri)
                            viewModel.updateContactPhotoUri(nil)
                            showPhotoOptions = false
                        } label: {
                            Label("Remove Photo", systemImage: "trash")
                        }
                    }
                }
                Section("Choose avatar color") {
                    LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: Dimensions.small), count: 5), spacing: Dimensions.small) {
                        ForEach(0..<AvatarColors.argbValues.count, id: \.self) { index in
                            let argb = AvatarColors.argbValues[index]
                            let isSelected = viewModel.avatarColor == argb
                            Button {
                                viewModel.updateAvatarColor(argb)
                            } label: {
                                Circle()
                                    .fill(AvatarColors.colorFromInt(argb))
                                    .frame(width: Dimensions.avatarMedium, height: Dimensions.avatarMedium)
                                    .overlay(
                                        Circle()
                                            .strokeBorder(isSelected ? ConnectColors.primary : Color.clear, lineWidth: 3)
                                    )
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .padding(.vertical, Dimensions.small)
                }
            }
            .navigationTitle("Photo & avatar")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Done") { showPhotoOptions = false }
                }
            }
        }
    }

    private func save() {
        viewModel.save()
    }

    private func handleSaveResult() {
        if case .success = viewModel.saveResult {
            viewModel.clearSaveResult()
            if let id = viewModel.connectionId, id > 0 {
                onViewConnection(id)
            } else if let id = viewModel.lastInsertedId {
                onViewConnection(id)
            }
            onSave()
            dismiss()
        }
    }
}

extension SaveResult {
    var errorMessage: String? {
        if case .error(let m) = self { return m }
        return nil
    }
}
