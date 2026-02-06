import Foundation

enum PhoneNumberFormatter {
    /// Format for display (e.g. 555-123-4567 or +1 (555) 123-4567). Matches Android logic.
    static func format(_ phoneNumber: String?) -> String {
        guard let raw = phoneNumber, !raw.isEmpty else { return "" }
        let digits = raw.filter { $0.isNumber || $0 == "+" }
        let hasPlus = digits.hasPrefix("+")
        let clean = hasPlus ? String(digits.dropFirst()) : digits
        guard !clean.isEmpty else { return raw }
        let d = clean.filter(\.isNumber)
        switch d.count {
        case 0: return raw
        case 1...3: return hasPlus ? "+\(d)" : d
        case 4...6:
            let a = d.prefix(3)
            let b = d.dropFirst(3)
            return hasPlus ? "+\(a)-\(b)" : "\(a)-\(b)"
        case 7...10:
            let a = d.prefix(3)
            let b = d.dropFirst(3).prefix(3)
            let c = d.dropFirst(6)
            return hasPlus ? "+\(a)-\(b)-\(c)" : "\(a)-\(b)-\(c)"
        case 11 where d.hasPrefix("1"):
            return "+1 (\(d.dropFirst(1).prefix(3))) \(d.dropFirst(4).prefix(3))-\(d.dropFirst(7))"
        default:
            if d.count > 10 {
                let country = d.dropLast(10)
                let area = d.suffix(10).prefix(3)
                let first = d.suffix(7).prefix(3)
                let last = d.suffix(4)
                return "+\(country) (\(area)) \(first)-\(last)"
            }
            return "\(d.prefix(3))-\(d.dropFirst(3).prefix(3))-\(d.dropFirst(6))"
        }
    }
}
