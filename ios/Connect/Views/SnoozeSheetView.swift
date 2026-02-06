import SwiftUI

struct SnoozeSheetView: View {
    let connection: ScheduledConnection
    let onSelect: (Date) -> Void
    let onDismiss: () -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var pickADate = Calendar.current.date(byAdding: .day, value: 1, to: Date()) ?? Date()
    @State private var showDatePicker = false

    var body: some View {
        NavigationStack {
            List {
                Section {
                    Button("1 day") { pick(1, .day) }
                    Button("2 days") { pick(2, .day) }
                    Button("1 week") { pick(1, .weekOfYear) }
                    Button("1 month") { pick(1, .month) }
                }
                Section("Pick a date") {
                    DatePicker("Date", selection: $pickADate, in: minDate, displayedComponents: .date)
                    Button("Snooze until \(formatDate(pickADate))") {
                        onSelect(pickADate)
                        dismiss()
                        onDismiss()
                    }
                }
            }
            .navigationTitle("Snooze")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button("Cancel") { dismiss(); onDismiss() } }
            }
        }
    }

    private var minDate: PartialRangeFrom<Date> {
        let tomorrow = Calendar.current.date(byAdding: .day, value: 1, to: Calendar.current.startOfDay(for: Date())) ?? Date()
        return tomorrow...
    }

    private func formatDate(_ d: Date) -> String {
        let f = DateFormatter()
        f.dateStyle = .medium
        return f.string(from: d)
    }

    private func pick(_ value: Int, _ component: Calendar.Component) {
        let date = Calendar.current.date(byAdding: component, value: value, to: Date()) ?? Date()
        onSelect(date)
        dismiss()
        onDismiss()
    }
}
