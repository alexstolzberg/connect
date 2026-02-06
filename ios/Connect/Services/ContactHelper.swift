import Foundation
import UIKit
import MessageUI

enum ContactHelper {
    static func cleanPhone(_ phone: String) -> String {
        phone.filter { $0.isNumber || $0 == "+" }
    }

    static func makeCall(phoneNumber: String) {
        let cleaned = cleanPhone(phoneNumber)
        guard let url = URL(string: "tel:\(cleaned.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? cleaned)"),
              UIApplication.shared.canOpenURL(url) else { return }
        UIApplication.shared.open(url)
    }

    static func sendMessage(phoneNumber: String, body: String = "") {
        let cleaned = cleanPhone(phoneNumber)
        guard let url = URL(string: "sms:\(cleaned)&body=\(body.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? "")"),
              UIApplication.shared.canOpenURL(url) else { return }
        UIApplication.shared.open(url)
    }

    static func sendEmail(email: String, subject: String = "", body: String = "") {
        var components = URLComponents(string: "mailto:\(email.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? email)")
        var queryItems: [URLQueryItem] = []
        if !subject.isEmpty { queryItems.append(URLQueryItem(name: "subject", value: subject)) }
        if !body.isEmpty { queryItems.append(URLQueryItem(name: "body", value: body)) }
        if !queryItems.isEmpty { components?.queryItems = queryItems }
        guard let url = components?.url, UIApplication.shared.canOpenURL(url) else { return }
        UIApplication.shared.open(url)
    }

    static func faceTimeAudio(phoneNumber: String) {
        let cleaned = cleanPhone(phoneNumber)
        guard let url = URL(string: "facetime-audio:\(cleaned)") else { return }
        UIApplication.shared.open(url)
    }

    static func faceTimeVideo(phoneNumber: String) {
        let cleaned = cleanPhone(phoneNumber)
        guard let url = URL(string: "facetime:\(cleaned)") else { return }
        UIApplication.shared.open(url)
    }

    static func canSendMail() -> Bool {
        MFMailComposeViewController.canSendMail()
    }

    static func canSendSMS() -> Bool {
        MFMessageComposeViewController.canSendText()
    }
}
