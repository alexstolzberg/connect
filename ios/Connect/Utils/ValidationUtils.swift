import Foundation

enum ValidationUtils {
    private static let emailRegex = try! NSRegularExpression(
        pattern: "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    )

    static func isValidEmail(_ email: String?) -> Bool {
        guard let e = email?.trimmingCharacters(in: .whitespacesAndNewlines), !e.isEmpty else { return true }
        let range = NSRange(e.startIndex..., in: e)
        return emailRegex.firstMatch(in: e, range: range) != nil
    }

    static func isValidPhone(_ phone: String?) -> Bool {
        guard let p = phone, !p.isEmpty else { return true }
        let digits = p.filter { $0.isNumber }
        return digits.count >= 7 && digits.count <= 15
    }

    static func emailError(_ email: String?) -> String? {
        guard let e = email, !e.isEmpty else { return nil }
        return isValidEmail(e) ? nil : "Invalid email format"
    }

    static func phoneError(_ phone: String?) -> String? {
        guard let p = phone, !p.isEmpty else { return nil }
        return isValidPhone(p) ? nil : "Invalid phone number format"
    }
}
