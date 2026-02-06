import Foundation
import UIKit

/// Stores connection photos in app Documents. contactPhotoUri in model is a relative path (e.g. "connection_photos/1.jpg").
enum PhotoStorage {
    private static let subdir = "connection_photos"

    static var photosDirectory: URL {
        let dir = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
        let sub = dir.appendingPathComponent(subdir, isDirectory: true)
        try? FileManager.default.createDirectory(at: sub, withIntermediateDirectories: true)
        return sub
    }

    /// Resolve stored path to full file URL. Accepts relative path or filename.
    static func photoURL(for path: String?) -> URL? {
        guard let path = path?.trimmingCharacters(in: .whitespaces), !path.isEmpty else { return nil }
        if path.contains("/") {
            return FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first?.appendingPathComponent(path)
        }
        return photosDirectory.appendingPathComponent(path)
    }

    /// Save image; returns relative path to store in model (e.g. "connection_photos/\(id).jpg"), or nil on failure.
    static func savePhoto(_ image: UIImage, connectionId: Int64) -> String? {
        guard let data = image.jpegData(compressionQuality: 0.85) else { return nil }
        let filename = "\(connectionId).jpg"
        let url = photosDirectory.appendingPathComponent(filename)
        do {
            try data.write(to: url)
            return "\(subdir)/\(filename)"
        } catch {
            return nil
        }
    }

    /// Save image for a new connection (not yet saved). Uses temp id; call replacePhotoAfterInsert when you get the real id.
    static func savePhotoForNewConnection(_ image: UIImage, tempId: String = "new") -> String? {
        guard let data = image.jpegData(compressionQuality: 0.85) else { return nil }
        let filename = "\(tempId).jpg"
        let url = photosDirectory.appendingPathComponent(filename)
        do {
            try data.write(to: url)
            return "\(subdir)/\(filename)"
        } catch {
            return nil
        }
    }

    /// After insert, rename temp file to real id so it persists.
    static func replacePhotoAfterInsert(oldPath: String?, newConnectionId: Int64) {
        guard let old = oldPath, let oldURL = photoURL(for: old) else { return }
        let newFilename = "\(newConnectionId).jpg"
        let newURL = photosDirectory.appendingPathComponent(newFilename)
        try? FileManager.default.removeItem(at: newURL)
        try? FileManager.default.moveItem(at: oldURL, to: newURL)
    }

    static func deletePhoto(at path: String?) {
        guard let url = photoURL(for: path) else { return }
        try? FileManager.default.removeItem(at: url)
    }
}
