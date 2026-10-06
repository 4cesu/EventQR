package com.thedavelopers.eventqr.features.uploads.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.thedavelopers.eventqr.features.uploads.model.dto.StoredFileResponse;
import com.thedavelopers.eventqr.features.uploads.model.entity.StoredFile;
import com.thedavelopers.eventqr.features.uploads.repository.StoredFileRepository;
import com.thedavelopers.eventqr.shared.exceptions.BadRequestException;
import com.thedavelopers.eventqr.shared.exceptions.ResourceNotFoundException;

@Service
@Transactional
public class FileStorageService {

    // Bytes live in Postgres by default. With app.storage.type=s3 new uploads go to an S3-compatible
    // bucket instead (see S3FileContentStore); files already in the database keep being served from
    // there. Uploads are capped at 5 MB with the size check before any full content read, and
    // /content is streamed with a Content-Length. The metadata GET still returns base64 content
    // because the mobile clients decode it directly (EventDetail / EditEventDetails banner
    // previews) - that contract stays until the clients move to /content.
    private static final double EVENT_POSTER_MIN_RATIO = 1.55;
    private static final double EVENT_POSTER_MAX_RATIO = 1.90;

    private static final int PROFILE_PHOTO_MIN_WIDTH = 300;
    private static final int PROFILE_PHOTO_MIN_HEIGHT = 300;

    private static final long MAX_IMAGE_BYTES = 5L * 1024L * 1024L;

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private final StoredFileRepository storedFileRepository;

    /** Present only when app.storage.type=s3; otherwise bytes stay in the database as before. */
    private final FileContentStore contentStore;

    public FileStorageService(StoredFileRepository storedFileRepository) {
        this(storedFileRepository, null);
    }

    @Autowired
    public FileStorageService(StoredFileRepository storedFileRepository,
                              @Autowired(required = false) FileContentStore contentStore) {
        this.storedFileRepository = storedFileRepository;
        this.contentStore = contentStore;
    }

    public StoredFileResponse store(UUID ownerId, String purpose, MultipartFile file) {
        validateFile(file, purpose);
        try {
            byte[] content = file.getBytes();
            StoredFile storedFile = new StoredFile();
            storedFile.setOwnerId(ownerId);
            storedFile.setPurpose(normalizePurpose(purpose));
            storedFile.setFileName(normalizeFileName(file.getOriginalFilename(), content));
            storedFile.setContentType(normalizeContentType(file.getContentType(), content));
            storedFile.setSize(content.length);
            storedFile.setStoredAt(Instant.now());
            if (contentStore == null) {
                storedFile.setContent(content);
                return toResponse(storedFileRepository.save(storedFile), "STORED", true);
            }
            String key = storedFile.getPurpose() + "/" + UUID.randomUUID();
            contentStore.put(key, storedFile.getContentType(), content);
            storedFile.setStorageKey(key);
            try {
                StoredFile saved = storedFileRepository.save(storedFile);
                return new StoredFileResponse(saved.getId(), saved.getOwnerId(), saved.getPurpose(), saved.getFileName(),
                        saved.getContentType(), saved.getSize(), "STORED", saved.getStoredAt(), encode(content));
            } catch (RuntimeException exception) {
                deleteQuietly(key); // don't leave an orphaned object behind a failed insert
                throw exception;
            }
        } catch (IOException exception) {
            throw new BadRequestException("Unable to read uploaded file");
        }
    }

    @Transactional(readOnly = true)
    public StoredFileResponse find(UUID fileId) {
        StoredFile storedFile = requireFile(fileId);
        return toResponse(storedFile, "AVAILABLE", true);
    }

    @Transactional(readOnly = true)
    public StoredFileContent readContent(UUID fileId) {
        StoredFile storedFile = requireFile(fileId);
        String contentType = storedFile.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = MediaTypeDetector.detect(contentOf(storedFile));
        }
        return new StoredFileContent(contentOf(storedFile), contentType);
    }

    public StoredFileResponse delete(UUID fileId) {
        StoredFile existing = requireFile(fileId);
        StoredFileResponse response = toResponse(existing, "DELETED", true);
        storedFileRepository.delete(existing);
        if (existing.getStorageKey() != null && contentStore != null) {
            deleteQuietly(existing.getStorageKey());
        }
        return response;
    }

    private StoredFile requireFile(UUID fileId) {
        return storedFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + fileId));
    }

    private void validateFile(MultipartFile file, String purpose) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }
        // Reject oversize uploads BEFORE pulling the content into memory: MultipartFile
        // may be backed by a temp file, and getBytes() would force a full read of a file
        // we are about to reject.
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new BadRequestException("Image must not exceed 5 MB");
        }
        byte[] content = readBytes(file);
        String detectedType = MediaTypeDetector.detect(content);
        String declaredType = normalizeDeclaredContentType(file.getContentType());
        String originalName = file.getOriginalFilename();
        if (!isAllowedImageType(detectedType) && !isAllowedImageType(declaredType) && !hasAllowedImageExtension(originalName)) {
            throw new BadRequestException("Only JPG, JPEG, and PNG image uploads are supported");
        }
        if (!isAllowedImageType(detectedType)) {
            throw new BadRequestException("Selected file is not a readable JPG, JPEG, or PNG image");
        }
        String normalizedPurpose = normalizePurpose(purpose);
        if ("event-poster".equals(normalizedPurpose) || "event-logo".equals(normalizedPurpose)) {
            validateEventPoster(content);
        } else if ("profile-photo".equals(normalizedPurpose)) {
            validateProfilePhoto(content);
        }
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new BadRequestException("Unable to read uploaded file");
        }
    }

    private boolean isAllowedImageType(String contentType) {
        String normalized = normalizeDeclaredContentType(contentType);
        return "image/jpeg".equals(normalized) || "image/png".equals(normalized);
    }

    private String normalizeDeclaredContentType(String contentType) {
        String normalized = contentType == null ? "" : contentType.trim().toLowerCase(Locale.US);
        return "image/jpg".equals(normalized) ? "image/jpeg" : normalized;
    }

    private boolean hasAllowedImageExtension(String filename) {
        String normalized = filename == null ? "" : filename.trim().toLowerCase(Locale.US);
        return normalized.endsWith(".jpg") || normalized.endsWith(".jpeg") || normalized.endsWith(".png");
    }

    private void validateEventPoster(byte[] content) {
        BufferedImage image = readImage(content, "Event poster");
        int width = image.getWidth();
        int height = image.getHeight();
        double ratio = height == 0 ? 0.0 : (double) width / (double) height;
        // Minimum-dimension check relaxed: the mobile app crops posters to a locked 16:9 frame
        // before upload (scope addition beyond SRS/SDD Module 3), so small source images can
        // produce valid smaller-than-1200px crops. Ratio is still enforced server-side.
        if (ratio < EVENT_POSTER_MIN_RATIO || ratio > EVENT_POSTER_MAX_RATIO) {
            throw new BadRequestException("Event poster must use a landscape 16:9-style ratio");
        }
    }

    private void validateProfilePhoto(byte[] content) {
        BufferedImage image = readImage(content, "Profile photo");
        int width = image.getWidth();
        int height = image.getHeight();
        if (width < PROFILE_PHOTO_MIN_WIDTH || height < PROFILE_PHOTO_MIN_HEIGHT) {
            throw new BadRequestException("Profile photo must be at least 300 x 300 pixels");
        }
    }

    private BufferedImage readImage(byte[] content, String label) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(content));
            if (image == null) {
                throw new BadRequestException(label + " must be a readable JPG, JPEG, or PNG image");
            }
            return image;
        } catch (BadRequestException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new BadRequestException("Unable to validate image");
        }
    }

    private String normalizePurpose(String purpose) {
        return purpose == null || purpose.isBlank() ? "image" : purpose.trim().toLowerCase(Locale.US);
    }

    private String normalizeContentType(String contentType, byte[] content) {
        String detectedType = MediaTypeDetector.detect(content);
        if (isAllowedImageType(detectedType)) {
            return detectedType;
        }
        String declaredType = normalizeDeclaredContentType(contentType);
        if (isAllowedImageType(declaredType)) {
            return declaredType;
        }
        return "application/octet-stream";
    }

    private String normalizeFileName(String originalFileName, byte[] content) {
        String cleanName = originalFileName == null || originalFileName.isBlank() ? "upload" : originalFileName.trim();
        String detectedType = MediaTypeDetector.detect(content);
        if ("image/jpeg".equals(detectedType) && !cleanName.toLowerCase(Locale.US).matches(".*\\.jpe?g$")) {
            return cleanName + ".jpg";
        }
        if ("image/png".equals(detectedType) && !cleanName.toLowerCase(Locale.US).endsWith(".png")) {
            return cleanName + ".png";
        }
        return cleanName;
    }

    /** The file's bytes from wherever they live: object storage if it has a key, else the database. */
    private byte[] contentOf(StoredFile storedFile) {
        if (storedFile.getStorageKey() != null) {
            if (contentStore == null) {
                throw new IllegalStateException(
                        "File is in object storage but app.storage.type is not s3: " + storedFile.getId());
            }
            return contentStore.get(storedFile.getStorageKey());
        }
        return storedFile.getContent();
    }

    private void deleteQuietly(String key) {
        try {
            contentStore.delete(key);
        } catch (RuntimeException exception) {
            log.warn("Could not delete stored object {}", key, exception);
        }
    }

    private StoredFileResponse toResponse(StoredFile storedFile, String status, boolean includeContent) {
        byte[] content = includeContent ? contentOf(storedFile) : null;
        return new StoredFileResponse(
                storedFile.getId(),
                storedFile.getOwnerId(),
                storedFile.getPurpose(),
                storedFile.getFileName(),
                storedFile.getContentType(),
                storedFile.getSize(),
                status,
                storedFile.getStoredAt(),
                encode(content));
    }

    public record StoredFileContent(byte[] content, String contentType) {
    }

    private static String encode(byte[] content) {
        return Base64.getEncoder().encodeToString(content == null ? new byte[0] : content);
    }

    private static class MediaTypeDetector {
        private static String detect(byte[] content) {
            if (content == null || content.length < 4) {
                return "application/octet-stream";
            }
            if ((content[0] & 0xFF) == 0xFF && (content[1] & 0xFF) == 0xD8) {
                return "image/jpeg";
            }
            if ((content[0] & 0xFF) == 0x89 && content[1] == 0x50 && content[2] == 0x4E && content[3] == 0x47) {
                return "image/png";
            }
            return "application/octet-stream";
        }
    }
}
