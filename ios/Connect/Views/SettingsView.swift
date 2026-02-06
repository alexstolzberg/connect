import SwiftUI

struct SettingsView: View {
    @StateObject private var viewModel = SettingsViewModel()
    var onAbout: () -> Void

    var body: some View {
        Form {
            Section("Appearance") {
                Picker("Theme", selection: $viewModel.themeMode) {
                    ForEach(ThemeMode.allCases, id: \.self) { Text($0.displayName).tag($0) }
                }
            }
            Section("Notifications") {
                Toggle("Reminders", isOn: $viewModel.notificationsEnabled)
                    .onChange(of: viewModel.notificationsEnabled) { on in
                        if on {
                            NotificationService.requestAuthorization { _ in }
                        }
                    }
            }
            Section("Defaults") {
                DatePicker(
                    "Default reminder time",
                    selection: Binding(
                        get: {
                            ReminderTimeHelper.date(from: viewModel.defaultReminderTime) ?? Date()
                        },
                        set: {
                            viewModel.setDefaultReminderTime(ReminderTimeHelper.string(from: $0))
                        }
                    ),
                    displayedComponents: .hourAndMinute
                )
            }
            Section {
                Button("About") { onAbout() }
            }
        }
        .navigationTitle("Settings")
    }
}
