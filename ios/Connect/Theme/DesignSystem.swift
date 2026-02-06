import SwiftUI

enum Dimensions {
    static let xxsmall: CGFloat = 4
    static let xsmall: CGFloat = 8
    static let small: CGFloat = 12
    static let medium: CGFloat = 16
    static let large: CGFloat = 24
    static let cardPadding: CGFloat = 16
    static let screenPadding: CGFloat = 16
    static let listItemSpacing: CGFloat = 8
    static let iconSmall: CGFloat = 18
    static let iconMedium: CGFloat = 24
    static let avatarMedium: CGFloat = 40
    static let avatarXLarge: CGFloat = 120
    static let radiusSmall: CGFloat = 8
    static let radiusMedium: CGFloat = 12
    static let borderThin: CGFloat = 1
}

enum ConnectColors {
    /// Matches Android ConnectPrimary (0xFF6366F1)
    static let primary = Color(red: 99/255.0, green: 102/255.0, blue: 241/255.0)
    static let greenIndicator = Color(red: 0.3, green: 0.69, blue: 0.31)
    static let yellowIndicator = Color(red: 1, green: 0.76, blue: 0.03)
    static let redIndicator = Color(red: 0.96, green: 0.26, blue: 0.21)

    // Card backgrounds (light)
    static let greenBackgroundLight = Color(red: 0.91, green: 0.96, blue: 0.91)
    static let yellowBackgroundLight = Color(red: 1, green: 0.97, blue: 0.88)
    static let redBackgroundLight = Color(red: 1, green: 0.92, blue: 0.93)

    // Card backgrounds (dark)
    static let greenBackgroundDark = Color(red: 0.72, green: 0.85, blue: 0.73)
    static let yellowBackgroundDark = Color(red: 0.93, green: 0.91, blue: 0.66)
    static let redBackgroundDark = Color(red: 0.94, green: 0.72, blue: 0.69)

    // Card outline (dark)
    static let greenOutlineDark = Color(red: 0.4, green: 0.73, blue: 0.42)
    static let yellowOutlineDark = Color(red: 0.83, green: 0.82, blue: 0.45)
    static let redOutlineDark = Color(red: 0.82, green: 0.54, blue: 0.51)

    static let onCardDark = Color.black
}

/// Avatar background colors (match Android palette). Store ARGB int in model.
enum AvatarColors {
    static let argbValues: [Int] = [
        0xFF9B87F5, 0xFFFF6B9D, 0xFFFFA07A, 0xFFFFB6C1, 0xFFFFD700,
        0xFF90EE90, 0xFF87CEEB, 0xFFDDA0DD, 0xFFFF6347, 0xFF40E0D0,
        0xFFFFA500, 0xFF98D8C8, 0xFFFF69B4, 0xFF9370DB, 0xFF20B2AA,
        0xFFFF8C00, 0xFFBA55D3, 0xFF00CED1, 0xFFFF1493, 0xFF7B68EE,
    ]

    static var colors: [Color] { argbValues.map { colorFromInt($0) } }

    static func colorForName(_ name: String) -> Color {
        let index = abs(name.hashValue) % argbValues.count
        return colorFromInt(argbValues[index])
    }

    static func colorByIndex(_ index: Int) -> Color {
        colorFromInt(argbValues[index % argbValues.count])
    }

    static func colorFromInt(_ value: Int) -> Color {
        let a = Double((value >> 24) & 0xFF) / 255
        let r = Double((value >> 16) & 0xFF) / 255
        let g = Double((value >> 8) & 0xFF) / 255
        let b = Double(value & 0xFF) / 255
        return Color(red: r, green: g, blue: b, opacity: a)
    }
}

/// iOS 16–compatible empty state (replaces ContentUnavailableView which is iOS 17+).
struct EmptyStateView: View {
    let title: String
    let systemImage: String
    let description: String?

    var body: some View {
        VStack(spacing: Dimensions.small) {
            Image(systemName: systemImage)
                .font(.system(size: 48))
                .foregroundStyle(.secondary)
            Text(title)
                .font(.title2)
            if let d = description {
                Text(d)
                    .font(.body)
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
            }
        }
        .padding()
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
