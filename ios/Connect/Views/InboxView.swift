import SwiftUI

struct InboxView: View {
    @StateObject private var viewModel = HomeViewModel()
    var onAdd: () -> Void
    var onConnectionTap: (Int64) -> Void
    var onShowSnackbar: (String) -> Void = { _ in }

    @State private var showSnoozeFor: ScheduledConnection?
    @State private var searchText = ""

    var body: some View {
        Group {
            if viewModel.inboxSections.isEmpty && viewModel.searchQuery.isEmpty {
                EmptyStateView(title: "No reminders", systemImage: "person.2", description: "Add a connection to get started.")
            } else {
                List {
                    ForEach(viewModel.inboxSections) { section in
                        Section(section.title) {
                            ForEach(section.connections) { conn in
                                ConnectionRowView(
                                    connection: conn,
                                    onTap: { onConnectionTap(conn.id) },
                                    onSnooze: { showSnoozeFor = conn },
                                    onMarkComplete: {
                                        viewModel.markAsContacted(conn)
                                        viewModel.syncFromStore()
                                        onShowSnackbar("Marked as contacted")
                                    },
                                    onCall: nil,
                                    onMessage: nil,
                                    onEmail: nil
                                )
                            }
                        }
                    }
                }
                .listStyle(.insetGrouped)
            }
        }
        .navigationTitle("Inbox")
        .searchable(text: $searchText, prompt: "Search")
        .onChange(of: searchText) { viewModel.setSearchQuery($0) }
        .toolbar {
            ToolbarItem(placement: .primaryAction) { Button(action: onAdd) { Image(systemName: "plus") } }
        }
        .refreshable { viewModel.refresh() }
        .onAppear {
            viewModel.syncFromStore()
            // If store was still loading on first paint, sync again so list appears
            if viewModel.inboxSections.isEmpty {
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) { viewModel.syncFromStore() }
            }
        }
        .sheet(item: $showSnoozeFor) { conn in
            SnoozeSheetView(connection: conn) { date in
                viewModel.snooze(conn, until: date)
                viewModel.syncFromStore()
                showSnoozeFor = nil
                onShowSnackbar("Snoozed")
            } onDismiss: { showSnoozeFor = nil }
        }
    }
}

