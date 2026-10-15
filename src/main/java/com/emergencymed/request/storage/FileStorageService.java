package com.emergencymed.request.storage;

import com.emergencymed.request.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private final Path uploadLocation;

    public FileStorageService(@Value("${app.upload-dir:./storage/prescriptions}") String uploadDir) {
        this.uploadLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadLocation);
        } catch (IOException e) {
            log.error("Could not create upload directory: {}", this.uploadLocation, e);
        }
    }

    public static class StoredFile {
        private final String storageFilename;
        private final String originalFilename;
        private final String contentType;
        private final long size;

        public StoredFile(String storageFilename, String originalFilename, String contentType, long size) {
            this.storageFilename = storageFilename;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.size = size;
        }

        public String getStorageFilename() {
            return storageFilename;
        }

        public String getOriginalFilename() {
            return originalFilename;
        }

        public String getContentType() {
            return contentType;
        }

        public long getSize() {
            return size;
        }
    }

    public StoredFile store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("Prescription file must not be empty");
        }

        if (file.getSize() > 2 * 1024 * 1024) {
            throw new ValidationException("File size exceeds 2 MB limit");
        }

        String detectedType = detectAndValidateMimeType(file);

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            originalName = "prescription";
        }

        String extension = getExtensionForMimeType(detectedType);
        String storageFilename = UUID.randomUUID().toString() + extension;

        Path targetPath = this.uploadLocation.resolve(storageFilename).normalize();

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return new StoredFile(storageFilename, originalName, detectedType, file.getSize());
        } catch (IOException e) {
            log.error("Failed to store file: {}", storageFilename, e);
            throw new RuntimeException("Failed to store prescription file", e);
        }
    }

    public Resource loadAsResource(String filename) {
        try {
            Path file = this.uploadLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("File path invalid: " + filename, e);
        }
    }

    private String detectAndValidateMimeType(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[8];
            int read = is.read(header);
            if (read < 4) {
                throw new ValidationException("Invalid file format");
            }

            // PDF: %PDF- (0x25, 0x50, 0x44, 0x46)
            if (header[0] == 0x25 && header[1] == 0x50 && header[2] == 0x44 && header[3] == 0x46) {
                return "application/pdf";
            }

            // JPEG: 0xFF, 0xD8, 0xFF
            if ((header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) {
                return "image/jpeg";
            }

            // PNG: 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
            if ((header[0] & 0xFF) == 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) {
                return "image/png";
            }

            throw new ValidationException("Unsupported file type. Only JPG, PNG, and PDF files are allowed.");
        } catch (IOException e) {
            throw new ValidationException("Failed to read prescription file content");
        }
    }

    private String getExtensionForMimeType(String mime) {
        return switch (mime) {
            case "application/pdf" -> ".pdf";
            case "image/png" -> ".png";
            default -> ".jpg";
        };
    }
}
