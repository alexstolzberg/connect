import XCTest
@testable import Connect

final class ConnectTests: XCTestCase {

    // MARK: - TimeFormatter

    func testFormatRelativeTime_justNow() {
        let date = Date()
        let result = TimeFormatter.formatRelativeTime(date: date)
        XCTAssertTrue(result == "just now" || result.contains("second"), "Expected 'just now' or seconds ago, got: \(result)")
    }

    func testFormatRelativeTime_minutesAgo() {
        let date = Calendar.current.date(byAdding: .minute, value: -5, to: Date())!
        let result = TimeFormatter.formatRelativeTime(date: date)
        XCTAssertTrue(result.contains("minute") || result.contains("minutes"))
        XCTAssertTrue(result.contains("ago"))
    }

    func testFormatRelativeTime_daysAgo() {
        let date = Calendar.current.date(byAdding: .day, value: -3, to: Date())!
        let result = TimeFormatter.formatRelativeTime(date: date)
        XCTAssertTrue(result.contains("day") || result.contains("days"))
        XCTAssertTrue(result.contains("ago"))
    }

    func testContactColorCategory_neverContacted_returnsRed() {
        let result = TimeFormatter.contactColorCategory(lastContactedDate: nil, reminderFrequencyDays: 7)
        XCTAssertEqual(result, .red)
    }

    func testContactColorCategory_withinFrequency_returnsGreen() {
        let last = Calendar.current.date(byAdding: .day, value: -2, to: Date())!
        let result = TimeFormatter.contactColorCategory(lastContactedDate: last, reminderFrequencyDays: 7)
        XCTAssertEqual(result, .green)
    }

    func testContactColorCategory_overdueOnePeriod_returnsYellow() {
        let last = Calendar.current.date(byAdding: .day, value: -10, to: Date())!
        let result = TimeFormatter.contactColorCategory(lastContactedDate: last, reminderFrequencyDays: 7)
        XCTAssertEqual(result, .yellow)
    }

    func testContactColorCategory_overdueTwoPeriods_returnsRed() {
        let last = Calendar.current.date(byAdding: .day, value: -20, to: Date())!
        let result = TimeFormatter.contactColorCategory(lastContactedDate: last, reminderFrequencyDays: 7)
        XCTAssertEqual(result, .red)
    }

    // MARK: - ValidationUtils

    func testIsValidEmail_emptyOrNil_returnsTrue() {
        XCTAssertTrue(ValidationUtils.isValidEmail(nil))
        XCTAssertTrue(ValidationUtils.isValidEmail(""))
    }

    func testIsValidEmail_valid_returnsTrue() {
        XCTAssertTrue(ValidationUtils.isValidEmail("test@example.com"))
        XCTAssertTrue(ValidationUtils.isValidEmail("user.name+tag@domain.co"))
    }

    func testIsValidEmail_invalid_returnsFalse() {
        XCTAssertFalse(ValidationUtils.isValidEmail("invalid"))
        XCTAssertFalse(ValidationUtils.isValidEmail("no-at-sign"))
        XCTAssertFalse(ValidationUtils.isValidEmail("@nodomain.com"))
    }

    func testIsValidPhone_emptyOrNil_returnsTrue() {
        XCTAssertTrue(ValidationUtils.isValidPhone(nil))
        XCTAssertTrue(ValidationUtils.isValidPhone(""))
    }

    func testIsValidPhone_valid_returnsTrue() {
        XCTAssertTrue(ValidationUtils.isValidPhone("5551234567"))
        XCTAssertTrue(ValidationUtils.isValidPhone("555-123-4567"))
        XCTAssertTrue(ValidationUtils.isValidPhone("+15551234567"))
    }

    func testIsValidPhone_tooShort_returnsFalse() {
        XCTAssertFalse(ValidationUtils.isValidPhone("123456"))
    }

    func testPhoneError_valid_returnsNil() {
        XCTAssertNil(ValidationUtils.phoneError("5551234567"))
    }

    func testPhoneError_invalid_returnsMessage() {
        XCTAssertNotNil(ValidationUtils.phoneError("123"))
    }

    func testEmailError_valid_returnsNil() {
        XCTAssertNil(ValidationUtils.emailError("a@b.co"))
    }

    func testEmailError_invalid_returnsMessage() {
        XCTAssertNotNil(ValidationUtils.emailError("notanemail"))
    }

    // MARK: - PhoneNumberFormatter

    func testPhoneNumberFormatter_empty_returnsEmpty() {
        XCTAssertEqual(PhoneNumberFormatter.format(nil), "")
        XCTAssertEqual(PhoneNumberFormatter.format(""), "")
    }

    func testPhoneNumberFormatter_tenDigits_formatsAsXXX_XXX_XXXX() {
        XCTAssertEqual(PhoneNumberFormatter.format("5551234567"), "555-123-4567")
    }

    func testPhoneNumberFormatter_withDashes_preservesFormat() {
        let result = PhoneNumberFormatter.format("555-123-4567")
        XCTAssertEqual(result, "555-123-4567")
    }

    func testPhoneNumberFormatter_usWithCountryCode() {
        let result = PhoneNumberFormatter.format("15551234567")
        XCTAssertTrue(result.contains("+1"))
        XCTAssertTrue(result.contains("555"))
    }

    // MARK: - ReminderTimeHelper

    func testReminderTimeHelper_stringFromDate() {
        var components = DateComponents()
        components.hour = 14
        components.minute = 30
        let date = Calendar.current.date(from: components) ?? Date()
        let result = ReminderTimeHelper.string(from: date)
        XCTAssertEqual(result, "14:30")
    }

    func testReminderTimeHelper_dateFromString() {
        let date = ReminderTimeHelper.date(from: "09:45")
        XCTAssertNotNil(date)
        let cal = Calendar.current
        XCTAssertEqual(cal.component(.hour, from: date!), 9)
        XCTAssertEqual(cal.component(.minute, from: date!), 45)
    }

    func testReminderTimeHelper_dateFromNil_returnsNil() {
        XCTAssertNil(ReminderTimeHelper.date(from: nil))
        XCTAssertNil(ReminderTimeHelper.date(from: ""))
    }

    func testReminderTimeHelper_roundtrip() {
        let original = "18:00"
        let date = ReminderTimeHelper.date(from: original)
        XCTAssertNotNil(date)
        let back = ReminderTimeHelper.string(from: date!)
        XCTAssertEqual(back, original)
    }
}
