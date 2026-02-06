import SwiftUI

struct SplashView: View {
    var onFinish: () -> Void

    @State private var opacity: Double = 0

    private var gradient: LinearGradient {
        LinearGradient(
            colors: [
                Color(red: 0.72, green: 0.77, blue: 0.91),  // #B8C5E8
                Color(red: 0.61, green: 0.49, blue: 0.85),  // #9B7DD9
                Color(red: 0.85, green: 0.47, blue: 0.62),  // #D9779F
                Color(red: 1, green: 0.42, blue: 0.29)       // #FF6B4A
            ],
            startPoint: .topLeading,
            endPoint: .bottomTrailing
        )
    }

    private let squareSize: CGFloat = 280
    private let cornerRadius: CGFloat = 24

    var body: some View {
        ZStack {
            gradient
                .ignoresSafeArea()
            VStack(spacing: Dimensions.large) {
                Image("SplashIcon")
                    .resizable()
                    .scaledToFit()
                    .frame(width: Dimensions.avatarXLarge, height: Dimensions.avatarXLarge)
                    .opacity(opacity)
                Text("Connect")
                    .font(.system(size: 48, weight: .bold))
                    .foregroundColor(.primary)
                    .opacity(opacity)
                Text("Stay connected with the people who matter")
                    .font(.system(size: 16))
                    .foregroundColor(.secondary)
                    .opacity(opacity)
            }
            .frame(width: squareSize, height: squareSize)
            .background(Color.white)
            .clipShape(RoundedRectangle(cornerRadius: cornerRadius))
        }
        .onAppear {
            withAnimation(.easeIn(duration: 1.0)) {
                opacity = 1
            }
            DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
                onFinish()
            }
        }
    }
}
