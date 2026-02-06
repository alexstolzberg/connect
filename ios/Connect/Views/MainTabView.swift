import SwiftUI

private struct IdentifiableInt64: Identifiable {
    let id: Int64
}

struct MainTabView: View {
    @State private var selectedTab = 0
    @State private var pathInbox = NavigationPath()
    @State private var pathAll = NavigationPath()
    @State private var pathSettings = NavigationPath()
    @State private var showAddEdit = false
    @State private var editConnectionId: IdentifiableInt64?
    @State private var snackbarMessage: String?
    @State private var connectionIdToView: Int64?

    var body: some View {
        TabView(selection: $selectedTab) {
            NavigationStack(path: $pathInbox) {
                InboxView(
                    onAdd: { showAddEdit = true },
                    onConnectionTap: { id in pathInbox.append(id) },
                    onShowSnackbar: { snackbarMessage = $0 }
                )
                .navigationDestination(for: Int64.self) { id in
                    ConnectionDetailsView(
                        connectionId: id,
                        onEdit: { editConnectionId = IdentifiableInt64(id: $0) },
                        onDelete: { pathInbox.removeLast(pathInbox.count) },
                        onShowSnackbar: { snackbarMessage = $0 }
                    )
                }
            }
            .tabItem { Label("Inbox", systemImage: "tray.fill") }
            .tag(0)

            NavigationStack(path: $pathAll) {
                AllView(
                    onAdd: { showAddEdit = true },
                    onConnectionTap: { id in pathAll.append(id) },
                    onShowSnackbar: { snackbarMessage = $0 }
                )
                .navigationDestination(for: Int64.self) { id in
                    ConnectionDetailsView(
                        connectionId: id,
                        onEdit: { editConnectionId = IdentifiableInt64(id: $0) },
                        onDelete: { pathAll.removeLast(pathAll.count) },
                        onShowSnackbar: { snackbarMessage = $0 }
                    )
                }
            }
            .tabItem { Label("All", systemImage: "list.bullet") }
            .tag(1)

            NavigationStack(path: $pathSettings) {
                SettingsView(onAbout: { pathSettings.append("about") })
                    .navigationDestination(for: String.self) { value in
                        if value == "about" { AboutView() }
                    }
            }
            .tabItem { Label("Settings", systemImage: "gearshape.fill") }
            .tag(2)
        }
        .sheet(isPresented: $showAddEdit) {
            AddEditView(connectionId: nil, onSave: { showAddEdit = false }) { id in
                showAddEdit = false
                connectionIdToView = id
            }
            .onDisappear {
                if let id = connectionIdToView {
                    connectionIdToView = nil
                    selectedTab = 0
                    pathInbox.append(id)
                }
            }
        }
        .sheet(item: $editConnectionId) { wrap in
            AddEditView(connectionId: wrap.id, onSave: { editConnectionId = nil }) { _ in
                editConnectionId = nil
            }
        }
        .overlay(alignment: .bottom) {
            if let msg = snackbarMessage {
                Text(msg)
                    .padding()
                    .background(.ultraThinMaterial)
                    .clipShape(RoundedRectangle(cornerRadius: 8))
                    .padding(.bottom, 50)
                    .onAppear {
                        DispatchQueue.main.asyncAfter(deadline: .now() + 2) { snackbarMessage = nil }
                    }
            }
        }
    }
}
