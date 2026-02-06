import SwiftUI

@main
struct ConnectApp: App {
    @StateObject private var prefs = Preferences.shared
    @State private var showSplash = true

    var body: some Scene {
        WindowGroup {
            Group {
                if showSplash {
                    SplashView(onFinish: { showSplash = false })
                } else {
                    MainTabView()
                        .preferredColorScheme(prefs.themeMode == .light ? .light : prefs.themeMode == .dark ? .dark : nil)
                }
            }
            .onAppear {
                // Start loading connections during splash so Inbox has data when first shown
                _ = ConnectionStore.shared
            }
            .tint(ConnectColors.primary)
        }
    }
}
