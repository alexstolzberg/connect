import Foundation
import UIKit
import MessageUI

enum ContactHelper {
    static func cleanPhone(_ phone: String) -> String {
        phone.filter { $0.isNumber || $0 == "+" }
    }

    /// URL for Phone app (call). Use with Link or UIApplication.shared.open.
    static func callURL(phoneNumber: String) -> URL? {
        let cleaned = cleanPhone(phoneNumber)
        guard !cleaned.isEmpty else { return nil }
        return URL(string: "tel:\(cleaned)")
    }

    /// URL for Messages app. Use with Link or UIApplication.shared.open.
    static func messageURL(phoneNumber: String, body: String = "") -> URL? {
        let cleaned = cleanPhone(phoneNumber)
        guard !cleaned.isEmpty else { return nil }
        if body.isEmpty {
            return URL(string: "sms:\(cleaned)")
        }
        let encoded = body.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? body
        return URL(string: "sms:\(cleaned);body=\(encoded)")
    }

    /// URL for default Mail app (mailto:). Use with Link or UIApplication.shared.open.
    static func emailURL(email: String, subject: String = "", body: String = "") -> URL? {
        let trimmed = email.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty, trimmed.contains("@") else { return nil }
        var urlString = "mailto:\(trimmed.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? trimmed)"
        var query: [String] = []
        if !subject.isEmpty { query.append("subject=\(subject.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? subject)") }
        if !body.isEmpty { query.append("body=\(body.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? body)") }
        if !query.isEmpty { urlString += "?" + query.joined(separator: "&") }
        return URL(string: urlString)
    }

    /// URL for FaceTime audio. Use with Link or UIApplication.shared.open.
    static func faceTimeAudioURL(phoneNumber: String) -> URL? {
        let cleaned = cleanPhone(phoneNumber)
        guard !cleaned.isEmpty else { return nil }
        return URL(string: "facetime-audio:\(cleaned)")
    }

    /// URL for FaceTime video. Use with Link or UIApplication.shared.open.
    static func faceTimeVideoURL(phoneNumber: String) -> URL? {
        let cleaned = cleanPhone(phoneNumber)
        guard !cleaned.isEmpty else { return nil }
        return URL(string: "facetime:\(cleaned)")
    }

    /// Open a URL (tel, sms, mailto, facetime). Use from Button action when Link inside a Button doesn’t receive taps.
    static func open(_ url: URL) {
        UIApplication.shared.open(url)
    }

    static func canSendMail() -> Bool {
        MFMailComposeViewController.canSendMail()
    }

    static func canSendSMS() -> Bool {
        MFMessageComposeViewController.canSendText()
    }
}
