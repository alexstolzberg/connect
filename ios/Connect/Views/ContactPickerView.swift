import SwiftUI
import Contacts
import ContactsUI

/// Result of picking a contact: name, first phone, first email.
struct PickedContact {
    let name: String
    let phone: String?
    let email: String?
}

/// Presents the system contact picker; requests access if needed. Calls onSelect with the picked contact's name, phone, email, or onCancel when dismissed.
struct ContactPickerView: UIViewControllerRepresentable {
    var onSelect: (PickedContact) -> Void
    var onCancel: () -> Void
    var onPermissionDenied: (() -> Void)?

    func makeUIViewController(context: Context) -> UIViewController {
        UIViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        guard !context.coordinator.hasPresented else { return }
        context.coordinator.hasPresented = true

        let store = CNContactStore()
        switch CNContactStore.authorizationStatus(for: .contacts) {
        case .notDetermined:
            store.requestAccess(for: .contacts) { granted, _ in
                DispatchQueue.main.async {
                    if granted {
                        presentPicker(from: uiViewController, coordinator: context.coordinator)
                    } else {
                        context.coordinator.onPermissionDenied?()
                        context.coordinator.onCancel()
                    }
                }
            }
        case .authorized:
            presentPicker(from: uiViewController, coordinator: context.coordinator)
        case .denied:
            context.coordinator.onPermissionDenied?()
            context.coordinator.onCancel()
        case .restricted:
            context.coordinator.onPermissionDenied?()
            context.coordinator.onCancel()
        @unknown default:
            context.coordinator.onCancel()
        }
    }

    private func presentPicker(from host: UIViewController, coordinator: Coordinator) {
        let picker = CNContactPickerViewController()
        picker.delegate = coordinator
        coordinator.host = host
        host.present(picker, animated: true)
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(onSelect: onSelect, onCancel: onCancel, onPermissionDenied: onPermissionDenied)
    }

    final class Coordinator: NSObject, CNContactPickerDelegate {
        let onSelect: (PickedContact) -> Void
        let onCancel: () -> Void
        let onPermissionDenied: (() -> Void)?
        weak var host: UIViewController?
        var hasPresented = false

        init(onSelect: @escaping (PickedContact) -> Void, onCancel: @escaping () -> Void, onPermissionDenied: (() -> Void)?) {
            self.onSelect = onSelect
            self.onCancel = onCancel
            self.onPermissionDenied = onPermissionDenied
        }

        func contactPicker(_ picker: CNContactPickerViewController, didSelect contact: CNContact) {
            let name = [contact.givenName, contact.familyName].filter { !$0.isEmpty }.joined(separator: " ")
            let phone = contact.phoneNumbers.first?.value.stringValue
            let email = contact.emailAddresses.first?.value as String?
            picker.dismiss(animated: true) {
                self.onSelect(PickedContact(name: name.isEmpty ? "Unknown" : name, phone: phone?.nilIfEmpty, email: email?.nilIfEmpty))
            }
        }

        func contactPickerDidCancel(_ picker: CNContactPickerViewController) {
            picker.dismiss(animated: true, completion: onCancel)
        }
    }
}

private extension String {
    var nilIfEmpty: String? { isEmpty ? nil : self }
}
