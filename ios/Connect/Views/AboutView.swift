import SwiftUI

struct AboutView: View {
    @Environment(\.dismiss) private var dismiss

    private var versionString: String {
        (Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String) ?? "1.0"
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: Dimensions.medium) {
                // Title and version
                HStack(spacing: Dimensions.medium) {
                    Image(systemName: "info.circle.fill")
                        .font(.system(size: 44))
                        .foregroundColor(ConnectColors.primary)
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Connect")
                            .font(.title.bold())
                        Text("Version \(versionString)")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                    Spacer()
                }
                .padding(.vertical, Dimensions.small)

                Divider()

                sectionTitle("Purpose")
                Text("Connect helps you maintain meaningful relationships by reminding you to reach out to the people who matter most. Life gets busy, and it's easy to lose touch with friends, family, and colleagues. This app ensures you never forget to stay connected.")
                    .font(.body)
                    .foregroundColor(.primary)

                sectionTitle("How to Use")
                VStack(alignment: .leading, spacing: Dimensions.medium) {
                    howToStep(1, "Add Connections", "Tap the + button to add a new connection. Pick a contact from your device or add one manually. Set how often you want to be reminded to reach out.")
                    howToStep(2, "View Your Reminders", "Check the Inbox to see connections due soon, or the All tab to see every connection. Connections are color-coded based on how recently you've contacted them (see Color Coding below).")
                    howToStep(3, "Stay Connected", "When it's time to reach out, tap the checkmark to mark as contacted, or use the call/message buttons to contact them directly. The app will automatically schedule your next reminder.")
                    howToStep(4, "Manage Connections", "Tap any connection to view details, edit settings, or delete if needed. You can also add notes and birthdays for each connection.")
                }

                Divider()

                sectionTitle("Color Coding")
                Text("Connections are color-coded to help you quickly see who needs attention:")
                    .font(.body)
                VStack(alignment: .leading, spacing: Dimensions.small) {
                    colorRule("Green", Color(red: 0.3, green: 0.69, blue: 0.31), "Contacted within your reminder frequency (e.g., within 7 days for weekly reminders)")
                    colorRule("Yellow", Color(red: 1, green: 0.76, blue: 0.03), "Overdue by up to one reminder period (e.g., 7–14 days for weekly reminders)")
                    colorRule("Red", Color(red: 0.96, green: 0.27, blue: 0.21), "Overdue by more than one reminder period or never contacted")
                }

                Divider()

                sectionTitle("Features")
                VStack(alignment: .leading, spacing: Dimensions.small) {
                    featureItem("Flexible Reminders", "Set daily, weekly, biweekly, or monthly reminder frequencies")
                    featureItem("Multiple Contact Methods", "Call, message, FaceTime, or email your contacts directly from the app")
                    featureItem("Visual Indicators", "Color-coded contacts help you see who needs attention")
                    featureItem("Notes & Birthdays", "Keep track of important information and special dates")
                    featureItem("Automatic Scheduling", "The app automatically calculates your next reminder date")
                }

                Text("Stay connected with the people who matter most.")
                    .font(.body)
                    .fontWeight(.medium)
                    .foregroundColor(ConnectColors.primary)
                    .padding(.top, Dimensions.small)

                Spacer(minLength: Dimensions.large)
            }
            .padding(Dimensions.screenPadding)
        }
        .navigationTitle("About")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .cancellationAction) { Button("Done") { dismiss() } }
        }
    }

    private func sectionTitle(_ title: String) -> some View {
        Text(title)
            .font(.title2.bold())
    }

    private func howToStep(_ number: Int, _ title: String, _ description: String) -> some View {
        HStack(alignment: .top, spacing: Dimensions.medium) {
            Text("\(number)")
                .font(.headline)
                .frame(width: 28, height: 28)
                .background(ConnectColors.primary.opacity(0.2))
                .clipShape(Circle())
            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.subheadline.bold())
                Text(description)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            Spacer(minLength: 0)
        }
    }

    private func colorRule(_ name: String, _ color: Color, _ description: String) -> some View {
        HStack(alignment: .top, spacing: Dimensions.small) {
            RoundedRectangle(cornerRadius: 4)
                .fill(color)
                .frame(width: 12, height: 12)
                .padding(.top, 4)
            VStack(alignment: .leading, spacing: 2) {
                Text(name)
                    .font(.subheadline.bold())
                Text(description)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            Spacer(minLength: 0)
        }
    }

    private func featureItem(_ title: String, _ description: String) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(title)
                .font(.subheadline.bold())
            Text(description)
                .font(.caption)
                .foregroundColor(.secondary)
        }
    }
}
