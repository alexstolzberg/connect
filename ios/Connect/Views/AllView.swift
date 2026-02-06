import SwiftUI

struct AllView: View {
    @StateObject private var viewModel = HomeViewModel()
    var onAdd: () -> Void
    var onConnectionTap: (Int64) -> Void
    var onShowSnackbar: (String) -> Void = { _ in }

    @State private var showSnoozeFor: ScheduledConnection?
    @State private var searchText = ""
    @State private var sortMenu = false

    var body: some View {
        Group {
            if viewModel.allConnections.isEmpty && viewModel.searchQuery.isEmpty {
                EmptyStateView(title: "No connections", systemImage: "person.2", description: "Add a connection to see them here.")
            } else {
                List {
                    ForEach(viewModel.allConnections) { conn in
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
                .listStyle(.insetGrouped)
            }
        }
        .navigationTitle("All")
        .searchable(text: $searchText, prompt: "Search")
        .onChange(of: searchText) { viewModel.setSearchQuery($0) }
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Menu {
                    Button("A–Z") { viewModel.setAllSortOrder(.a_z) }
                    Button("Date (soonest first)") { viewModel.setAllSortOrder(.dateAscending) }
                    Button("Date (latest first)") { viewModel.setAllSortOrder(.dateDescending) }
                } label: { Image(systemName: "arrow.up.arrow.down.circle") }
            }
            ToolbarItem(placement: .primaryAction) { Button(action: onAdd) { Image(systemName: "plus") } }
        }
        .refreshable { viewModel.refresh() }
        .onAppear { viewModel.syncFromStore() }
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
